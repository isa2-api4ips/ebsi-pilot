package eu.domibus.plugin.jms;


import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.FileUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * This class is responsible for transformations from {@link javax.jms.MapMessage} to {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author Padraig McGourty, Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */


@Service("jmsMessageTransformer")
public class JMSMessageTransformer implements MessageRetrievalTransformer<MapMessage>, MessageSubmissionTransformer<MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSMessageTransformer.class);

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Autowired
    protected FileUtilExtService fileUtilExtService;

    /**
     * Transforms {@link eu.domibus.plugin.Submission} to {@link javax.jms.MapMessage}
     *
     * @param submission the message to be transformed     *
     * @return result of the transformation as {@link javax.jms.MapMessage}
     */
    @Override
    public MapMessage transformFromSubmission(final Submission submission, final MapMessage messageOut) {
        try {
            if (submission.getMpc() != null) {
                messageOut.setStringProperty(MPC, submission.getMpc());
            }
            messageOut.setStringProperty(ACTION, submission.getAction());
            messageOut.setStringProperty(SERVICE, submission.getService());
            messageOut.setStringProperty(SERVICE_TYPE, submission.getServiceType());
            messageOut.setStringProperty(CONVERSATION_ID, submission.getConversationId());
            messageOut.setStringProperty(MESSAGE_ID, submission.getMessageId());

            for (final Submission.Party fromParty : submission.getFromParties()) {
                messageOut.setStringProperty(FROM_PARTY_ID, fromParty.getPartyId());
                messageOut.setStringProperty(FROM_PARTY_TYPE, fromParty.getPartyIdType());
            }
            messageOut.setStringProperty(FROM_ROLE, submission.getFromRole());

            for (final Submission.Party toParty : submission.getToParties()) {
                messageOut.setStringProperty(TO_PARTY_ID, toParty.getPartyId());
                messageOut.setStringProperty(TO_PARTY_TYPE, toParty.getPartyIdType());
            }
            messageOut.setStringProperty(TO_ROLE, submission.getToRole());


            for (final Submission.TypedProperty p : submission.getMessageProperties()) {
                if (p.getKey().equals(PROPERTY_ORIGINAL_SENDER)) {
                    messageOut.setStringProperty(PROPERTY_ORIGINAL_SENDER, p.getValue());
                    continue;
                }
                if (p.getKey().equals(PROPERTY_ENDPOINT)) {
                    messageOut.setStringProperty(PROPERTY_ENDPOINT, p.getValue());
                    continue;
                }
                if (p.getKey().equals(PROPERTY_FINAL_RECIPIENT)) {
                    messageOut.setStringProperty(PROPERTY_FINAL_RECIPIENT, p.getValue());
                    continue;
                }
                //only reached if none of the predefined properties are set
                messageOut.setStringProperty(PROPERTY_PREFIX + p.getKey(), p.getValue());
                messageOut.setStringProperty(PROPERTY_TYPE_PREFIX + p.getKey(), p.getType());
            }
            messageOut.setStringProperty(PROTOCOL, "AS4");
            messageOut.setStringProperty(AGREEMENT_REF, submission.getAgreementRef());
            messageOut.setStringProperty(AGREEMENT_REF_TYPE, submission.getAgreementRefType());
            messageOut.setStringProperty(REF_TO_MESSAGE_ID, submission.getRefToMessageId());

            // save the first payload (payload_1) for the bodyload (if exists)
            int counter = 1;
            for (final Submission.Payload p : submission.getPayloads()) {
                if (p.isInBody()) {
                    counter = 2;
                    break;
                }
            }

            final boolean putAttachmentsInQueue = Boolean.parseBoolean(getProperty(PUT_ATTACHMENTS_IN_QUEUE));
            for (final Submission.Payload p : submission.getPayloads()) {
                // counter is increased for payloads (not for bodyload which is always set to payload_1)
                counter = transformFromSubmissionHandlePayload(messageOut, putAttachmentsInQueue, counter, p);
            }
            messageOut.setIntProperty(TOTAL_NUMBER_OF_PAYLOADS, submission.getPayloads().size());
        } catch (final JMSException | IOException ex) {
            LOG.error("Error while filling the MapMessage", ex);
            throw new DefaultJmsPluginException(ex);
        }

        return messageOut;
    }

    protected String getProperty(String propertyName) {
        return domibusPropertyExtService.getProperty(JMS_PLUGIN_PROPERTY_PREFIX + "." + propertyName);
    }

    private int transformFromSubmissionHandlePayload(MapMessage messageOut, boolean putAttachmentsInQueue, int counter, Submission.Payload p) throws JMSException, IOException {
        if (p.isInBody()) {
            if (p.getPayloadDatahandler() != null) {
                messageOut.setBytes(MessageFormat.format(PAYLOAD_NAME_FORMAT, 1), IOUtils.toByteArray(p.getPayloadDatahandler().getInputStream()));
                messageOut.setStringProperty(P1_IN_BODY, "true");
            }

            messageOut.setStringProperty(MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, 1), findMime(p.getPayloadProperties()));
            messageOut.setStringProperty(MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, 1), p.getContentId());
        } else {
            final String payContID = MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, counter);
            final String propPayload = MessageFormat.format(PAYLOAD_NAME_FORMAT, counter);
            final String payMimeTypeProp = MessageFormat.format(PAYLOAD_MIME_TYPE_FORMAT, counter);
            final String payFileNameProp = MessageFormat.format(PAYLOAD_FILE_NAME_FORMAT, counter);
            if (p.getPayloadDatahandler() != null) {
                if (putAttachmentsInQueue) {
                    LOG.debug("putAttachmentsInQueue is true");
                    messageOut.setBytes(propPayload, IOUtils.toByteArray(p.getPayloadDatahandler().getInputStream()));
                } else {
                    LOG.debug("putAttachmentsInQueue is false");
                    messageOut.setStringProperty(payFileNameProp, findFilename(p.getPayloadProperties()));
                }
            }
            messageOut.setStringProperty(payMimeTypeProp, findMime(p.getPayloadProperties()));
            messageOut.setStringProperty(payContID, p.getContentId());
            counter++;
        }
        return counter;
    }

    private String findElement(String element, Collection<Submission.TypedProperty> props) {
        for (Submission.TypedProperty prop : props) {
            if (element.equals(prop.getKey()) && isEmpty(trim(prop.getType()))) {
                return prop.getValue();
            }
        }
        return null;
    }

    private String findMime(Collection<Submission.TypedProperty> props) {
        return findElement(MIME_TYPE, props);
    }

    private String findFilename(Collection<Submission.TypedProperty> props) {
        return findElement(PAYLOAD_FILENAME, props);
    }

    public QueueContext getQueueContext(final String messageId, final MapMessage messageIn) throws JMSException {
        String service = getService(messageIn);
        String action = getAction(messageIn);
        return new QueueContext(messageId, service, action);
    }

    protected String getService(final MapMessage messageIn) throws JMSException {
        return getPropertyWithFallback(messageIn, SERVICE);
    }

    protected String getAction(final MapMessage messageIn) throws JMSException {
        return getPropertyWithFallback(messageIn, ACTION);
    }

    /**
     * Transforms {@link javax.jms.MapMessage} to {@link eu.domibus.plugin.Submission}
     *
     * @param messageIn the message ({@link javax.jms.MapMessage}) to be tranformed
     * @return the result of the transformation as {@link eu.domibus.plugin.Submission}
     */
    @Override
    public Submission transformToSubmission(final MapMessage messageIn) {
        final Submission target = new Submission();
        try {
            target.setMpc(messageIn.getStringProperty(MPC));
            populateMessageInfo(target, messageIn);
            populatePartyInfo(target, messageIn);
            populateCollaborationInfo(target, messageIn);
            populateMessageProperties(target, messageIn);
            populatePayloadInfo(target, messageIn);
        } catch (final JMSException ex) {
            LOG.error("Error while getting properties from MapMessage", ex);
            throw new DefaultJmsPluginException(ex);
        }
        return target;
    }

    private void populateMessageInfo(Submission target, MapMessage messageIn) throws JMSException {
        if (target == null || messageIn == null) {
            return;
        }
        target.setMessageId(messageIn.getStringProperty(MESSAGE_ID));
        target.setRefToMessageId(messageIn.getStringProperty(REF_TO_MESSAGE_ID));
    }

    private void populatePartyInfo(Submission target, MapMessage messageIn) throws JMSException {
        if (target == null || messageIn == null) {
            return;
        }
        setTargetFromPartyIdAndFromPartyType(messageIn, target);
        target.setFromRole(getPropertyWithFallback(messageIn, FROM_ROLE));

        setTargetToPartyIdAndToPartyType(messageIn, target);
        target.setToRole(getPropertyWithFallback(messageIn, TO_ROLE));
    }

    private void populateCollaborationInfo(Submission target, MapMessage messageIn) throws JMSException {
        if (target == null || messageIn == null) {
            return;
        }
        target.setAgreementRef(getPropertyWithFallback(messageIn, AGREEMENT_REF));
        target.setAgreementRefType(getPropertyWithFallback(messageIn, AGREEMENT_REF_TYPE));
        target.setAction(getAction(messageIn));
        target.setService(getService(messageIn));
        target.setServiceType(getPropertyWithFallback(messageIn, SERVICE_TYPE));
        target.setConversationId(messageIn.getStringProperty(CONVERSATION_ID));
    }

    private void populateMessageProperties(Submission target, MapMessage messageIn) throws JMSException {
        if (target == null || messageIn == null) {
            return;
        }
        //not part of ebMS3, eCODEX legacy property
        String strOriginalSender = messageIn.getStringProperty(PROPERTY_ORIGINAL_SENDER);
        if (isNotBlank(strOriginalSender)) {
            target.addMessageProperty(PROPERTY_ORIGINAL_SENDER, strOriginalSender);
        }
        String endpoint = messageIn.getStringProperty(PROPERTY_ENDPOINT);
        if (isNotEmpty(endpoint)) {
            target.addMessageProperty(PROPERTY_ENDPOINT, messageIn.getStringProperty(PROPERTY_ENDPOINT));
        }

        //not part of ebMS3, eCODEX legacy property
        String strFinalRecipient = messageIn.getStringProperty(PROPERTY_FINAL_RECIPIENT);
        String strFinalRecipientType = messageIn.getStringProperty(PROPERTY_FINAL_RECIPIENT_TYPE);
        LOG.debug("FinalRecipient [{}] and FinalRecipientType [{}] properties from Message", strFinalRecipient, strFinalRecipientType);
        if (isNotEmpty(strFinalRecipient)) {
            target.addMessageProperty(PROPERTY_FINAL_RECIPIENT, strFinalRecipient, strFinalRecipientType);
        }

        Enumeration<String> allProps = messageIn.getPropertyNames();
        while (allProps.hasMoreElements()) {
            String key = allProps.nextElement();
            if (key.startsWith(PROPERTY_PREFIX)) {
                target.addMessageProperty(key.substring(PROPERTY_PREFIX.length()), messageIn.getStringProperty(key), messageIn.getStringProperty(PROPERTY_TYPE_PREFIX + key.substring(PROPERTY_PREFIX.length())));
            }
        }
    }

    private void populatePayloadInfo(Submission target, MapMessage messageIn) throws JMSException {
        if (target == null || messageIn == null) {
            return;
        }
        LOG.debug("Submission message id [{}]", target.getMessageId());
        int numPayloads = 0;
        if (messageIn.propertyExists(TOTAL_NUMBER_OF_PAYLOADS)) {
            numPayloads = messageIn.getIntProperty(TOTAL_NUMBER_OF_PAYLOADS);
        }
        String bodyloadEnabled = getPropertyWithFallback(messageIn, JMSMessageConstants.P1_IN_BODY);
        for (int i = 1; i <= numPayloads; i++) {
            transformToSubmissionHandlePayload(messageIn, target, bodyloadEnabled, i);
        }
    }

    private String getPropertyWithFallback(final MapMessage messageIn, String propName) throws JMSException {
        String propValue = trim(messageIn.getStringProperty(propName));
        if (isEmpty(propValue)) {
            propValue = getProperty(propName);
        }

        return propValue;
    }

    private void setTargetToPartyIdAndToPartyType(MapMessage messageIn, Submission target) throws JMSException {
        String toPartyID = getPropertyWithFallback(messageIn, TO_PARTY_ID);
        String toPartyType = getPropertyWithFallback(messageIn, TO_PARTY_TYPE);
        LOG.debug("To Party Id  [{}] and Type [{}]", toPartyID, toPartyType);
        if (toPartyID != null) {
            target.addToParty(toPartyID, toPartyType);
        }
    }

    private void setTargetFromPartyIdAndFromPartyType(MapMessage messageIn, Submission target) throws JMSException {
        String fromPartyID = getPropertyWithFallback(messageIn, FROM_PARTY_ID);
        String fromPartyType = getPropertyWithFallback(messageIn, FROM_PARTY_TYPE);
        LOG.debug("From Party Id  [{}] and Type [{}]", fromPartyID, fromPartyType);
        if (fromPartyID != null) {
            target.addFromParty(fromPartyID, fromPartyType);
        }
    }

    private void transformToSubmissionHandlePayload(MapMessage messageIn, Submission target, String bodyloadEnabled, int i) throws JMSException {
        final String propPayload = MessageFormat.format(PAYLOAD_NAME_FORMAT, i);

        final String mimeType = getMimeType(messageIn, i);
        final String payFileNameProp = MessageFormat.format(PAYLOAD_FILE_NAME_FORMAT, i);
        String fileName = fileUtilExtService.sanitizeFileName(trim(messageIn.getStringProperty(payFileNameProp)));
        final String payloadNameProperty = MessageFormat.format(JMS_PAYLOAD_NAME_FORMAT, i);
        String payloadName = fileUtilExtService.sanitizeFileName(trim(messageIn.getStringProperty(payloadNameProperty)));
        final String payContID = MessageFormat.format(PAYLOAD_MIME_CONTENT_ID_FORMAT, i);
        final String contentId = trim(messageIn.getStringProperty(payContID));
        final Collection<Submission.TypedProperty> partProperties = new ArrayList<>();
        partProperties.add(new Submission.TypedProperty(MIME_TYPE, mimeType));
        if (fileName != null && !fileName.trim().equals("")) {
            partProperties.add(new Submission.TypedProperty(PAYLOAD_FILENAME, fileName));
        }
        if (StringUtils.isNotBlank(payloadName)) {
            partProperties.add(new Submission.TypedProperty(MessageConstants.PAYLOAD_PROPERTY_FILE_NAME, payloadName));
        }
        DataHandler payloadDataHandler;
        try {
            payloadDataHandler = new DataHandler(new ByteArrayDataSource(messageIn.getBytes(propPayload), mimeType));
        } catch (JMSException jmsEx) {
            LOG.debug("no payload data as byte[] available, trying payload via URL", jmsEx);
            try {
                payloadDataHandler = new DataHandler(new URLDataSource(new URL(messageIn.getString(propPayload))));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(propPayload + " neither available as byte[] or URL, aborting transformation");
            }
        }
        boolean inBody = (i == 1 && "true".equalsIgnoreCase(bodyloadEnabled));

        target.addPayload(contentId, payloadDataHandler, partProperties, inBody, null, null);
    }

    /**
     *
     * @return {@link MediaType#APPLICATION_OCTET_STREAM} if null or empty
     */
    protected String getMimeType(MapMessage messageIn, int index) throws JMSException {
        final String payMimeTypeProp = MessageFormat.format(JMSMessageConstants.PAYLOAD_MIME_TYPE_FORMAT, index);
        String mimeType = trim(messageIn.getStringProperty(payMimeTypeProp));
        if (StringUtils.isBlank(mimeType)) {
            LOG.debug("Use default mime type: [{}]", MediaType.APPLICATION_OCTET_STREAM);
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return mimeType;
    }

}
