package eu.domibus.ebsi;

import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.ebsi.ebms3.interceptor.TsaTimeStampRFC3161;
import eu.domibus.ebsi.enums.EBSITimestampType;
import eu.domibus.ebsi.queue.EBSITimestampJmsMessageSender;
import eu.domibus.ebsi.utils.XMLSignatureUtils;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.soap.SOAPMessage;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;
import java.util.UUID;

@RunWith(JMockit.class)
public class TimestampServiceTest {

    static{
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }
    @Mocked
    EBSIConfiguration configuration;
    @Mocked
    EBSITimestampJmsMessageSender timestampJmsMessageSender;
    @Mocked
    EBSINotaryService ebsiNotaryService;
    @Mocked
    XMLSignatureUtils signatureUtils;
    @Mocked
    TsaTimeStampRFC3161 tsaTimeStampRFC3161;

    @Tested
    TimestampService testInstance;

    @Before
    public void setup() {
        testInstance = new TimestampService(configuration, ebsiNotaryService, timestampJmsMessageSender, signatureUtils, tsaTimeStampRFC3161);
    }

    @Test
    public void testTimestampSignalSoapMessage(@Mocked SoapMessage message,
                                           @Mocked SOAPMessage soapMessage,
                                           @Mocked Exchange exchange) throws Exception {
        // given
        String messageId = UUID.randomUUID().toString();
        new Expectations(testInstance) {{
            message.getContent(SOAPMessage.class);
            result = soapMessage;
            signatureUtils.getUserMessageElement(soapMessage);
            result = null;
            configuration.isTimestampForMessageTypeEnabled(false);
            result = true;
            message.getExchange();
            result = exchange;
            exchange.get(TimestampService.MESSAGE_ID_CONTEXT_PROPERTY);
            result = messageId;
            testInstance.timestampFirstSoapMessageSignature(soapMessage, messageId);
        }};
        // when
        testInstance.timestampSoapMessage(message);
        //then
        new Verifications() {{

        }};
    }

    @Test
    public void testTimestampUserSoapMessage(@Mocked SoapMessage message,
                                         @Mocked SOAPMessage soapMessage,
                                         @Mocked Element element,
                                         @Mocked Exchange exchange) throws Exception {
        // given
        String messageId = UUID.randomUUID().toString();
        new Expectations(testInstance) {{
            message.getContent(SOAPMessage.class);
            result = soapMessage;
            signatureUtils.getUserMessageElement(soapMessage);
            result = element;
            configuration.isTimestampForMessageTypeEnabled(true);
            result = true;
            message.getExchange();
            result = exchange;
            exchange.get(TimestampService.MESSAGE_ID_CONTEXT_PROPERTY);
            result = messageId;
            testInstance.timestampFirstSoapMessageSignature(soapMessage, messageId);
        }};
        // when
        testInstance.timestampSoapMessage(message);
        //then
        new Verifications() {{

        }};
    }

    @Test
    public void testEBSITimestampFirstSoapMessageSignature(
            @Mocked SOAPMessage soapMessage,
            @Mocked Document document,
            @Mocked Element signature,
            @Mocked Element signatureValue,
            @Mocked Element timestampElement,
            @Mocked Element object) throws Exception {
        String messageId = UUID.randomUUID().toString();
        String signatureId = UUID.randomUUID().toString();
        String signatureValueId = UUID.randomUUID().toString();
        String signatureValueText = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        boolean async = false;
        new Expectations(testInstance) {{

            signatureUtils.getSignatureElement(soapMessage);
            result = signature;

            signatureUtils.getSignatureValueElement(signature);
            result = signatureValue;

            signatureUtils.createElementIdIfMissing((Element)any, "Signature-");
            result = signatureId;
            signatureUtils.createElementIdIfMissing((Element)any, "SignatureValue-");
            result = signatureValueId;

            signatureUtils.getElementText(signatureValue);
            result = signatureValueText;

            signature.getOwnerDocument();
            result = document;

            configuration.getTimestampAsynchronous();
            result = async;

            configuration.getTimestampType();
            result = EBSITimestampType.EBSI;

            testInstance.createEbsiTimestamp(document,signature, async,signatureValueId,
                    signatureId,signatureValueText,messageId );
            result = timestampElement;

            document.createElementNS(XMLSignatureUtils.XMLDSIG_NAMESPACE, XMLSignatureUtils.XMLDSIG_ELEMENT_OBJECT);
            result = object;

            object.appendChild(timestampElement);
            signature.appendChild(object);

        }};
        // when
        testInstance.timestampFirstSoapMessageSignature(soapMessage, messageId);
        //then
        new Verifications() {{
            testInstance.createEbsiTimestamp(document,signature, async,signatureValueId,
                    signatureId,signatureValueText,messageId );
            object.appendChild(timestampElement);
            signature.appendChild(object);
        }};
    }
    @Test
    public void testTSATimestampFirstSoapMessageSignature(
            @Mocked SOAPMessage soapMessage,
            @Mocked Document document,
            @Mocked Element signature,
            @Mocked Element signatureValue,
            @Mocked Element timestampElement,
            @Mocked Element object) throws Exception {
        String messageId = UUID.randomUUID().toString();
        String signatureId = UUID.randomUUID().toString();
        String signatureValueId = UUID.randomUUID().toString();
        String signatureValueText = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        boolean async = false;
        new Expectations(testInstance) {{

            signatureUtils.getSignatureElement(soapMessage);
            result = signature;

            signatureUtils.getSignatureValueElement(signature);
            result = signatureValue;

            signatureUtils.createElementIdIfMissing((Element)any, "Signature-");
            result = signatureId;
            signatureUtils.createElementIdIfMissing((Element)any, "SignatureValue-");
            result = signatureValueId;

            signatureUtils.getElementText(signatureValue);
            result = signatureValueText;

            signature.getOwnerDocument();
            result = document;

            configuration.getTimestampAsynchronous();
            result = async;

            configuration.getTimestampType();
            result = EBSITimestampType.TSA;

            testInstance.createTsaTimestamp(document, async,signatureValueId,
                    signatureId,signatureValueText );
            result = timestampElement;

            document.createElementNS(XMLSignatureUtils.XMLDSIG_NAMESPACE, XMLSignatureUtils.XMLDSIG_ELEMENT_OBJECT);
            result = object;

            object.appendChild(timestampElement);
            signature.appendChild(object);

        }};
        // when
        testInstance.timestampFirstSoapMessageSignature(soapMessage, messageId);
        //then
        new Verifications() {{
            testInstance.createTsaTimestamp(document, async,signatureValueId,
                    signatureId,signatureValueText );
            object.appendChild(timestampElement);
            signature.appendChild(object);
        }};
    }


