package eu.domibus.ebsi.ebms3.interceptor;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;



class TsaTimeStampRFC3161Test {
    TsaTimeStampRFC3161 testInstance = new TsaTimeStampRFC3161();

    @Test
    public void testTimestamp() throws MalformedURLException, NoSuchAlgorithmException {
        String url = "http://rfc3161timestamp.globalsign.com/advanced";
        String value = "testToStamp";


        testInstance.getTimestamp(getHashSHA256(value.getBytes()), new URL(url));
    }

    private static byte[] getHashSHA256(byte[] valueToBeHashed) throws NoSuchAlgorithmException {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        MessageDigest md = MessageDigest.getInstance("SHA256", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        md.reset();
        md.update(valueToBeHashed);
        return md.digest();
    }
}