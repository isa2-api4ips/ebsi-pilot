package eu.domibus.ebsi.utils;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.common.util.XMLUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;
import java.util.UUID;


@Service
public class XMLSignatureUtils {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(XMLSignatureUtils.class);

    // SOAP
    public static final String SOAP_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String SOAP_HEADER = "Header";

    // XMLdSIG elements
    public static final String XMLDSIG_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
    public static final String XMLDSIG_ELEMENT_SIGNATURE = "Signature";
    public static final String XMLDSIG_ELEMENT_SIGNATURE_VALUE = "SignatureValue";
    public static final String XMLDSIG_ELEMENT_OBJECT = "Object";

    // ebMS 3.0 elements
    public static final String EBMS_NAMESPACE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";
    public static final String EBMS_ELEMENT_USERMESSAGE = "UserMessage";

    // ETSI XAdES
    public static final String XADES_NAMESPACE = "http://uri.etsi.org/01903/v1.1.1";
    public static final String XADES_ELEMENT_QUALIFYING_PROPERTIES = "QualifyingProperties";
    public static final String XADES_ELEMENT_UNSIGNED_PROPERTIES = "UnsignedProperties";
    public static final String XADES_ELEMENT_UNSIGNED_SIGNATURE_PROPERTIES = "UnsignedSignatureProperties";
    public static final String XADES_ELEMENT_SIGNATURE_TIMESTAMP = "SignatureTimeStamp";
    public static final String XADES_ELEMENT_HASHDATAINFO = "HashDataInfo";
    public static final String XADES_ELEMENT_XMLTIMESTAMP = "XMLTimeStamp";
    public static final String XADES_ELEMENT_ENCAPSULATED_TIMESTAMP = "EncapsulatedTimeStamp";
    public static final String XADES_ATTRIBUTE_TARGET = "Target";
    public static final String XADES_ATTRIBUTE_URI = "uri";

