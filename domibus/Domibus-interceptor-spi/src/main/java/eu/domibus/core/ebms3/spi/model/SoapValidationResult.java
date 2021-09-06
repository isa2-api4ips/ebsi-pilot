package eu.domibus.core.ebms3.spi.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SoapValidationResult {
    Map<String, Object> validationData = new HashMap<>();
    int validationResult = 0;

    public Map<String, Object> getValidationData() {
        return validationData;
    }

    public void setValidationData(Map<String, Object> validationData) {
        this.validationData = validationData;
    }

    public int getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(int validationResult) {
        this.validationResult = validationResult;
    }

    public SoapValidationResult addString(String key, String value){
        validationData.put(key, value);
        return this;
    }
    public SoapValidationResult addDate(String key, Date value){
        validationData.put(key, value);
        return this;
    }
    public SoapValidationResult addLocalDateTime(String key, LocalDateTime value){
        validationData.put(key, value);
        return this;
    }

    public SoapValidationResult addInteger(String key, Integer value){
        validationData.put(key, value);
        return this;
    }
    public SoapValidationResult addObject(String key, Object value){
        validationData.put(key, value);
        return this;
    }

    public String getStringValue(String key){
        return (String)validationData.get(key);
    }
    public LocalDateTime getLocalDateTimeValue(String key){
        return safeCast(validationData.get(key), LocalDateTime.class);
    }
    public Integer getIntegerValue(String key){
        return safeCast(validationData.get(key), Integer.class);
    }


    public static <T> T safeCast(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

}
