package eu.domibus.ebsi;

import eu.domibus.core.ebms3.spi.SoapMessageValidationSPI;


import eu.domibus.core.ebms3.spi.model.SoapValidationResult;
import eu.domibus.ebsi.utils.XMLSignatureUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.edelivery.ebsi.exceptions.TimestampException;
import eu.europa.ec.edelivery.ebsi.timestamp.TimeStampApi;
import eu.europa.ec.edelivery.ebsi.timestamp.entities.TimestampData;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.web3j.utils.Numeric;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * EBSI pilot: Simple timestamp validation SPI implementation
 *
 * @author Joze Rihtarsic
 * @since 4.2.1
 */

@Service
public class TimestampValidation extends SoapMessageValidationSPI {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TimestampValidation.class);
    TimeStampApi timeStampApi = new TimeStampApi();
    XMLSignatureUtils xmlSignatureUtils = new XMLSignatureUtils();

    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryNamespaceAwareThreadLocal = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory;
    });

    public static DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware() {
        return documentBuilderFactoryNamespaceAwareThreadLocal.get();
    }

    public SoapValidationResult validateSoapMessage(InputStream is) {
        SoapValidationResult result = new SoapValidationResult();
        try {
            Element xmlHeaderSignature = xmlSignatureUtils.getSignatureElementFromInputStream(is);
            String validationUrl = extractEBSIValidationString(xmlHeaderSignature);

            TimestampData data = timeStampApi.geTimestampData(new URL(validationUrl));

            String signatureValue = extractSignatureValue(xmlHeaderSignature);
            String hexSigHashValue = null;
            boolean isValidHash =false;
            // validate signature hash value
            if (!StringUtils.isBlank(signatureValue)) {
                byte[] byteHashSigValue = xmlSignatureUtils.getSignatureHashValueSHA256(signatureValue);
                hexSigHashValue = Numeric.toHexStringNoPrefix(byteHashSigValue);
                isValidHash = StringUtils.equals(hexSigHashValue, data.getHash());
            }
            result.addString("NotarizedHashValue", data.getHash())
                    .addString("SignatureHashValue", hexSigHashValue)
                    .addString("RegisteredBy", data.getRegisteredBy())
                    .addLocalDateTime("Timestamp", LocalDateTime.parse(data.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME) )
                    .addString("TxHash", data.getTxHash())
                    .addInteger("BlockNumber", data.getBlockNumber())
                    .addString("ValidationUrl", validationUrl)
                    .addString("IsValidTimestampHash", BooleanUtils.toStringTrueFalse(isValidHash));

            return result;
        } catch (NoSuchAlgorithmException | ParserConfigurationException | TimestampException | SAXException | IOException | SOAPException e) {
            LOG.error("Message timestamp validation parse error! ", e);
        }

        return null;
    }


    public String extractEBSIValidationString(Element signature)  {
        Element ebsiTimestampURL=  xmlSignatureUtils.getEBSITimestampVerificationElement(signature);
        return ebsiTimestampURL!=null?xmlSignatureUtils.getElementText(ebsiTimestampURL):null;
    }

    public String extractSignatureValue(Element signature)  {
        Element signatureValue =  xmlSignatureUtils.getSignatureValueElement(signature);
        return signatureValue!=null?xmlSignatureUtils.getElementText(signatureValue):null;
    }
}
