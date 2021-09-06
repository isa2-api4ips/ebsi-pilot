package eu.europa.ec.edelivery.ebsi.timestamp;

import eu.europa.ec.edelivery.ebsi.exceptions.TimestampException;
import eu.europa.ec.edelivery.ebsi.timestamp.entities.TimestampData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class TimeStampApiTest {

    TimeStampApi timeStampApi = new TimeStampApi();

    @Test
    void testGetTimestampData() throws TimestampException {
        // given
        InputStream jsonStream =  TimeStampApiTest.class.getResourceAsStream("/examples/json/timestamp-01.json");
        // when
        TimestampData timestampData = timeStampApi.getTimestampData(jsonStream);
        // then
        assertNotNull(timestampData);
        assertEquals("8ed68c06a76f8c81b19eb193251a18c67c3d2c76bd592bb05ab5b87cbb706782", timestampData.getHash());
        assertEquals("0x2cf81263cc679c9132d3375cefd82d4f72c183e5", timestampData.getRegisteredBy());
        assertEquals("2021-04-08T05:41:49.000Z", timestampData.getTimestamp());
        assertEquals("0x971b8cc13ccee77b4be16aa060987cbd1e2d592544f696015e894c1bf7a3470c", timestampData.getTxHash());
        assertEquals(20645996, timestampData.getBlockNumber());
    }



}