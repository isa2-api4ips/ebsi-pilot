package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Joze RIHTARSIC
 * @since 4.2.1 - EBSI Pilot example
 *
 * Entity for retrieving message timestamp data. Class is part of EBSI pilot project
 * and is not part of domibus standard deployment!
 * */
public class MessageTimestampInfoRO implements Serializable {

    private String messageId;
    private Date timestamp;
    private String notarizedHashValue;
    private String signatureHashValue;
    private String registeredBy;
    private String transactionHash;
    private String validationUrl;
    private String isValidSignatureHash;
    private Integer blockNumber;


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotarizedHashValue() {
        return notarizedHashValue;
    }

    public void setNotarizedHashValue(String notarizedHashValue) {
        this.notarizedHashValue = notarizedHashValue;
    }

    public String getSignatureHashValue() {
        return signatureHashValue;
    }

    public void setSignatureHashValue(String signatureHashValue) {
        this.signatureHashValue = signatureHashValue;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getIsValidSignatureHash() {
        return isValidSignatureHash;
    }

    public void setIsValidSignatureHash(String isValidSignatureHash) {
        this.isValidSignatureHash = isValidSignatureHash;
    }
}
