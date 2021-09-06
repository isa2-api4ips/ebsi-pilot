package eu.europa.ec.edelivery.ebsi;

import eu.europa.ec.edelivery.ebsi.contract.NotaryContract;
import eu.europa.ec.edelivery.ebsi.exceptions.RemoteCallException;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class NotaryServices {

    final String notaryAddress;

    public NotaryServices(String notaryAddress) {
        this.notaryAddress = notaryAddress;
    }

    public String timestampHash(Bytes32 bytes32Hash, Web3j web3j, Credentials credentials) throws RemoteCallException {
        String transactionHash;
        NotaryContract contract = NotaryContract.load(notaryAddress, web3j, credentials, EBSIContractGasProvider.DEFAULT);
        RemoteCall<TransactionReceipt> remoteCall = contract.addRecord(bytes32Hash);
        try {
            TransactionReceipt receipt = remoteCall.send();
            transactionHash = receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RemoteCallException("Error occurred while calling addRecord!", e);
        }
        return transactionHash;
    }
}
