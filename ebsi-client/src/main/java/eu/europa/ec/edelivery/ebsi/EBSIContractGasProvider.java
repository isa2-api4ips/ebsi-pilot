package eu.europa.ec.edelivery.ebsi;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

public class EBSIContractGasProvider implements ContractGasProvider {

    public static ContractGasProvider DEFAULT = new  EBSIContractGasProvider();

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return null;
    }

    @Override
    public BigInteger getGasPrice() {
        return null;
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return  BigInteger.valueOf(285000l);
    }

    @Override
    public BigInteger getGasLimit() {
        return  BigInteger.valueOf(285000l);
    }
}
