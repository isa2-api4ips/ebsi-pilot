package eu.domibus.ebsi.conf;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 */
@Conditional(ApplicationServerCondition.class)
@Configuration
public class EBSIApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSIApplicationServerConfiguration.class);

    @Bean("extEBSITimestampQueue")
    public JndiObjectFactoryBean sendMessageQueue() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String ebsiTimestampQueueJndi = "jms/domibus.external.ebsi.timestamp.queue";
        LOG.debug("Using send queue jndi [{}]", ebsiTimestampQueueJndi);
        jndiObjectFactoryBean.setJndiName(ebsiTimestampQueueJndi);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }
}
