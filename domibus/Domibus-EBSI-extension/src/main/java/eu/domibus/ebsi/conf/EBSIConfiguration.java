package eu.domibus.ebsi.conf;

import eu.domibus.ebsi.enums.EBSITimestampMessageDirection;
import eu.domibus.ebsi.enums.EBSITimestampMessageTypes;
import eu.domibus.ebsi.enums.EBSITimestampType;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


import static eu.domibus.ebsi.conf.EBSIExtensionPropertyManager.*;


@Configuration
@PropertySource(value = "classpath:ebsi-extension-default.properties")
@PropertySource(ignoreResourceNotFound = true, value = "file:${domibus.config.location}/extensions/config/ebsi-extension.properties")
public class EBSIConfiguration {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EBSIConfiguration.class);

    public static final String DID_CACHE = "did-document-cache";

    private DomibusPropertyExtService domibusPropertyExtService;

    public EBSIConfiguration(DomibusPropertyExtService domibusPropertyExtService){
        this.domibusPropertyExtService = domibusPropertyExtService;
    }

    public String getEbsiUrl() {
        return domibusPropertyExtService.getProperty(EBSI_URL);
    }

    public String getEbsiDidName() {
        return domibusPropertyExtService.getProperty(EBSI_DID_NAME);
    }
    public String getNotaryAddress() {
        return domibusPropertyExtService.getProperty(EBSI_NOTARY_ADDRESS);
    }

    public String getApplicationName() {
        return domibusPropertyExtService.getProperty(EBSI_APPLICATION_NAME);
    }

    public String getKeystoreLocation() {
        return domibusPropertyExtService.getProperty(EBSI_KEYSTORE_LOCATION);
    }
    public String getKeystoreType() {
        return domibusPropertyExtService.getProperty(EBSI_KEYSTORE_TYPE);
    }

    public String getKeystorePassword() {
        return domibusPropertyExtService.getProperty(EBSI_KEYSTORE_PASSWORD);
    }

    public String getKeyAlias() {
        return domibusPropertyExtService.getProperty(EBSI_KEY_ALIAS);
    }
    public String getKeyPassword() {
        return domibusPropertyExtService.getProperty(EBSI_KEY_PASSWORD);
    }

    public boolean getTimestampAsynchronous() {
        return BooleanUtils.toBoolean(domibusPropertyExtService.getProperty(DOMIBUS_EBSI_TIMESTAMP_ASYNCHRONOUS));
    }

    public EBSITimestampMessageTypes getTimestampMessageTypes() {
        String value = domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
        try {
            return EBSITimestampMessageTypes.valueOfIgnoreCase(value);
        } catch (RuntimeException exception) {
            LOG.error("Configuration error: can not parse property [{}] with value [{}]. All_Messages will be used instead!",
                    EBSI_TIMESTAMP_MESSAGE_TYPE, value);
            return EBSITimestampMessageTypes.ALL_MESSAGES;
        }
    }

    public boolean isTimestampForUserMessagesEnabled() {
        EBSITimestampMessageTypes timestampMessageTypes = getTimestampMessageTypes();

        return timestampMessageTypes.equals(EBSITimestampMessageTypes.ALL_MESSAGES)
                || timestampMessageTypes.equals(EBSITimestampMessageTypes.USER_MESSAGES);
    }
    public boolean isTimestampForSignalMessagesEnabled() {
        EBSITimestampMessageTypes timestampMessageTypes = getTimestampMessageTypes();

        return timestampMessageTypes.equals(EBSITimestampMessageTypes.ALL_MESSAGES)
                || timestampMessageTypes.equals(EBSITimestampMessageTypes.SIGNAL_MESSAGES);
    }

    public boolean isTimestampForMessageTypeEnabled(boolean isUserMessage) {
        return isUserMessage && isTimestampForUserMessagesEnabled() ||
                !isUserMessage && isTimestampForSignalMessagesEnabled();

    }


    public EBSITimestampMessageDirection getTimestampMessageDirection() {
        String value = domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_DIRECTION);
        try {
            return EBSITimestampMessageDirection.valueOfIgnoreCase(value);
        } catch (RuntimeException exception) {
            LOG.error("Configuration error: can not parse property [{}] with value [{}]. All_Messages will be used instead!",
                    EBSI_TIMESTAMP_MESSAGE_DIRECTION, value);
            return EBSITimestampMessageDirection.ALL_MESSAGES;
        }
    }

    public boolean isTimestampForOutgoingMessagesEnabled() {
        EBSITimestampMessageDirection direction = getTimestampMessageDirection();
        return EBSITimestampMessageDirection.ALL_MESSAGES.equals(direction) || EBSITimestampMessageDirection.SENT_MESSAGES.equals(direction);
    }

    public boolean isTimestampForIncomingMessagesEnabled() {
        EBSITimestampMessageDirection direction = getTimestampMessageDirection();
        return EBSITimestampMessageDirection.ALL_MESSAGES.equals(direction) || EBSITimestampMessageDirection.RECEIVED_MESSAGES.equals(direction);
    }

    public EBSITimestampType getTimestampType() {
        String value = domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_TYPE);
        try {
            return EBSITimestampType.valueOfIgnoreCase(value);
        } catch (RuntimeException exception) {
            LOG.error("Configuration error: can not parse property [{}] with value [{}]. All_Messages will be used instead!",
                    EBSI_TIMESTAMP_TYPE, value);
            return EBSITimestampType.EBSI;
        }
    }
    public String getTimestampTsaUrl() {
        return domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_TSA_URL);
    }

}
