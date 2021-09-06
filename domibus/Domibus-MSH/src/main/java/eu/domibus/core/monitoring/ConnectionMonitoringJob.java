package eu.domibus.core.monitoring;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Ion Perpegel
 * @since 4.2
 */
@DisallowConcurrentExecution
public class ConnectionMonitoringJob extends DomibusQuartzJobBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringJob.class);

    @Autowired
    protected ConnectionMonitoringService connectionMonitoringService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        if (!connectionMonitoringService.isMonitoringEnabled()) {
            return;
        }

        LOG.debug("ConnectionMonitoringJob started on [{}] domain", domain);
        authUtils.runWithDomibusSecurityContext(connectionMonitoringService::sendTestMessages, AuthRole.ROLE_ADMIN);
        LOG.debug("ConnectionMonitoringJob ended on [{}] domain", domain);
    }
}
