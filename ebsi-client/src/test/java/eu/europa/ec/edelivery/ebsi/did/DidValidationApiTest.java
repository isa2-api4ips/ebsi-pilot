package eu.europa.ec.edelivery.ebsi.did;

import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import eu.europa.ec.edelivery.ebsi.exceptions.DidException;
import eu.europa.ec.edelivery.ebsi.exceptions.TimestampException;
import eu.europa.ec.edelivery.ebsi.timestamp.entities.TimestampData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class DidValidationApiTest {
    DidValidationApi testInstance = new DidValidationApi();

    @Test
    void testGeDIDDocument() throws IOException, DidException {

        URL url = new URL("https://api.ebsi.xyz/did/v1/identifiers/did%3Aebsi%3A0x2cf81263cc679c9132d3375cefd82d4f72c183e5");

        Did document = testInstance.geDIDDocument(url);

        assertNotNull(document);

        assertEquals("did:ebsi:0x2cf81263cc679c9132d3375cefd82d4f72c183e5", document.getId());
        assertEquals(3, document.getPublicKey().size());

    }
}