package eu.domibus.ebsi.conf;

import eu.domibus.ebsi.enums.EBSITimestampMessageDirection;
import eu.domibus.ebsi.enums.EBSITimestampMessageTypes;
import eu.domibus.ebsi.enums.EBSITimestampType;
import eu.domibus.ext.services.DomibusPropertyExtService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static eu.domibus.ebsi.conf.EBSIExtensionPropertyManager.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(JMockit.class)
public class EBSIConfigurationTest {

    @Mocked
    DomibusPropertyExtService domibusPropertyExtService;

    @Tested
    EBSIConfiguration testInstance;

    @Before
    public void setup(){
        testInstance = new EBSIConfiguration(domibusPropertyExtService );
    }

    @Test
    public void testGetEbsiUrl() {
        // given
        String propertyValue = "http://"+UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_URL);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getEbsiUrl();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetEbsiDidName() {
        // given
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_DID_NAME);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getEbsiDidName();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetNotaryAddress() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_NOTARY_ADDRESS);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getNotaryAddress();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetApplicationName() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_APPLICATION_NAME);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getApplicationName();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetKeystoreLocation() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_KEYSTORE_LOCATION);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getKeystoreLocation();
        //then
        assertEquals(propertyValue, result);    }

    @Test
    public void testGetKeystoreType() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_KEYSTORE_TYPE);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getKeystoreType();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetKeystorePassword() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_KEYSTORE_PASSWORD);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getKeystorePassword();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetKeyAlias() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_KEY_ALIAS);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getKeyAlias();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetKeyPassword() {
        String propertyValue = UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_KEY_PASSWORD);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getKeyPassword();
        //then
        assertEquals(propertyValue, result);
    }

    @Test
    public void testGetTimestampAsynchronous() {
        String propertyValue = "true";
        new Expectations() {{
            domibusPropertyExtService.getProperty(DOMIBUS_EBSI_TIMESTAMP_ASYNCHRONOUS);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.getTimestampAsynchronous();
        //then
        assertTrue(result);
    }

    @Test
    public void testGetTimestampMessageTypes() {
        String propertyValue = "Signal_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        EBSITimestampMessageTypes result = testInstance.getTimestampMessageTypes();
        //then
        assertEquals(EBSITimestampMessageTypes.SIGNAL_MESSAGES, result);
    }

    @Test
    public void testIsTimestampForSignalMessagesEnabled() {
        String propertyValue = "Signal_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForSignalMessagesEnabled();
        //then
        assertTrue(result);
    }

    @Test
    public void testIsTimestampForSignalMessagesEnabledAll() {
        String propertyValue = "All_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForSignalMessagesEnabled();
        //then
        assertTrue(result);
    }

    @Test
    public void testIsTimestampForUserMessagesEnabled() {
        String propertyValue = "USER_MESSAGES";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForUserMessagesEnabled();
        //then
        assertTrue(result);
    }

    @Test
    public void testIsTimestampForUserMessagesEnabledAll() {
        String propertyValue = "ALL_MESSAGES";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForUserMessagesEnabled();
        //then
        assertTrue(result);
    }

    @Test
    public void testIsTimestampForMessageTypeEnabled() {
        String propertyValue = "Signal_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_TYPE);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForMessageTypeEnabled(true);
        //then
        assertFalse(result);
    }

    @Test
    public void testGetTimestampMessageDirection() {
        String propertyValue = "Disabled";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_DIRECTION);
            result = propertyValue;
        }};
        // when
        EBSITimestampMessageDirection result = testInstance.getTimestampMessageDirection();
        //then
        assertEquals(EBSITimestampMessageDirection.DISABLED, result);
    }

    @Test
    public void testGetTimestampMessageDirection2() {
        String propertyValue = "Received_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_DIRECTION);
            result = propertyValue;
        }};
        // when
        EBSITimestampMessageDirection result = testInstance.getTimestampMessageDirection();
        //then
        assertEquals(EBSITimestampMessageDirection.RECEIVED_MESSAGES, result);
    }

    @Test
    public void testIsTimestampForOutgoingMessagesEnabled() {
        String propertyValue = "Received_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_DIRECTION);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForOutgoingMessagesEnabled();
        //then
        assertFalse( result);
    }

    @Test
    public void testIsTimestampForIncomingMessagesEnabled() {
        String propertyValue = "Received_Messages";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_MESSAGE_DIRECTION);
            result = propertyValue;
        }};
        // when
        boolean result = testInstance.isTimestampForIncomingMessagesEnabled();
        //then
        assertTrue( result);
    }

    @Test
    public void testGetTimestampType() {
        String propertyValue = "EBSI";
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_TYPE);
            result = propertyValue;
        }};
        // when
        EBSITimestampType result = testInstance.getTimestampType();
        //then
        assertEquals(EBSITimestampType.EBSI, result);
    }

    @Test
    public void testGetTimestampTsaUrl() {
        // given
        String propertyValue = "http://"+UUID.randomUUID().toString();
        new Expectations() {{
            domibusPropertyExtService.getProperty(EBSI_TIMESTAMP_TSA_URL);
            result = propertyValue;
        }};
        // when
        String result = testInstance.getTimestampTsaUrl();
        //then
        assertEquals(propertyValue, result);
    }


}