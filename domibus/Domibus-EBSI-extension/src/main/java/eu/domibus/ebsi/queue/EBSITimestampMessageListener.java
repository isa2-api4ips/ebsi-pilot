package eu.domibus.ebsi.queue;

import eu.domibus.ebsi.EBSINotaryService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.edelivery.ebsi.exceptions.RemoteCallException;
import eu.europa.ec.edelivery.ebsi.exceptions.SessionException;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static eu.domibus.ebsi.enums.EBSITimestampQueueProperty.EBMS_MESSAGE_ID;
import static eu.domibus.ebsi.enums.EBSITimestampQueueProperty.TIMESTAMP_VALUE;


@Service("ebsiTimestampMessageListener")
public class EBSITimestampMessageListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSITimestampMessageListener.class);

    EBSINotaryService ebsiNotaryService;

    public EBSITimestampMessageListener(EBSINotaryService ebsiNotaryService) {
        this.ebsiNotaryService = ebsiNotaryService;
    }

    @Override
    public void onMessage(Message message) {
        LOG.info("Got message to consume:" + message);

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String hexHashValue = textMessage.getStringProperty(TIMESTAMP_VALUE.getPropertyName());
                String messageId = textMessage.getStringProperty(EBMS_MESSAGE_ID.getPropertyName());

                LOG.info("Timestamp value: [{}] for message [{}]", hexHashValue, messageId);
                byte[] bytesValue = Numeric.hexStringToByteArray(hexHashValue);
                ebsiNotaryService.ebsiTimeStampSignature(bytesValue);
                LOG.info("Value: [{}] for message [{}] is timestamped", hexHashValue, messageId);
            } catch (JMSException ex) {
                throw new RuntimeException(ex);
            } catch (RemoteCallException | IOException | UnrecoverableKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException | SessionException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new IllegalArgumentException("Message Error");
        }
    }
}
