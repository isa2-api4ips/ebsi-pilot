package eu.domibus.ebsi.utils;

import org.junit.Assert;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.*;

public class XMLSignatureUtilsTest {

    XMLSignatureUtils testInstance = new XMLSignatureUtils();

    @Test
    public void testGetSignatureHashValueSHA256() throws NoSuchAlgorithmException {
        // given
        String signValue = UUID.randomUUID().toString();
        String signatureValueText = Base64.getEncoder().encodeToString(signValue.getBytes());
        byte[] valResult = getHashSHA256(signValue.getBytes());
        // when
        byte[] hashValue = testInstance.getSignatureHashValueSHA256(signatureValueText);
        // then
        Assert.assertArrayEquals(valResult,hashValue );
    }

    /**
     * Test utils
     *
     * @param valueToBeHashed
     * @return
     * @throws NoSuchAlgorithmException
     */
    public byte[] getHashSHA256(byte[] valueToBeHashed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA256", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        md.update(valueToBeHashed);
        return md.digest();
    }

}