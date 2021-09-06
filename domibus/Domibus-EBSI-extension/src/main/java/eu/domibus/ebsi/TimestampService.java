package eu.domibus.ebsi;

import eu.domibus.ebsi.conf.EBSIConfiguration;
import eu.domibus.ebsi.ebms3.interceptor.TsaTimeStampRFC3161;
import eu.domibus.ebsi.queue.EBSITimestampJmsMessageSender;
import eu.domibus.ebsi.utils.XMLSignatureUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.wss4j.common.util.XMLUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.web3j.utils.Numeric;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

@Service
public class TimestampService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TimestampService.class);

    protected static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";
    protected static final String SIGNATURE_HASH_ALGORITHM = "http://www.w3.org/2001/04/xmlenc#sha256";

    XMLSignatureUtils signatureUtils;

    EBSIConfiguration configuration;
    EBSITimestampJmsMessageSender timestampJmsMessageSender;
    EBSINotaryService ebsiNotaryService;
    TsaTimeStampRFC3161 tsaTimeStampRFC3161;

    public TimestampService(EBSIConfiguration configuration,
                            EBSINotaryService ebsiNotaryService,
                            EBSITimestampJmsMessageSender timestampJmsMessageSender,
                            XMLSignatureUtils signatureUtils, TsaTimeStampRFC3161 tsaTimeStampRFC3161) {
        this.configuration = configuration;
        this.ebsiNotaryService = ebsiNotaryService;
        this.timestampJmsMessageSender = timestampJmsMessageSender;
        this.signatureUtils = signatureUtils;
        this.tsaTimeStampRFC3161 = tsaTimeStampRFC3161;
    }

    /**
     * Method validates if timestamp is enabled for the message type  (Signal or User message)
     * and then timestamp the fist signature in the message.
     *
     * @param message - Soap message
     */
    public void timestampSoapMessage(SoapMessage message) {
        LOG.info("Timestamp soap message");
        SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
        if (soapMessage == null) {
            LOG.warn("NO message.getContent(SOAPMessage.class)! Skip timestamp!");
            return;
        }

        boolean isUserMessage;
        try {
            isUserMessage = signatureUtils.getUserMessageElement(soapMessage) != null;
        } catch (SOAPException e) {
            LOG.warn("Can not determinate Message type. Skip timestamp!", e);
            return;

        }

        if (!configuration.isTimestampForMessageTypeEnabled(isUserMessage)) {
            LOG.warn("Timestamp for the message type [{}] is not enabled!",
                    (isUserMessage ? "UserMessage" : "SignalMessage"));
            return;
        }
        Exchange exchange = message.getExchange();
        String messageId = (String) exchange.get(MESSAGE_ID_CONTEXT_PROPERTY);
        // timestamp signature
        LOG.info("Timestamp message [{}].", messageId);
        timestampFirstSoapMessageSignature(soapMessage, messageId);
    }

    /**
     * Message extract first signature element in SOAPMessage header and timestamp
     * signature value
     *
     * @param soapMessage - Soap message
     */
    public void timestampFirstSoapMessageSignature(SOAPMessage soapMessage, String messageId) {
        try {
            if (soapMessage.getSOAPHeader() == null) {
                LOG.warn("Soap message does not have SOAPHeader.");
                return;
            }

            Element signatureElement = signatureUtils.getSignatureElement(soapMessage);
            if (signatureElement == null) {
                LOG.info("Soap message does not have Signature. Skip timestamp for the message!");

                if (LOG.isDebugEnabled()) {
                    String outputString =
                            XMLUtils.prettyDocumentToString(soapMessage.getSOAPHeader().getOwnerDocument());
                    LOG.debug("Soap header:" + outputString);
                }
                return;
            }

            Element signatureValueElement = signatureUtils.getSignatureValueElement(signatureElement);
            if (signatureValueElement == null) {
                LOG.error("Invalid signature! Signature does not have SignatureValue element!");
                if (LOG.isDebugEnabled()) {
                    String outputString =
                            XMLUtils.prettyDocumentToString(soapMessage.getSOAPHeader().getOwnerDocument());
                    LOG.debug("Soap header:" + outputString);
                }
                return;
            }
            // get data from signature
            String signatureId = signatureUtils.createElementIdIfMissing(signatureElement, "Signature-");
            String signatureValueId = signatureUtils.createElementIdIfMissing(signatureValueElement, "SignatureValue-");
            String signatureValue = signatureUtils.getElementText(signatureValueElement);

            // set timestamp to Signature Object element
            Document sigDoc = signatureElement.getOwnerDocument();
            boolean asincTimestamp = configuration.getTimestampAsynchronous();

            Element timestampElement = null;
            switch (configuration.getTimestampType()) {
                case EBSI:
                    timestampElement = createEbsiTimestamp(sigDoc, signatureElement, asincTimestamp,
                            signatureValueId,
                            signatureId,
                            signatureValue,
                            messageId
                    );
                    break;
                case TSA:
                    timestampElement = createTsaTimestamp(sigDoc, configuration.getTimestampAsynchronous(), signatureValueId, signatureId, signatureValue);
                    break;
                default:
                    LOG.error("Timestamp [{}] not implemented!", configuration.getTimestampType());
            }

            if (timestampElement != null) {
                Element object = sigDoc.createElementNS(XMLSignatureUtils.XMLDSIG_NAMESPACE, XMLSignatureUtils.XMLDSIG_ELEMENT_OBJECT);
                object.appendChild(timestampElement);
                signatureElement.appendChild(object);
            }
            // for pilot just ignore errors
        } catch (SOAPException e) {
            LOG.error("Error occurred while reading SoapMessage headers.", e);
        } catch (TransformerException | IOException e) {
            LOG.error("Error occurred while adding Timestamp element SoapMessage signature headers.", e);
        } catch (Exception e) {
            LOG.error("Error occurred while adding Timestamp element.", e);
        }
    }

    public Element createTsaTimestamp(Document document, boolean asynchronousTimestamp, String signatureValueId, String signatureID, String signatureValue) throws Exception {
        if (asynchronousTimestamp) {
            LOG.warn("TSA timestamp can not be done asynchronously! Setting is ignored.");
        }

        String timestampURL = configuration.getTimestampTsaUrl();
        if (StringUtils.isBlank(timestampURL)) {
            LOG.warn("TSA timestamp URL is not given. Message is not timestamped!");
            return null;
        }

        byte[] byteHashSigValue = signatureUtils.getSignatureHashValueSHA256(signatureValue);
        LOG.debug("Create TSA timestamp!");
        byte[] timestampRFC3161Encoded = tsaTimeStampRFC3161.getTimestamp(byteHashSigValue, new URL(timestampURL));
        Element encapsulatedTimeStamp = document.createElementNS(XMLSignatureUtils.XADES_NAMESPACE, XMLSignatureUtils.XADES_ELEMENT_ENCAPSULATED_TIMESTAMP);
        encapsulatedTimeStamp.setTextContent(Base64.getEncoder().encodeToString(timestampRFC3161Encoded));

        return signatureUtils.buildXADESTimestampContainer(document, signatureValueId, signatureID, encapsulatedTimeStamp);
    }

    public Element createEbsiTimestamp(Document document, Element signatureElement, boolean asynchronous, String signatureValueId, String signatureID, String signatureValue, String messageId) throws Exception {
        // get by
        byte[] byteHashSigValue = signatureUtils.getSignatureHashValueSHA256(signatureValue);
        String hexHashValue = Numeric.toHexString(byteHashSigValue);
        String verificationURL = ebsiNotaryService.getEbsiRestValidationUrlForHashValue(hexHashValue);
        // check if message already has timestamp. If yes then do not timestamp it again.
        if (isSignatureIsAlreadyNotarized(verificationURL, signatureElement)) {
            LOG.warn("Signature for message [{}] is already notarized! New notarize request is ignored!", messageId);
            return null;
        }


        String transactionHash = null;

        if (asynchronous) {
            // add request to timestamp queue
            timestampJmsMessageSender.addMessageToTimestamp(hexHashValue, messageId);
        } else {
            transactionHash = ebsiNotaryService.ebsiTimeStampSignature(byteHashSigValue);
        }
        return signatureUtils.buildEBSIimestamp(document, signatureID, signatureValueId, verificationURL, SIGNATURE_HASH_ALGORITHM, hexHashValue, transactionHash);
    }

    boolean isSignatureIsAlreadyNotarized(String verificationURL, Element signatureElement) {
        Element sigVerifyElement = signatureUtils.getEBSITimestampVerificationElement(signatureElement);
        return sigVerifyElement != null &&
                Objects.equals(signatureUtils.getElementText(sigVerifyElement), verificationURL);
    }
}
