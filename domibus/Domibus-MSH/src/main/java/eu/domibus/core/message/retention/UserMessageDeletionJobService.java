package eu.domibus.core.message.retention;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class UserMessageDeletionJobService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDeletionJobService.class);

    protected UserMessageDeletionJobDao userMessageDeletionJobDao;

    protected UserMessageLogDao userMessageLogDao;

    protected MetricRegistry metricRegistry;

    public UserMessageDeletionJobService(UserMessageDeletionJobDao userMessageDeletionJobDao, UserMessageLogDao userMessageLogDao, MetricRegistry metricRegistry) {
        this.userMessageDeletionJobDao = userMessageDeletionJobDao;
        this.userMessageLogDao = userMessageLogDao;
        this.metricRegistry = metricRegistry;
    }

    public void executeJob(UserMessageDeletionJobEntity deletionJob) {
        com.codahale.metrics.Timer.Context deletionJobTimer = metricRegistry.timer("executeDeletionJob_" + deletionJob.getProcedureName() + "_" + deletionJob.getJobNumber()).time();
        setJobAsRunning(deletionJob);
        userMessageLogDao.deleteExpiredMessages(deletionJob.getStartRetentionDate(), deletionJob.getEndRetentionDate(), deletionJob.getMpc(), deletionJob.getMaxCount(), deletionJob.getProcedureName());
        setJobAsStopped(deletionJob);
        deletionJobTimer.stop();
    }

    public void setJobAsStopped(UserMessageDeletionJobEntity deletionJob) {
        deletionJob.setState(UserMessageDeletionJobState.STOPPED.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Stopped deletion job [{}]", deletionJob);
    }

    public void setJobAsRunning(UserMessageDeletionJobEntity deletionJob) {
        deletionJob.setActualStartDate(new Date(System.currentTimeMillis()));
        deletionJob.setState(UserMessageDeletionJobState.RUNNING.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Started deletion job [{}]", deletionJob);
    }

    public List<UserMessageDeletionJobEntity> findCurrentDeletionJobs() {
        return userMessageDeletionJobDao.findCurrentDeletionJobs();
    }

    public void deleteJob(UserMessageDeletionJobEntity deletionJob) {
        LOG.trace("Deletion job removed from database [{}]", deletionJob);
        userMessageDeletionJobDao.delete(deletionJob);
    }

    public void createJob(UserMessageDeletionJobEntity deletionJob) {
        LOG.trace("Deletion job created in the database [{}]", deletionJob);
        userMessageDeletionJobDao.create(deletionJob);
    }

    public boolean doJobsOverlap(UserMessageDeletionJobEntity currentDeletionJob, UserMessageDeletionJobEntity newDeletionJob) {
        if (!currentDeletionJob.equals(newDeletionJob)) {
            LOG.trace("Jobs are different, do not overlap");
            return false;
        }
        if (newDeletionJob.getEndRetentionDate().before(currentDeletionJob.getStartRetentionDate())) {
            LOG.trace("Jobs do not overlap, new job interval is before current job.");
            return false;
        }
        if (newDeletionJob.getStartRetentionDate().after(currentDeletionJob.getEndRetentionDate())) {
            LOG.trace("Jobs do not overlap, new job interval is after current job.");
            return false;
        }
        LOG.trace("Jobs overlap.");
        return true;
    }


    public boolean isJobOverlaping(UserMessageDeletionJobEntity deletionJob, List<UserMessageDeletionJobEntity> currentDeletionJobs) {
        LOG.trace("Verify if deletion job overlaps current deletion jobs.");
        if (CollectionUtils.isEmpty(currentDeletionJobs)) {
            LOG.trace("No overlapping, there are no current deletion jobs.");
            return false;
        }

        List<UserMessageDeletionJobEntity> result = currentDeletionJobs.stream().filter(currentDeletionJob -> doJobsOverlap(currentDeletionJob, deletionJob))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            LOG.trace("Deletion job does not overlap with current deletion jobs.");
            return false;
        }

        LOG.debug("Deletion job overlaps the following jobs [{}]", result);
        return true;
    }
}
