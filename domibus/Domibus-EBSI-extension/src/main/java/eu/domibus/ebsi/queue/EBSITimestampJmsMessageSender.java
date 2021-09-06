package eu.domibus.ebsi.queue;


import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.jms.JMSException;
import javax.jms.Queue;
import static eu.domibus.ebsi.enums.EBSITimestampQueueProperty.EBMS_MESSAGE_ID;
import static eu.domibus.ebsi.enums.EBSITimestampQueueProperty.TIMESTAMP_VALUE;


@Service
public class EBSITimestampJmsMessageSender {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSITimestampJmsMessageSender.class);

    protected JMSExtService jmsExtService;

    protected Queue ebsiTimestampQueue;

    public EBSITimestampJmsMessageSender(JMSExtService jmsExtService, @Qualifier("extEBSITimestampQueue") Queue ebsiTimestampQueue) {
        this.jmsExtService = jmsExtService;
        this.ebsiTimestampQueue = ebsiTimestampQueue;
    }

    public void addMessageToTimestamp(String hexHashValue, String messageID) {
        String queueName;
        try {
            queueName = ebsiTimestampQueue.getQueueName();
        } catch (JMSException e) {
            LOG.error("Message not send", e);
            return;
        }
        final JmsMessageDTO jmsMessage = JMSMessageDTOBuilder.
                create().
                property(EBMS_MESSAGE_ID.getPropertyName(),messageID).
                property(TIMESTAMP_VALUE.getPropertyName(),hexHashValue).
                build();

        LOG.debug("send message: [{}] to ebsi timestamp for file: [{}], queue name: [{}]", jmsMessage, hexHashValue, queueName );
        jmsExtService.sendMessageToQueue(jmsMessage,queueName);
    }

}