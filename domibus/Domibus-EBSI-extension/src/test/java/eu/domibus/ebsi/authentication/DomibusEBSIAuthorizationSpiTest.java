package eu.domibus.ebsi.authentication;

import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.ebsi.TimestampService;
import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.ext.domain.PartyIdDTO;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.x500.X500Principal;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.UUID;

import static eu.domibus.ebsi.conf.EBSIExtensionPropertyManager.EBSI_DID_NAME;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@RunWith(JMockit.class)
public class DomibusEBSIAuthorizationSpiTest {


    AuthorizationServiceSpi defaultDomainAuthorizationServiceSpi = null;
    @Mocked
    EBSIConfiguration configuration;

    @Tested
    DomibusEBSIAuthorizationSpi testInstance;

    @Before
    public void setup() {
        testInstance = new DomibusEBSIAuthorizationSpi(configuration, defaultDomainAuthorizationServiceSpi);
    }

    @Test
    public void testGetDidDocumentURL() {
        // given
        String url = "http://"+UUID.randomUUID().toString()+"/";
        String didDocument = "did:ebsi:"+UUID.randomUUID().toString();
        String expectedURL = url + "did/v1/identifiers/" +didDocument.replaceAll(":","%3A");
        new Expectations() {{
            configuration.getEbsiUrl();
            result = url;
            configuration.getEbsiDidName();
            result = didDocument;
        }};
        // when
        URL result = testInstance.getDidDocumentURL();
        //then
        assertNotNull(result);
        assertEquals(expectedURL, result.toString());
    }

    @Test
    public void testGetDidDocument(@Mocked Did didDocument) throws Exception {
        // given
        URL url = new URL("http://"+UUID.randomUUID().toString()+"");
        new Expectations(testInstance) {{

            testInstance.getDidDocumentURL();
            result = url;

            testInstance.getDidDocumentForUrl(url);
            result = didDocument;
        }};
        // when
        Did result = testInstance.getDidDocument();
        //then
        assertNotNull(result);
        assertEquals(didDocument, result);
    }

    @Test
    public void testVerifyEBSIAuthorization(@Mocked Did didDocument,
                                            @Mocked PartyIdDTO partyIdDTO,
                                            @Mocked X509Certificate certificate,
                                            @Mocked X500Principal principal ) throws Exception {
        // given
        String schema = "test-schema-type";
        String partyId = "0007:123456578";
        String certSubject = "UUID.randomUUID().toString()";
        new Expectations(testInstance) {{

            testInstance.getDidDocument();
            result = didDocument;

            partyIdDTO.getValue();
            result=partyId;

            partyIdDTO.getType();
            result=schema;

            certificate.getSubjectX500Principal();
            result=principal;

            principal.getName();
            result=certSubject;


            testInstance.didEntryExists(didDocument, schema, partyId, certificate);
            result = true;
        }};
        // when
        boolean result = testInstance.verifyEBSIAuthorization(partyIdDTO,certificate );
        //then
        assertTrue(result);

        new FullVerifications (){{

        }};
    }


    /*
    @Test
    public void authorize() {
    }


    @Test
    public void testAuthorize() {
    }

    @Test
    public void getIdentifier() {
    }*/
}