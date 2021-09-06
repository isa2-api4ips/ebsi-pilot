package eu.domibus.ebsi.queue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class EBSIMessageListenerContainerInitializer {

    MessageListenerContainer timestampJMSContainer;

    public EBSIMessageListenerContainerInitializer(@Qualifier("ebsiPluginTimestampContainer") MessageListenerContainer timestampJMSContainer) {
        this.timestampJMSContainer = timestampJMSContainer;
        this.timestampJMSContainer.start();
    }

}
