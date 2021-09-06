package eu.domibus.ebsi.enums;

import org.apache.commons.lang3.StringUtils;

public enum EBSITimestampMessageDirection {
    DISABLED,
    ALL_MESSAGES,
    SENT_MESSAGES,
    RECEIVED_MESSAGES;

    public static EBSITimestampMessageDirection valueOfIgnoreCase(String name) {
        return valueOf(StringUtils.upperCase(name));
    }
}
