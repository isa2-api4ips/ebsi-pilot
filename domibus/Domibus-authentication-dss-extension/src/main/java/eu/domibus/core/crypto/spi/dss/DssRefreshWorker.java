package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import eu.domibus.ext.services.CommandExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 *
 * This job will trigger a DSS refresh on the node executing the job and send a command to the other nodes
 * to perform as DSS refresh too.
 */
@DisallowConcurrentExecution
public class DssRefreshWorker extends DomibusQuartzJobExtBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssRefreshWorker.class);

    @Autowired
    private CommandExtService commandExtService;

    @Autowired
    private DssRefreshCommand dssRefreshCommand;

    @Override
    public void executeJob(JobExecutionContext context, DomainDTO domain) {
        LOG.info("Executing DSS refresh job at:[{}]", LocalDateTime.now());
        commandExtService.executeCommand(DssRefreshCommand.COMMAND_NAME, new HashMap<>());
        dssRefreshCommand.execute(new HashMap<>());
    }
}
