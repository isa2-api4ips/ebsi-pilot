package eu.europa.ec.edelivery.ebsi;

import eu.europa.ec.edelivery.ebsi.did.DidValidationApi;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import eu.europa.ec.edelivery.ebsi.jwt.entities.JWTUtills;
import eu.europa.ec.edelivery.ebsi.jwt.entities.SessionToken;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.*;

class DIDServiceTest {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    JWTUtills loginUtils = new JWTUtills();
    String contractAddress = "0xFd1d90Ae81547E06079459dCaFCFdB2c01795214";
    DIDService testInstance = new DIDService(contractAddress);

    DidValidationApi didValidationApi = new DidValidationApi();


    @Test
    void addRecordToDocument() throws Exception {
        URL urlValidation = new URL("https://api.ebsi.xyz/did/v1/identifiers/did%3Aebsi%3A0x2cf81263cc679c9132d3375cefd82d4f72c183e5");

        String partySchema = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        String partyid = "domibus-blue";
        X509Certificate certificate =  getCertificate(getClass().getResourceAsStream("/certificates/blue_gw.cer"));

        String identity = "0x2cf81263cc679c9132d3375cefd82d4f72c183e5";
        String pass = "test123";
        String alias = "ebsi-edelivery-pilot";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(DIDServiceTest.class.getResourceAsStream("/keystore/ebsi-keystore.p12"), pass.toCharArray());

        ECPrivateKey privateKey = (ECPrivateKey) keyStore.getKey(alias, pass.toCharArray());


        ECPublicKey publicKey = (ECPublicKey) keyStore.getCertificate(alias).getPublicKey();


        String ebsiServiceUrl = "https://api.ebsi.xyz";
        String ebsiServiceSessionContext = "/ledger/v1/sessions";
        String ebsiServiceBesuContext = "/ledger/v1/blockchains/besu";
        String ledgerRPC = ebsiServiceUrl + ebsiServiceBesuContext;
        String applicationName = "ext-cef-edelivery-pp";

        String audience = "ebsi-ledger";
        URL url = new URL(ebsiServiceUrl + ebsiServiceSessionContext);

        SessionToken sessionToken = loginUtils.generateSessionToken(publicKey, privateKey, audience, url, applicationName);
        assertNotNull(sessionToken);


        BigInteger privkey = privateKey.getS();
        ECKeyPair ecKeyPair = ECKeyPair.create(privkey);
        Credentials credentials = Credentials.create(ecKeyPair);
        HttpService service = new HttpService(ledgerRPC); // put fullnode url here
        service.addHeader("Authorization", "Bearer " + sessionToken.getAccessToken());
        System.out.println("Token: " + sessionToken.getAccessToken());


        Web3j web3j = Web3j.build(service);

       // String transaction = testInstance.addRecordToDocument(identity, partySchema, partyid,certificate, web3j, credentials );
       // assertNotNull(transaction);
        Did document = didValidationApi.geDIDDocument(urlValidation);
        boolean value =  didValidationApi.didEntryExists(document, partySchema, partyid,certificate );
        assertTrue(value);
        String transactionDelete = testInstance.removeRecordFromToDocument(identity, partySchema, partyid,certificate, web3j, credentials );


    }

    public X509Certificate getCertificate(InputStream inputStream) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate)certFactory.generateCertificate(inputStream);
    }
}