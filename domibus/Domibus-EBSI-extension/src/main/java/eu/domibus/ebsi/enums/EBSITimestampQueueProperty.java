package eu.domibus.ebsi.enums;

import org.apache.commons.lang3.StringUtils;

public enum EBSITimestampQueueProperty {
    EBMS_MESSAGE_ID("ebms.messageid"),
    TIMESTAMP_VALUE("timestamp.hex.value");

    String propertyName;
    EBSITimestampQueueProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
