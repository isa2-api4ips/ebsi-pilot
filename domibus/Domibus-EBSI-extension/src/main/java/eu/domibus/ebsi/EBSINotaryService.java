package eu.domibus.ebsi;

import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.edelivery.ebsi.NotaryServices;
import eu.europa.ec.edelivery.ebsi.exceptions.RemoteCallException;
import eu.europa.ec.edelivery.ebsi.exceptions.SessionException;
import eu.europa.ec.edelivery.ebsi.jwt.entities.JWTUtills;
import eu.europa.ec.edelivery.ebsi.jwt.entities.SessionToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;


@Service
public class EBSINotaryService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSINotaryService.class);

    private static final String EBSI_REST_VALIDATION = "/timestamp/v1/hashes/";

    private static final String EBSI_REST_SESSION = "/ledger/v1/sessions";
    private static final String EBSI_REST_SERVICE = "/ledger/v1/blockchains/besu";
    private static final String EBSI_REST_AUDIENCE = "ebsi-ledger";

    private static final String HTTP_HEADER_SESSION_AUTH = "Authorization";
    private static final String REST_SESSION_AUTH_TYPE = "Bearer";

    EBSIConfiguration configuration;

    JWTUtills jwtUtil = new JWTUtills();

    public EBSINotaryService(EBSIConfiguration configuration) {
        this.configuration = configuration;
    }

    public String ebsiTimeStampSignature(byte[] byteHashSigValue) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, SessionException, RemoteCallException {
        LOG.info("ebsiTimeStampSignature: [{}]", byteHashSigValue);

        // rest service
        String ebsiServiceUrl = configuration.getEbsiUrl();
        ebsiServiceUrl = StringUtils.removeEnd(ebsiServiceUrl, "/");

        String ledgerRPC = ebsiServiceUrl + EBSI_REST_SERVICE;
        URL ledgerSessionURL  = new URL(ebsiServiceUrl + EBSI_REST_SESSION);

        // keystore
        String pass = configuration.getKeystorePassword();
        String keyPass = configuration.getKeyPassword();
        String keyAlias = configuration.getKeyAlias();
        String keystorePath = configuration.getKeystoreLocation();
        String keystoreType = configuration.getKeystoreType();
        LOG.info("Load keystore: [{}], type: [{}], alias: [{}]", keystorePath, keystoreType, keyAlias);
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        keyStore.load(new FileInputStream(keystorePath), pass.toCharArray());
        ECPrivateKey privateKey = (ECPrivateKey) keyStore.getKey(keyAlias, keyPass.toCharArray());
        ECPublicKey publicKey =  (ECPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();


        // block chain data
        String notaryAddress = configuration.getNotaryAddress();


        // create application session
        SessionToken sessionToken = createSession(ledgerSessionURL, privateKey, publicKey);

        // create application credentials for the Smart contract invoking
        Credentials credentials = getApplicationCredentials(privateKey);

        HttpService service = new HttpService(ledgerRPC); // put full node url here
        service.addHeader(HTTP_HEADER_SESSION_AUTH,REST_SESSION_AUTH_TYPE + " " +sessionToken.getAccessToken() );

        Web3j web3j = Web3j.build(service);


        Bytes32 signatureHashBytes32 = new Bytes32(byteHashSigValue);
        NotaryServices notaryServices = new NotaryServices(notaryAddress);
        String txValue = notaryServices.timestampHash(signatureHashBytes32, web3j, credentials);

        LOG.info("***********************************************************************************");
        LOG.info("Timestamped: [{}]", txValue);
        LOG.info("***********************************************************************************");
        return txValue;
    }

    public String getEbsiRestValidationUrlForHashValue(String hexHashValue){
        String ebsiServiceUrl = configuration.getEbsiUrl();
        ebsiServiceUrl = StringUtils.removeEnd(ebsiServiceUrl, "/");
        return ebsiServiceUrl+EBSI_REST_VALIDATION + hexHashValue;
    }

    public Credentials getApplicationCredentials(ECPrivateKey privateKey) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey.getS());
        return Credentials.create(ecKeyPair);
    }

    public SessionToken createSession(URL sessionUrl, ECPrivateKey privateKey, ECPublicKey publicKey ) throws IOException, SessionException {
        String applicationName = configuration.getApplicationName();
        return jwtUtil.generateSessionToken(publicKey,privateKey, EBSI_REST_AUDIENCE, sessionUrl, applicationName );
    }

}
