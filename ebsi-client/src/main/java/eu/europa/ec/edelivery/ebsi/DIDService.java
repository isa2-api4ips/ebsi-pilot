package eu.europa.ec.edelivery.ebsi;

import eu.europa.ec.edelivery.ebsi.did.DIDUtils;
import eu.europa.ec.edelivery.ebsi.exceptions.DidException;
import eu.europa.ec.edelivery.ebsi.exceptions.RemoteCallException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import eu.europa.ec.edelivery.ebsi.contracts.EthereumDIDRegistry;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class DIDService {

    DIDUtils didUtils = new DIDUtils();

    public static final String IDENTITY_TYPE="did/pub/RSA/veriKey/hash";


    final String didSmartcontractAddress;

    public DIDService(String didSmartcontractAddress) {
        this.didSmartcontractAddress = didSmartcontractAddress;
    }


    public String addRecordToDocument(String didDocument, String partySchema, String partyId, X509Certificate certificate, Web3j web3j, Credentials credentials) throws RemoteCallException, DidException {
        byte[] value = new byte[0];
        try {
            value = didUtils.generatedDidValue(partySchema,partyId, certificate );
        } catch (IOException | CertificateEncodingException e) {
            throw new DidException("Error occurred while validating did entry", e);
        }
        return addRecordToDocument(didDocument, value, web3j, credentials);
    }

    public String addRecordToDocument(String didDocument, byte[] value, Web3j web3j, Credentials credentials) throws RemoteCallException {
        String transactionHash;

        EthereumDIDRegistry contract = EthereumDIDRegistry.load(didSmartcontractAddress, web3j, credentials, EBSIContractGasProvider.DEFAULT);

        RemoteCall<TransactionReceipt> remoteCall = contract.setAttribute(didDocument, stringToByte32(IDENTITY_TYPE),
                value,
                BigInteger.valueOf(86400));

        try {
            TransactionReceipt receipt = remoteCall.send();
            transactionHash = receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RemoteCallException("Error occurred while calling addRecord!", e);
        }
        return transactionHash;
    }

    public String removeRecordFromToDocument(String didDocument, String partySchema, String partyId, X509Certificate certificate, Web3j web3j, Credentials credentials) throws RemoteCallException, DidException {
        byte[] value = new byte[0];
        try {
            value = didUtils.generatedDidValue(partySchema,partyId, certificate );
        } catch (IOException | CertificateEncodingException e) {
            throw new DidException("Error occurred while validating did entry", e);
        }
        return removeRecordFromToDocument(didDocument, value, web3j, credentials);
    }

    public String removeRecordFromToDocument(String didDocument, byte[] value, Web3j web3j, Credentials credentials) throws RemoteCallException {
        String transactionHash;

        EthereumDIDRegistry contract = EthereumDIDRegistry.load(didSmartcontractAddress, web3j, credentials, EBSIContractGasProvider.DEFAULT);
        RemoteCall<TransactionReceipt> remoteCall = contract.revokeAttribute(didDocument,stringToByte32("did/pub/RSA/veriKey/hash"),
                value);
        try {
            TransactionReceipt receipt = remoteCall.send();
            transactionHash = receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RemoteCallException("Error occurred while calling addRecord!", e);
        }
        return transactionHash;
    }

    byte[] stringToByte32(String val) {
        byte[] byteArray = new byte[32];
        byte[] copyBytes = val.getBytes();
        for (int i = 0; i < byteArray.length && i < copyBytes.length; i++) {
            byteArray[i] = copyBytes[i];
        }
        return byteArray;
    }
}
