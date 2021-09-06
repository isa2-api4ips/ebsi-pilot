package eu.domibus.ebsi.enums;

import org.apache.commons.lang3.StringUtils;

public enum EBSITimestampType {
    EBSI,
    TSA;

    public static EBSITimestampType valueOfIgnoreCase(String name) {
        return valueOf(StringUtils.upperCase(name));
    }
}
