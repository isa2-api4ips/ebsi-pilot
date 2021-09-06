package eu.domibus.ebsi.conf;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.TomcatCondition;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Class responsible for the configuration of the plugin for Tomcat
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(TomcatCondition.class)
@Configuration
public class EBSITomcatConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSITomcatConfiguration.class);

    @Bean("extEBSITimestampQueue")
    public ActiveMQQueue extEBSITimestampQueue() {
        String queueName ="domibus.external.ebsi.timestamp.queue";
        LOG.debug("Using ebsi extension timestamp queue name [{}]", queueName);
        return new ActiveMQQueue(queueName);
    }
}
