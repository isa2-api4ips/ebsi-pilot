package eu.domibus.ebsi.authentication;

import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.PullRequestPmodeData;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.ext.domain.PartyIdDTO;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.edelivery.ebsi.did.DidValidationApi;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import eu.europa.ec.edelivery.ebsi.exceptions.DidException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static eu.domibus.ebsi.conf.EBSIExtensionPropertyManager.EBSI_DID_NAME;
import static eu.domibus.ebsi.conf.EBSIExtensionPropertyManager.EBSI_URL;


@Service
public class DomibusEBSIAuthorizationSpi implements AuthorizationServiceSpi {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusEBSIAuthorizationSpi.class);

    private static final String EBSI_REST_DID_DOCUMENT = "/did/v1/identifiers/";

    private static final long  DID_CACHE_TTL=  5000;

    AuthorizationServiceSpi defaultDomainAuthorizationServiceSpi;
    EBSIConfiguration configuration;

    DidValidationApi didValidationApi = new DidValidationApi();
    Did cachedDidDocument;
    long lastcache = 0;

    public DomibusEBSIAuthorizationSpi( EBSIConfiguration configuration,
            final AuthorizationServiceSpi defaultDomainAuthorizationServiceSpi) {
        this.configuration = configuration;
        this.defaultDomainAuthorizationServiceSpi = defaultDomainAuthorizationServiceSpi;
    }

    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, UserMessageDTO userMessageDTO, UserMessagePmodeData userMessagePmodeData) throws AuthorizationException {
        LOG.info("EBSI  authorization certificate!!");
        defaultDomainAuthorizationServiceSpi.authorize(signingCertificateTrustChain, signingCertificate, userMessageDTO, userMessagePmodeData);

        LOG.info("EBSI  authorization: " + signingCertificate.getSubjectX500Principal());
        LOG.info("EBSI  authorization party name: " + userMessagePmodeData.getPartyName());
        Optional<PartyIdDTO> partyIdDTOOptional = userMessageDTO.getPartyInfo().getFrom().getPartyId().stream().filter(partyIdDTO -> verifyEBSIAuthorization(partyIdDTO, signingCertificate)).findFirst();
        if (!partyIdDTOOptional.isPresent()) {
            LOG.info("EBSI no sender is  authorized: ");
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_REJECTED, "Sender is not authorized by the EBSI");
        } else {
            LOG.info("EBSI authorize: " + partyIdDTOOptional.get().getValue() + "");
        }
    }

    public boolean verifyEBSIAuthorization(PartyIdDTO partyIdDTO, X509Certificate signingCertificate) {
        LOG.info("EBSI no sen1der is  authorized: " + partyIdDTO.getValue() + " " + partyIdDTO.getType() + " " + signingCertificate.getSubjectX500Principal().getName());
        Did document = null;
        try {
            document = getDidDocument();
        } catch (IOException | DidException e) {
            String mesage = "Error occurred while retrieving did document!";
            LOG.error(mesage, e);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, mesage);
        }
        if (document == null) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, "Could not retrieve DID document!");
        }
        return didEntryExists(document, partyIdDTO.getType(), partyIdDTO.getValue(), signingCertificate );

    }

    public boolean  didEntryExists(Did didDocument, String partySchema, String partyId, X509Certificate certificate) throws AuthorizationException {
        try {
            return didValidationApi.didEntryExists(didDocument, partySchema,partyId, certificate );
        } catch (IOException | DidException e) {
            String mesage = "Error occurred while validation EBSI!";
            LOG.error(mesage, e);
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_OTHER, mesage);
        }
    }

    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, PullRequestDTO pullRequestDTO, PullRequestPmodeData pullRequestPmodeData) throws AuthorizationException {
        LOG.info("EBSI  authorization signal: ");
        defaultDomainAuthorizationServiceSpi.authorize(signingCertificateTrustChain, signingCertificate, pullRequestDTO, pullRequestPmodeData);
    }

    public URL getDidDocumentURL() {
        String urlPath = configuration.getEbsiUrl();
        if (StringUtils.isBlank(urlPath)){
            LOG.info("EBSI URL is not given! Configure property: [{}] for did authorization!",EBSI_URL );
            return null;
        }
        urlPath = StringUtils.removeEnd(urlPath, "/");

        String didName = configuration.getEbsiDidName();
        if (StringUtils.isBlank(didName)){
            LOG.info("EBSI DID name is not given! Configure property: [{}] for did authorization!",EBSI_DID_NAME );
            return null;
        }

        URL url = null;
        try {
            url = new URL(urlPath+ EBSI_REST_DID_DOCUMENT + URLEncoder.encode(didName, "UTF-8"));
        }catch  (UnsupportedEncodingException | MalformedURLException ex){
            LOG.info("EBSI DID URL in not valid! Check property: [{}] and [{}] if they are valid!", EBSI_URL, EBSI_DID_NAME, ex);
        }
        return url;
    }

    public Did getDidDocument() throws IOException, DidException {
        if (cachedDidDocument == null || didCacheTimeout()){
            URL didUrl = getDidDocumentURL();
            if (didUrl != null) {
                cachedDidDocument = getDidDocumentForUrl(didUrl);
                // use current time because download can take a while!
                lastcache  =  Calendar.getInstance().getTimeInMillis();
            } else {
                LOG.debug("Can not download DID document! - Null URL!");
            }

        }
        return cachedDidDocument;
    }

    public Did getDidDocumentForUrl(URL didUrl) throws IOException, DidException {
         return didValidationApi.geDIDDocument(didUrl);
    }

    public boolean didCacheTimeout(){
        return Calendar.getInstance().getTimeInMillis() >  DID_CACHE_TTL + lastcache;
    }

    @Override
    public String getIdentifier() {
        return "EBSI_AUTHORIZATION_SPI";
    }
}
