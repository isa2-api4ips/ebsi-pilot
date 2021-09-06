package eu.domibus.core.audit;

import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLog;
import eu.domibus.core.audit.envers.RevisionLogicalName;
import eu.domibus.core.spring.SpringContextProvider;
import eu.domibus.core.util.AnnotationsUtil;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Custom listener that allows us to add custom information to the hibernate envers schema.
 */
public class CustomRevisionEntityListener implements EntityTrackingRevisionListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomRevisionEntityListener.class);

    private AnnotationsUtil annotationsUtil;

    public CustomRevisionEntityListener() {
        this.annotationsUtil = new AnnotationsUtil();
    }

    /**
     * Call when an new revision is created.
     * New revision are create one per transaction for every audited entity change.
     *
     * @param revision the new revision.
     */
    @Override
    public void newRevision(final Object revision) {
        RevisionLog revisionLog = (RevisionLog) revision;
        revisionLog.setRevisionDate(new Date(System.currentTimeMillis()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            revisionLog.setUserName(authentication.getName());
            return;
        }
        String databaseUserName = getDataBaseUser();
        LOG.trace("No authentication in application security context. Set DataBase username: [{}] to audit log.", databaseUserName);
        revisionLog.setUserName(databaseUserName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void entityChanged(final Class entityClass,
                              final String entityName,
                              final Serializable entityId,
                              final RevisionType revisionType, final
                              Object revisionEntity) {
        Optional<String> logicalName = annotationsUtil.getValue(entityClass, RevisionLogicalName.class);
        Optional<Integer> auditOrder = annotationsUtil.getValue(entityClass, RevisionLogicalName.class, "auditOrder", Integer.class);
        ((RevisionLog) revisionEntity).addEntityAudit(entityId.toString(),
                entityName,
                logicalName.orElse(entityName),
                getModificationType(revisionType),
                auditOrder.orElse(10)
        );
    }

    /**
     * Does a mapping between envers RevisionType and our own ModificationType enum.
     *
     * @param revisionType the envers enum.
     * @return our modification enum..
     */
    ModificationType getModificationType(final RevisionType revisionType) {
        switch (revisionType) {
            case ADD:
                return ModificationType.ADD;
            case DEL:
                return ModificationType.DEL;
            case MOD:
                return ModificationType.MOD;
            default:
                String msg = "RevisionType " + revisionType + " is unknown";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Retrieve database user from Spring application context!
     *
     * @return database username.
     */
    protected String getDataBaseUser() {
        if (SpringContextProvider.getApplicationContext() == null) {
            return null;
        }

        final DatabaseUtil databaseUtil = SpringContextProvider.getApplicationContext().getBean(DatabaseUtil.DATABASE_USER, DatabaseUtil.class);
        return databaseUtil != null ? databaseUtil.getDatabaseUserName() : null;
    }


}
