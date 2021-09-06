package eu.domibus.web.rest;

import com.google.common.io.CharStreams;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.core.ebms3.spi.SoapMessageValidationSPI;
import eu.domibus.core.ebms3.spi.model.SoapValidationResult;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.SoapServiceImpl;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.MessageTimestampInfoRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @author Joze RIHTARSIC
 * @since 4.2.1 - EBSI Pilot example
 * <p>
 * A service for retrieving message timestamp data. Class is part of EBSI pilot project
 * and is not part of domibus standard deployment!
 */

@RestController
@RequestMapping(value = "/rest/timestamp")
public class EBSITimestampResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSITimestampResource.class);

    Optional<SoapMessageValidationSPI> timestampValidation;

    SoapServiceImpl soapService;

    SignalMessageDao signalMessageDao;

    private ErrorHandlerService errorHandlerService;

    protected SoapUtil soapUtil;

    public EBSITimestampResource(Optional<SoapMessageValidationSPI> timestampValidation, SoapServiceImpl soapService, SignalMessageDao signalMessageDao, ErrorHandlerService errorHandlerService, SoapUtil soapUtil) {
        this.timestampValidation = timestampValidation;
        this.soapService = soapService;
        this.signalMessageDao=signalMessageDao;
        this.errorHandlerService = errorHandlerService;
        this.soapUtil = soapUtil;
    }

    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<ErrorRO> handleMessagingException(MessagingException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler({eu.domibus.messaging.MessageNotFoundException.class})
    public ResponseEntity<ErrorRO> handleMessageNotFoundException(eu.domibus.messaging.MessageNotFoundException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/validate")
    public ResponseEntity<MessageTimestampInfoRO> validateTimestampMessage(@RequestParam(value = "messageId", required = true) String messageId)
            throws MessageNotFoundException, ConfigurationException, IOException {
        LOG.info("Get timestamp for the message [{}]", messageId);

        if (!timestampValidation.isPresent()) {
            LOG.info("No SoapMessageValidationSPI validation registered to the Domibus! Check if extension with SoapMessageValidationSPI is present!");
            throw new ConfigurationException("No extension with SoapMessageValidationSPI registered to the Domibus!");
        }

        String soapEnvelope = soapService.getSoapEnvelopeAsRAWXml(messageId);
        LOG.info("Got timestamp soap message [{}]", soapEnvelope);
        if (soapEnvelope == null) {
            throw new MessageNotFoundException("Soap message for: [" + messageId + "] does was already deleted!");
        }

        // get data from EBSI
        try (StringReader stringReader = new StringReader(soapEnvelope); InputStream targetStream =
                new ByteArrayInputStream(CharStreams.toString(stringReader)
                        .getBytes(StandardCharsets.UTF_8.name()))) {
            SoapValidationResult result = timestampValidation.get().validateSoapMessage(targetStream);

            MessageTimestampInfoRO msg = new MessageTimestampInfoRO();
            msg.setMessageId(messageId);
            msg.setNotarizedHashValue(result.getStringValue("NotarizedHashValue"));
            msg.setSignatureHashValue(result.getStringValue("SignatureHashValue"));
            // browser could be in different timezone than server!
            LocalDateTime time = result.getLocalDateTimeValue("Timestamp");
            Date timestamp = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
            msg.setTimestamp(timestamp);
            msg.setBlockNumber(result.getIntegerValue("BlockNumber"));
            msg.setRegisteredBy(result.getStringValue("RegisteredBy"));
            msg.setTransactionHash(result.getStringValue("TxHash"));
            msg.setValidationUrl(result.getStringValue("ValidationUrl"));
            msg.setIsValidSignatureHash(result.getStringValue("IsValidTimestampHash"));

            return new ResponseEntity(msg, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/download")
    public ResponseEntity<ByteArrayResource> downloadUserMessage(@RequestParam(value = "messageId", required = true) String messageId)
            throws MessageNotFoundException, IOException {

        String soapEnvelope = soapService.getSoapEnvelopeAsRAWXml(messageId);
        LOG.info("Got timestamp soap message [{}]", soapEnvelope);
        if (soapEnvelope == null) {
            throw new MessageNotFoundException("Soap message for: [" + messageId + "] was already deleted!");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .header("content-disposition", "attachment; filename=" + messageId + ".xml")
                .body(new ByteArrayResource(soapEnvelope.getBytes()));
    }

    @RequestMapping(value = "/downloadByRefMessageId")
    public ResponseEntity<ByteArrayResource> downloadResponse(@RequestParam(value = "messageId", required = true) String messageId)
            throws MessageNotFoundException, IOException {


        List<String> signalMessages = signalMessageDao.findSignalMessageIdsByRefMessageId(messageId);
        if (signalMessages.isEmpty()) {
            throw new MessageNotFoundException("Signal message for: [" + messageId + "] does not exits!");
        }
        // for ebsi pilot use just fist one.
        String signalMessageId = signalMessages.get(0);

        String soapEnvelope = soapService.getSoapEnvelopeAsRAWXml(signalMessageId);
        LOG.info("Got timestamp soap message [{}]", soapEnvelope);
        if (soapEnvelope == null) {
            throw new MessageNotFoundException("Signal message ["+signalMessageId+"] message for: [" + messageId + "] was already deleted!");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .header("content-disposition", "attachment; filename=" + messageId + "-ref.xml")
                .body(new ByteArrayResource(soapEnvelope.getBytes()));
    }

}
