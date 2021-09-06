package eu.domibus.ebsi.conf;

import eu.domibus.common.JMSConstants;
import eu.domibus.ebsi.queue.EBSITimestampMessageListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.Queue;
import java.util.Optional;

@Configuration
public class EBSIMessageListenerContainerConfiguration {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSIMessageListenerContainerConfiguration.class);


    @Autowired
    @Qualifier("extEBSITimestampQueue")
    private Queue extEBSITimestampQueue;


    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private EBSITimestampMessageListener timestampMessageListener;

    @Autowired
    @Qualifier(JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;


    @Autowired
    Optional<JndiDestinationResolver> internalDestinationResolver;

    @Bean(name = "ebsiPluginTimestampContainer")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public MessageListenerContainer createDefaultMessageListenerContainer() {
        final String queueConcurrency = "1";
        LOG.info("EBSI timestamp Queue {} concurrency set to: {} and listener {}",extEBSITimestampQueue,  queueConcurrency, timestampMessageListener);
        return  createDefaultMessageListenerContainer(connectionFactory, extEBSITimestampQueue, timestampMessageListener, transactionManager, true);

    }


    private DefaultMessageListenerContainer createDefaultMessageListenerContainer( ConnectionFactory connectionFactory, Queue destination,
                                                                                  MessageListener messageListener, PlatformTransactionManager transactionManager,
                                                                                  boolean useInternalDestinationResolver) {

        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();


        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestination(destination);
        messageListenerContainer.setMessageListener(messageListener);
        messageListenerContainer.setTransactionManager(transactionManager);

        final String concurrency = "1";;
        if (StringUtils.isEmpty(concurrency)) {
            LOG.error("Concurrency value not defined for property [" + concurrency + "]");
        }
        messageListenerContainer.setConcurrency(concurrency);

        messageListenerContainer.setSessionTransacted(true);
        messageListenerContainer.setSessionAcknowledgeMode(0);


        messageListenerContainer.afterPropertiesSet();

        if (useInternalDestinationResolver && internalDestinationResolver.isPresent()) {
            messageListenerContainer.setDestinationResolver(internalDestinationResolver.get());
        }



        LOG.debug("DefaultMessageListenerContainer initialized for destination [{}] with concurrency=[{}] and message listener [{}]", destination, concurrency, messageListener);
        return messageListenerContainer;
    }

}