    @Test
    public void testCreateTsaTimestamp(  @Mocked Document document,
                                         @Mocked Element timestamp,
                                         @Mocked SoapMessage message,
                                         @Mocked SOAPMessage soapMessage,
                                         @Mocked Exchange exchange) throws Exception {
        // given
        String signatureId = UUID.randomUUID().toString();
        String signatureValueId = UUID.randomUUID().toString();
        String signatureValueText = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        String tsaURL = "http://" + UUID.randomUUID().toString();
        new Expectations(testInstance) {{
            configuration.getTimestampTsaUrl();
            result = tsaURL;

            tsaTimeStampRFC3161.getTimestamp((byte[])any, (URL) any);
            result  = signatureValueText.getBytes();

            signatureUtils.getSignatureHashValueSHA256(anyString);
            result = "123456789".getBytes();

            document.createElementNS(XMLSignatureUtils.XADES_NAMESPACE, XMLSignatureUtils.XADES_ELEMENT_ENCAPSULATED_TIMESTAMP);
            result = timestamp;

            timestamp.setTextContent(anyString);

            signatureUtils.buildXADESTimestampContainer(document, signatureValueId, signatureId, timestamp);
        }};
        // when
        testInstance.createTsaTimestamp(document, false,signatureValueId,signatureId, signatureValueText );

        //then
        new FullVerifications() {{

        }};
    }

    @Test
    public void testCreateEbsiTimestampAsinc(  @Mocked Document document,
                                         @Mocked Element timestamp,
                                         @Mocked Element signature,
                                         @Mocked SoapMessage message,
                                         @Mocked SOAPMessage soapMessage,
                                         @Mocked Exchange exchange) throws Exception {
        // given
        String messageId = UUID.randomUUID().toString();
        String signatureId = UUID.randomUUID().toString();
        String signatureValueId = UUID.randomUUID().toString();
        String signatureValueText = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        String validationURL = "http://" + UUID.randomUUID().toString();
        new Expectations(testInstance) {{

            ebsiNotaryService.getEbsiRestValidationUrlForHashValue(anyString);
            result  = validationURL;

            signatureUtils.getSignatureHashValueSHA256(anyString);
            result = "123456789".getBytes();

            testInstance.isSignatureIsAlreadyNotarized(anyString, (Element)any);
            result = false;

            timestampJmsMessageSender.addMessageToTimestamp(anyString, messageId);

            signatureUtils.buildEBSIimestamp(document,signatureId, signatureValueId, validationURL, anyString, anyString, null);
            result = timestamp;

        }};
        // when
        Element result = testInstance.createEbsiTimestamp(document, signature,true,signatureValueId,signatureId, signatureValueText, messageId );

        //then
        Assert.assertEquals(timestamp, result);

        new FullVerifications() {{

        }};
    }

    @Test
    public void testIsSignatureIsAlreadyNotarized( @Mocked Element signatureElement,
                                                   @Mocked Element timestampElement
                                                    ) {

        String validationURL = "https://api.ebsi.xyz/timestamp/v1/hashes/0x959645587223d8e2805ddb78fb9f39d77e33db0fd0da8aefd96cb531f0dfa86f";
        new Expectations(testInstance) {{

            signatureUtils.getEBSITimestampVerificationElement(signatureElement);
            result  = timestampElement;

            signatureUtils.getElementText(timestampElement);
            result  = validationURL ;

        }};
        // when
        boolean result = testInstance.isSignatureIsAlreadyNotarized(validationURL, signatureElement);

        //then
        Assert.assertTrue(result);

        new FullVerifications() {{

        }};

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