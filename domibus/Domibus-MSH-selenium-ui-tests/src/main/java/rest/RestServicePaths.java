package rest;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class RestServicePaths {
	
	public static final String LOGIN = "/rest/security/authentication";
	public static final String CONNECTED = "/rest/security/user/connected";
	public static final String PMODE = "/rest/pmode";
	public static final String TRUSTSTORE = "/rest/truststore/save";
	public static final String PMODE_LIST = "/rest/pmode/list";
	public static final String PMODE_PROCESS_LIST = "/rest/party/processes";
	public static final String PMODE_RESTORE = "/rest/pmode/restore/";
	public static final String USERS = "/rest/user/users";
	public static final String PLUGIN_USERS = "/rest/plugin/users";
	public static final String DOMAINS = "/rest/application/domains";
	public static final String IS_MULTITENANCY = "/rest/application/multitenancy";
	public static final String MESSAGE_FILTERS = "/rest/messagefilters";
	public static final String SESSION_DOMAIN = "/rest/security/user/domain";
	
	public static final String JMS_SEARCH = "/rest/jms/messages";
	public static final String JMS_QUEUES = "/rest/jms/destinations";
	public static final String JMS_ACTION = "/rest/jms/messages/action";
	
	public static final String UPDATE_PARTIES = "/rest/party/update";
	public static final String GET_PARTIES = "/rest/party/list";
	
	
	public static final String CON_MON_PARTIES = "/rest/testservice/parties";
	public static final String CON_MON_PARTIES_DETAILS = "/rest/testservice/connectionmonitor";
	
	public static final String MESSAGE_LOG_CSV = "/rest/messagelog/csv";
	public static final String MESSAGE_LOG_MESSAGE = "/rest/message/download";
	public static final String MESSAGE_LOG_MESSAGES = "rest/messagelog";
	public static final String MESSAGE_LOG_RESEND = "/rest/message/restore";
	
	public static final String MESSAGE_FILTERS_CSV = "/rest/messagefilters/csv";
	public static final String ERROR_LOG_CSV = "/rest/errorlogs/csv";
	public static final String PMODE_CURRENT_DOWNLOAD = "/rest/pmode/";
	public static final String PMODE_ARCHIVE_CSV = "/rest/pmode/csv";
	public static final String PMODE_PARTIES_CSV = "/rest/party/csv";
	public static final String JMS_MESSAGES_CSV = "/rest/jms/csv";
	public static final String TRUSTSTORE_CSV = "/rest/truststore/csv";
	public static final String USERS_CSV = "/rest/user/csv";
	public static final String PLUGIN_USERS_CSV = "/rest/plugin/csv";
	public static final String AUDIT_CSV = "/rest/audit/csv";
	public static final String ALERTS_CSV = "/rest/alerts/csv";
	public static final String DOMIBUS_PROPERTIES = "rest/configuration/properties";
	
	public static final String ERRORS = "/rest/errorlogs";
	public static final String AUDIT_COUNT = "rest/audit/count";
	public static final String AUDIT_LIST = "rest/audit/list";
	
	public static final String ALERTS_LIST = "/rest/alerts";
	
	public static final String LOGGING = "/rest/logging/loglevel";
	public static final String LOGGING_RESET = "rest/logging/reset";
	
	
	public static final String UI_REPLICATION_COUNT = "/rest/uireplication/count";
	public static final String UI_REPLICATION_SYNC = "/rest/uireplication/sync";
	
	public static final String PASSWORD = "/rest/security/user/password";
	
	
}