    // ETSI XAdES
    public static final String EBSI_NAMESPACE = "http://edelivery.tech.ec.europa.eu/ebsi/timestamp";
    public static final String EBSI_ELEMENT_TIMESTAMP_VERIFY_URL = "TimeStampVerificationUrl";
    public static final String EBSI_ELEMENT_TIMESTAMP = "EBSITimeStamp";
    public static final String EBSI_ELEMENT_DIGEST_METHOD = "DigestMethod";
    public static final String EBSI_ELEMENT_TRANSACTION_HASH = "TransactionHash";
    public static final String EBSI_ATTRIBUTE_ALGORITHM = "Algorithm";


    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryNamespaceAwareThreadLocal = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory;
    });

    public static DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware() {
        return documentBuilderFactoryNamespaceAwareThreadLocal.get();
    }

    /**
     * Method returns first XMLdSIG Signature element from SOAP header in SOAP message!
     * If Signature does not exist it return null.
     *
     * @param soapMessage
     * @return first UserMessage element in SOAPHeader. If UserMessage does not exists, null is returned!
     * @throws SOAPException - parse exception
     */
    public Element getSignatureElement(SOAPMessage soapMessage) throws SOAPException {
        return getFirstElement(soapMessage, XMLDSIG_NAMESPACE, XMLDSIG_ELEMENT_SIGNATURE);
    }

    public Element getSignatureElementFromInputStream(InputStream rawXmlInputStream) throws SOAPException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = getDocumentBuilderFactoryNamespaceAware();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(rawXmlInputStream);


        return getFirstSoapHeadersElement(document, XMLDSIG_NAMESPACE, XMLDSIG_ELEMENT_SIGNATURE);
    }

    /**
     * Method returns first XMLdSIG SignatureValue element from Signature!
     * If SignatureValue element does not exist it return null.
     *
     * @param xmlSignature
     * @return first SignatureValue element in Signature. If SignatureValue does not exists, null is returned!
     */
    public Element getSignatureValueElement(Element xmlSignature) {
        NodeList nodeList = xmlSignature.getElementsByTagNameNS(XMLDSIG_NAMESPACE, XMLDSIG_ELEMENT_SIGNATURE_VALUE);
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        return (Element) nodeList.item(0);
    }

    public Element getEBSITimestampVerificationElement(Element xmlSignature) {
        NodeList nodeList = xmlSignature.getElementsByTagNameNS(EBSI_NAMESPACE, EBSI_ELEMENT_TIMESTAMP_VERIFY_URL);
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        return (Element) nodeList.item(0);
    }

    /**
     * Returns element text
     * @param element
     * @return
     */
    public String getElementText(Element element) {
        return XMLUtils.getElementText(element);
    }


    /**
     * Method returns first User message from SOAP header in SOAP message! If message does not exist it return null.
     *
     * @param soapMessage
     * @return first UserMessage element in SOAPHeader. If UserMessage does not exists, null is returned!
     * @throws SOAPException - parse exception
     */
    public Element getUserMessageElement(SOAPMessage soapMessage) throws SOAPException {
        return getFirstElement(soapMessage, EBMS_NAMESPACE, EBMS_ELEMENT_USERMESSAGE);
    }

    /**
     * Method returns first element with given namespace and element name from SOAP header in SOAP message!
     *
     * @param soapMessage - soap message
     * @param namespace   - namespace of searched element
     * @param elementName - element name of searched element
     * @return first element for given search parameters: namespace and element
     * @throws SOAPException
     */

    public Element getFirstElement(SOAPMessage soapMessage, String namespace, String elementName) throws SOAPException {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if (soapHeader == null) {
            return null;
        }

        NodeList nodeList = soapHeader.getElementsByTagNameNS(namespace, elementName);
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        if (nodeList.getLength() > 0) {
            LOG.warn("Soap message has more than one element [{}:{}]. Return first!", namespace, elementName);
        }
        return (Element) nodeList.item(0);
    }

    public Element getFirstSoapHeadersElement(Document soapMessage, String namespace, String elementName) throws SOAPException {
        NodeList soapHeaders = soapMessage.getElementsByTagNameNS(SOAP_NAMESPACE, SOAP_HEADER);
        if (soapHeaders == null || soapHeaders.getLength()<1) {
            return null;
        }
        Element soapHeader = (Element)soapHeaders.item(0);

        NodeList nodeList = soapHeader.getElementsByTagNameNS(namespace, elementName);
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        if (nodeList.getLength() > 0) {
            LOG.warn("Soap message has more than one element [{}:{}]. Return first!", namespace, elementName);
        }
        return (Element) nodeList.item(0);
    }

    /**
     * Generate XADES Timestamp "envelope for the timestamp":
     *
     * <QualifyingProperties xmlns="http://uri.etsi.org/01903/v1.1.1" Target="#[signatureId]">
     * <UnsignedProperties>
     * <UnsignedSignatureProperties>
     * <SignatureTimeStamp>
     * <HashDataInfo uri="#[signatureValueId]"/>
     * [timestamp element]
     * </SignatureTimeStamp>
     * </UnsignedSignatureProperties>
     * </UnsignedProperties>
     * </QualifyingProperties>
     *
     * @param document         - w3c XML document
     * @param signatureValueId - signature value element  ID
     * @param signatureID      - signature element id
     * @param timestamp        - signature element
     * @return returns QualifyingProperties elemnt
     */

    public Element buildXADESTimestampContainer(Document document, String signatureValueId, String signatureID, Element timestamp) {

        Element qualifyingPropertiesElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_QUALIFYING_PROPERTIES);
        qualifyingPropertiesElement.setAttribute(XADES_ATTRIBUTE_TARGET, prependElementReferenceId(signatureID));

        Element unsignedPropertiesElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_UNSIGNED_PROPERTIES);
        Element unsignedSignaturePropertiesElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_UNSIGNED_SIGNATURE_PROPERTIES);
        Element signatureTimeStampElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_SIGNATURE_TIMESTAMP);
        Element hashDataInfoElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_HASHDATAINFO);
        hashDataInfoElement.setAttribute(XADES_ATTRIBUTE_URI, prependElementReferenceId(signatureValueId));
        signatureTimeStampElement.appendChild(hashDataInfoElement);
        unsignedSignaturePropertiesElement.appendChild(signatureTimeStampElement);
        unsignedPropertiesElement.appendChild(unsignedSignaturePropertiesElement);
        qualifyingPropertiesElement.appendChild(unsignedPropertiesElement);

        signatureTimeStampElement.appendChild(timestamp);

        return qualifyingPropertiesElement;
    }

    public Element buildEBSIimestamp(Document document, String signatureID, String signatureValueId, String timestampVerificationURL, String hashAlgorithm, String signatureHexHash, String transactionHash) {

        // XADES xml timestamp envelope
        Element xmlTimeStampElement = document.createElementNS(XADES_NAMESPACE, XADES_ELEMENT_XMLTIMESTAMP);

        // ebsi timestamp data
        Element timestamp = document.createElementNS(EBSI_NAMESPACE, EBSI_ELEMENT_TIMESTAMP); // Element to be inserted
        Element url = document.createElementNS(EBSI_NAMESPACE, EBSI_ELEMENT_TIMESTAMP_VERIFY_URL); // Element to be inserted
        Element digestMethod = document.createElementNS(EBSI_NAMESPACE, EBSI_ELEMENT_DIGEST_METHOD); // Element to be inserted
        if (StringUtils.isNotBlank(hashAlgorithm)) {
            digestMethod.setAttribute(EBSI_ATTRIBUTE_ALGORITHM, hashAlgorithm);
        }

        url.setTextContent(timestampVerificationURL);
        timestamp.appendChild(url);
        timestamp.appendChild(digestMethod);

        // add transaction hash if exits.
        if (StringUtils.isNotBlank(transactionHash)) {
            Element transactionValue = document.createElementNS(EBSI_NAMESPACE, EBSI_ELEMENT_TRANSACTION_HASH); // Element to be inserted
            transactionValue.setTextContent(transactionHash);
            timestamp.appendChild(transactionValue);
        }
        xmlTimeStampElement.appendChild(timestamp);

        return buildXADESTimestampContainer(document, signatureValueId, signatureID, xmlTimeStampElement);
    }

    /**
     * Prepend Element reference char # if it does not exist.
     *
     * @param elementIdentifier
     * @return element identifier with added #/
     */
    public String prependElementReferenceId(String elementIdentifier) {
        return StringUtils.prependIfMissing(elementIdentifier, "#");
    }


    public String createElementIdIfMissing(Element element, String idPrefix) {
        String elementIdentifierId = element.getAttribute("Id");
        if (StringUtils.isBlank(elementIdentifierId)) {
            elementIdentifierId = idPrefix + UUID.randomUUID().toString();
            element.setAttribute("Id", elementIdentifierId);
        }
        return elementIdentifierId;
    }

    public byte[] getSignatureHashValueSHA256(String signatureValue) throws NoSuchAlgorithmException {
        byte[] byteSigValue = Base64.getDecoder().decode(signatureValue);
        return getHashSHA256(byteSigValue);
    }

    public byte[] getHashSHA256(byte[] valueToBeHashed) throws NoSuchAlgorithmException {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        MessageDigest md = MessageDigest.getInstance("SHA256", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        md.reset();
        md.update(valueToBeHashed);
        return md.digest();
    }


}
