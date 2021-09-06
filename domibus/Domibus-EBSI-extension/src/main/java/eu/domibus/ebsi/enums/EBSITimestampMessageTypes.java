package eu.domibus.ebsi.enums;

import org.apache.commons.lang3.StringUtils;

public enum EBSITimestampMessageTypes {
    ALL_MESSAGES,
    SIGNAL_MESSAGES,
    USER_MESSAGES;

    public static EBSITimestampMessageTypes valueOfIgnoreCase(String name) {
        return valueOf(StringUtils.upperCase(name));
    }
}
