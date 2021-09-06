package eu.domibus.test.jms

import eu.domibus.test.utils.DomibusSoapUIConstants
import eu.domibus.test.utils.LogUtils
import eu.domibus.test.utils.SoapUIPropertyUtils
import org.apache.activemq.ActiveMQConnectionFactory

import javax.jms.*
import javax.naming.Context
import javax.naming.InitialContext

class DomibusJMSPlugin {

    static def defaultPluginAdminC2Default = "pluginAdminC2Default"
    static def defaultAdminDefaultPassword = "adminDefaultPassword"

    def allJMSProperties;
    def log;
    def context;

    //START: JMS communication - specific properties
    def jmsSender = null
    def jmsConnectionHandler = null
    // END: JMS communication - specific properties

    /**
     * Constructor set the logger and groovy context.
     * From the groovy the parameter  allJMSDomainsProperties is parset
     *
     * @param log - the logger
     * @param context - the soapui parameter with allJMSDomainsProperties
     */
    DomibusJMSPlugin(log, context) {
        this.log = log
        this.context = context
        this.allJMSProperties = SoapUIPropertyUtils.parseJMSDomainProperties(
                context.expand('${#Project#' + DomibusSoapUIConstants.PROP_GLOBAL_JMS_ALL_PROPERTIES + '}'), this.log)
    }

    // Class destructor
    void finalize() {
        log.debug "Domibus class not needed longer."
    }

//---------------------------------------------------------------------------------------------------------------------------------
// This methods support JMS project
//---------------------------------------------------------------------------------------------------------------------------------
    static void addPluginCredentialsIfNeeded(context, log, messageMap, String propPluginUsername = defaultPluginAdminC2Default, String propPluginPassword = defaultAdminDefaultPassword) {
        LogUtils.debugLog("  ====  Calling \"addPluginCredentialsIfNeeded\".", log)
        def unsecureLoginAllowed = context.expand("\${#Project#unsecureLoginAllowed}").toLowerCase()
        if (unsecureLoginAllowed == "false" || unsecureLoginAllowed == 0) {
            LogUtils.debugLog("  addPluginCredentialsIfNeeded  [][]  passed values are propPluginUsername=${propPluginUsername} propPluginPasswor=${propPluginPassword} ", log)
            def u = context.expand("\${#Project#${propPluginUsername}}")
            def p = context.expand("\${#Project#${propPluginPassword}}")
            LogUtils.debugLog("  addPluginCredentialsIfNeeded  [][]  Username|Password=" + u + "|" + p, log)
            messageMap.setStringProperty("username", u)
            messageMap.setStringProperty("password", p)
        }
    }

    static InitialContext getInitialContext(String providerUrl, String userName, String password, String initialContextFactory) throws Exception {
        InitialContext ic;
        if (providerUrl != null) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            if (userName != null) {
                env.put(Context.SECURITY_PRINCIPAL, userName);
            }
            if (password != null) {
                env.put(Context.SECURITY_CREDENTIALS, password);
            }
            ic = new InitialContext(env);
        } else {
            ic = new InitialContext();
        }
        return ic;
    }

    def connectUsingJMSApi(String PROVIDER_URL, String USER, String PASSWORD, String CONNECTION_FACTORY_JNDI, String QUEUE, String initialContextFactory) {

        MapMessage messageMap = null
        try {
            jmsConnectionHandler = getInitialContext(PROVIDER_URL, USER, PASSWORD, initialContextFactory);

            QueueConnectionFactory cf = (QueueConnectionFactory)jmsConnectionHandler.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection(USER, PASSWORD);
            QueueSession session = (QueueSession)qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = (Queue)jmsConnectionHandler.lookup(QUEUE);
            jmsSender = session.createSender(queue);

            messageMap = session.createMapMessage();
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in Weblogic deployment failed. " +
                    "PROVIDER_URL: $PROVIDER_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "CONNECTION_FACTORY_JNDI: $CONNECTION_FACTORY_JNDI | QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        return messageMap
    }

    def connectToActiveMQ(String FACTORY_URL, String USER, String PASSWORD, String QUEUE) {
        MapMessage messageMap = null
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(FACTORY_URL)
            jmsConnectionHandler = (Connection) connectionFactory.createConnection(USER, PASSWORD)
            //username and password of the default JMS broker
            QueueSession session = jmsConnectionHandler.createSession(false, Session.AUTO_ACKNOWLEDGE)
            Destination destination = session.createQueue(QUEUE)
            jmsSender = (MessageProducer) session.createProducer(destination)
            jmsSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
            messageMap = session.createMapMessage()
        } catch (Exception ex) {
            log.error "jmsConnectionHandlerInitialize    [][]  Connection to JMS queue in Tomcat deployment failed. " +
                    "FACTORY_URL: $FACTORY_URL | USER: $USER | PASSWORD: $PASSWORD | " +
                    "QUEUE: $QUEUE"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        return messageMap
    }

    def jmsConnectionHandlerInitializeC2() {
        jmsConnectionHandlerInitialize("C2Default")
    }

    def jmsConnectionHandlerInitializeC3() {
        jmsConnectionHandlerInitialize("C3Default")
    }


    def getJMSDomainProperty(String domain, String property){
        return SoapUIPropertyUtils.getDomainProperty(this.context, this.allJMSProperties, domain, property, this.log);
    }

    def jmsConnectionHandlerInitialize(String domain) {
        MapMessage messageMap = null

        log.info "Starting JMS message sending"
        String jmsClientType = getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_TYPE);
        String jmsURL = getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_URL);
        String serverUser =  getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_SRV_USERNAME);
        String serverPassword =  getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_SRV_PASSWORD);
        String jmsConnectionFactory =  getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_CF_JNDI);
        String queue =  getJMSDomainProperty(domain,DomibusSoapUIConstants.JSON_JMS_QUEUE);

        switch (jmsClientType) {
            case "weblogic":
                messageMap = connectUsingJMSApi(jmsURL, serverUser, serverPassword, jmsConnectionFactory, queue, "weblogic.jndi.WLInitialContextFactory")
                break
            case "tomcat":
                log.info("JmsServer Tomcat. Reading connection details.")
                messageMap = connectToActiveMQ(jmsURL, serverUser, serverPassword, queue)
                break
            case "wildfly":
                log.info("JmsServer Tomcat. Reading connection details.")
                messageMap = connectUsingJMSApi(jmsURL, serverUser, serverPassword, jmsConnectionFactory, queue, "org.jboss.naming.remote.client.InitialContextFactory")
                break

            default:
                log.error("Incorrect or not supported jms server type, jmsServer=" + jmsClientType);
                assert 0, "Properties value error, check jmsServer value."
                break
        }
        return messageMap
    }

    def sendMessageAndClean(messageMap) {

        log.info "sending message"
        try {
            jmsSender.send(messageMap);
            jmsConnectionHandler.close();
        } catch (Exception ex) {
            log.error "sendMessageAndClean    [][]  Sending and closing connection  to JMS queue"
            assert 0, "Exception occurred when trying to connect: " + ex;
        }
        log.info "message sent"
    }

}

