package eu.europa.ec.edelivery.ebsi.did;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import eu.europa.ec.edelivery.ebsi.did.entities.PublicKey;
import eu.europa.ec.edelivery.ebsi.exceptions.DidException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class DidValidationApi {

    DIDUtils didUtils = new DIDUtils();

    public Did geDIDDocument(URL url) throws IOException, DidException {
        Did didDocument;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");

        InputStream is = null;
        try {
            is = connection.getInputStream();
            didDocument = geDIDDocument(is);
        } catch (IOException | DidException e) {
            String output;
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(
                    (connection.getErrorStream())));
            StringBuffer buffer = new StringBuffer();
            while ((output = br.readLine()) != null) {
                buffer.append(output);
            }
            throw new DidException(output, e);
        } finally {
            is.close();
            connection.disconnect();
        }

        return didDocument;
    }

    public boolean didEntryExists(Did didDocument, String partySchema, String partyId, X509Certificate certificate) throws IOException, DidException {
        String didValue;
        try {
            didValue = didUtils.generatedDidHexValue(partySchema, partyId, certificate);
        } catch (CertificateEncodingException  e) {
            throw new DidException("Error occurred while generation did document", e);
        }
        return didEntryExists(didDocument, didValue);
    }


    public boolean didEntryExists(Did didDocument, String value) {

        if (value == null) {
            return false;
        }

        for (PublicKey key : didDocument.getPublicKey()) {
            if (key.getValue() == null) {
                continue;
            }
            if (value.equalsIgnoreCase(key.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static Did geDIDDocument(InputStream is) throws DidException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(is, Did.class);
        } catch (JsonProcessingException e) {
            throw new DidException("Error occurred while parsing JSON did response", e);
        } catch (IOException e) {
            throw new DidException("Error occurred while reading JSON di response", e);
        }
    }
}
