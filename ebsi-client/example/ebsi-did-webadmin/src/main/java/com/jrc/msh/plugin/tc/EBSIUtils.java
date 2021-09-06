package com.jrc.msh.plugin.tc;

import com.as4mail.ebsi.did.DIDDocumentEntryType;
import com.jrc.msh.plugin.tc.exception.EbsiException;
import eu.europa.ec.edelivery.ebsi.DIDService;
import eu.europa.ec.edelivery.ebsi.did.DIDUtils;
import eu.europa.ec.edelivery.ebsi.did.DidValidationApi;
import eu.europa.ec.edelivery.ebsi.did.entities.Did;
import eu.europa.ec.edelivery.ebsi.exceptions.DidException;
import eu.europa.ec.edelivery.ebsi.exceptions.RemoteCallException;
import eu.europa.ec.edelivery.ebsi.exceptions.SessionException;
import eu.europa.ec.edelivery.ebsi.jwt.entities.JWTUtills;
import eu.europa.ec.edelivery.ebsi.jwt.entities.SessionToken;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public class EBSIUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EBSIUtils.class);

    // data
    String keystorePath = "ebsi-keystore.p12";
    String keystorePassword = "test123";
    String keyAlias = "ebsi-edelivery-pilot";
    String keyPassword = "test123";
    String ebsiURl = "https://api.ebsi.xyz";


    String ebsiServiceSessionContext = "/ledger/v1/sessions";
    String ebsiServiceBesuContext = "/ledger/v1/blockchains/besu";
    String ledgerRPC = ebsiURl + ebsiServiceBesuContext;
    String applicationName = "ext-cef-edelivery-pp";

    String audience = "ebsi-ledger";


    JWTUtills loginUtils;
    DIDService didService;
    DIDUtils didUtils;
    DidValidationApi validationApi;

    public EBSIUtils(String didSmartContractAddress) {
        loginUtils = new JWTUtills();
        didService = new DIDService(didSmartContractAddress);
        didUtils = new DIDUtils();
        validationApi = new DidValidationApi();

    }

    public Did getDidDocument(String url) throws EbsiException {

        try {
            return validationApi.geDIDDocument(new URL(url));
        } catch (IOException | DidException e) {
            throw new EbsiException("Could not fetch did document!", e);
        }

    }

    public String saveDidEntry(DIDDocumentEntryType didEntry, String domainOwnerAddress) throws EbsiException {


        X509Certificate certificate;
        String transaction;
        ECPrivateKey privateKey;
        ECPublicKey publicKey;

        try {
            LOG.info("Parse certificate");
            certificate = X509CertificateUtils.getCertificateFromByteArray(didEntry.getCertificate().getBin());
        } catch (CertificateException e) {
            throw new EbsiException("Error occurred while parsing the certificate!", e);
        }


        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getClass().getResourceAsStream("/keystore/" + keystorePath), keystorePassword.toCharArray());
            privateKey = (ECPrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            publicKey = (ECPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
        } catch (KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new EbsiException("Error occurred while accessing keystore!", e);
        }

        LOG.info("Got keys to create session");
        URL url = null;
        SessionToken sessionToken = null;
        try {
            url = new URL(ebsiURl + ebsiServiceSessionContext);
            sessionToken = loginUtils.generateSessionToken(publicKey, privateKey, audience, url, applicationName);
        } catch (SessionException | IOException e) {
            throw new EbsiException("Error occurred while creating session!", e);
        }

        LOG.info("Got keys to create session");
        BigInteger privkey = privateKey.getS();
        ECKeyPair ecKeyPair = ECKeyPair.create(privkey);
        Credentials credentials = Credentials.create(ecKeyPair);
        HttpService service = new HttpService(ledgerRPC); // put fullnode url here
        service.addHeader("Authorization", "Bearer " + sessionToken.getAccessToken());
        LOG.info("Created session Token: " + sessionToken.getAccessToken());

        byte [] didHash;
        try {
            didHash = this.didUtils.generatedDidValue(didEntry.getPartySchema(),
                    didEntry.getPartyID(), certificate);
            didEntry.setDidEntry(didUtils.toHexString(didHash));
        }  catch (IOException | CertificateEncodingException e) {
            throw new EbsiException("Error occurred while executing smart contract transaction!", e);
        }

        Web3j web3j = Web3j.build(service);
        try {
            transaction = didService.addRecordToDocument(domainOwnerAddress,didHash , web3j, credentials,didEntry.getValidInSeconds());
            didEntry.setTransactionHash(transaction);
        } catch (RemoteCallException  e) {
            throw new EbsiException("Error occurred while executing smart contract transaction!", e);
        }

        return transaction;
    }

    public String generateHash(String partySchema, String partId, byte[] certBuff){
        try {
            X509Certificate certificate =   X509CertificateUtils.getCertificateFromByteArray(certBuff);
            return this.didUtils.generatedDidHexValue(partySchema,
                    partId, certificate);
        } catch (CertificateException | IOException e) {
            LOG.error("Error occurred while generating hash!", e);
        }
        return null;
    }

    public String deleteDidEntry(String didEntryValue, String domainOwnerAddress) throws EbsiException {

        String transaction;
        ECPrivateKey privateKey;
        ECPublicKey publicKey;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getClass().getResourceAsStream("/keystore/" + keystorePath), keystorePassword.toCharArray());
            privateKey = (ECPrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            publicKey = (ECPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
        } catch (KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new EbsiException("Error occurred while accessing keystore!", e);
        }

        LOG.info("Got keys to create session");
        URL url = null;
        SessionToken sessionToken = null;
        try {
            url = new URL(ebsiURl + ebsiServiceSessionContext);
            sessionToken = loginUtils.generateSessionToken(publicKey, privateKey, audience, url, applicationName);
        } catch (SessionException | IOException e) {
            throw new EbsiException("Error occurred while creating session!", e);
        }

        BigInteger privkey = privateKey.getS();
        ECKeyPair ecKeyPair = ECKeyPair.create(privkey);
        Credentials credentials = Credentials.create(ecKeyPair);
        HttpService service = new HttpService(ledgerRPC); // put fullnode url here
        service.addHeader("Authorization", "Bearer " + sessionToken.getAccessToken());
        LOG.info("Created session Token: " + sessionToken.getAccessToken());


        Web3j web3j = Web3j.build(service);
        try {
            transaction = didService.removeRecordFromToDocument(domainOwnerAddress,
                    Numeric.hexStringToByteArray(didEntryValue), web3j, credentials);
            LOG.info("Did entry deleted in transaction: " + transaction);
        } catch (RemoteCallException e) {
            throw new EbsiException("Error occurred while executing smart contract transaction!", e);
        }

        return transaction;
    }

}
