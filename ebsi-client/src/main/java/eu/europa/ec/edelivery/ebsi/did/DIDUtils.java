package eu.europa.ec.edelivery.ebsi.did;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class DIDUtils {

    public byte[] generatedDidValue(String partySchema, String partyId, X509Certificate certificate) throws IOException, CertificateEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(partySchema.getBytes());
        baos.write("::".getBytes());
        baos.write(partyId.getBytes());
        baos.write("::".getBytes());
        baos.write(certificate.getEncoded());
        return getHashSHA3(baos.toByteArray());
    }

    public String generatedDidHexValue(String partySchema, String partyId, X509Certificate certificate) throws IOException, CertificateEncodingException {
        return  Numeric.toHexString(generatedDidValue(partySchema, partyId, certificate));
    }


    public static byte[] getHashSHA3(byte[] valueToBeHashed) {
        return Hash.sha3(valueToBeHashed);
    }
}
