package eu.europa.ec.edelivery.ebsi.jwt.entities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.edelivery.ebsi.exceptions.SessionException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

public class JWTUtills {

    public SessionToken generateSessionToken(ECPublicKey publicKey, ECPrivateKey privateKey, String audience, URL url, String issuer) throws SessionException, IOException {

        Calendar c = Calendar.getInstance();
        Date startDate = c.getTime();
        c.add(Calendar.SECOND, 15);
        Date expireAtDate = c.getTime();

        String token = null;
        try {
            Algorithm algorithm = Algorithm.ECDSA256K(publicKey, privateKey);

            token = JWT.create()
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .withIssuedAt(startDate)
                    .withExpiresAt(expireAtDate)
                    .sign(algorithm);
        } catch (JWTCreationException ex) {
            throw new SessionException(ex);
        }


        SessionToken sessionToken = null;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        StringWriter sw = new StringWriter();
        sw.write("{\n" +
                "  \"grantType\": \"urn:ietf:params:oauth:grant-type:jwt-bearer\",\n" +
                "  \"assertion\": \"" + token + "\"\n" +
                "}");

        OutputStream os = connection.getOutputStream();
        os.write(sw.toString().getBytes());
        os.flush();
        InputStream is = null;
        try {
            is = connection.getInputStream();
            sessionToken = getSessionToken(is);
        } catch (IOException e) {
            String output;
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(
                    (connection.getErrorStream())));
            StringBuffer buffer = new StringBuffer();
            while ((output = br.readLine()) != null) {
                buffer.append(output);
            }
            throw new SessionException(output, e);
        }
        connection.getResponseCode();
        connection.disconnect();

        return sessionToken;
    }

    public ECPublicKey readPublicKey(File file) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("EC", "BC");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return (ECPublicKey) factory.generatePublic(pubKeySpec);
        }
    }

    public ECPrivateKey readPrivateKey(File file) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("EC");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return (ECPrivateKey) factory.generatePrivate(privKeySpec);
        }
    }

    public static SessionToken getSessionToken(InputStream is) throws SessionException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(is, SessionToken.class);
        } catch (JsonProcessingException e) {
            throw new SessionException("Error occurred while parsing JSON session response", e);
        } catch (IOException e) {
            throw new SessionException("Error occurred while reading JSON session response", e);
        }
    }
}
