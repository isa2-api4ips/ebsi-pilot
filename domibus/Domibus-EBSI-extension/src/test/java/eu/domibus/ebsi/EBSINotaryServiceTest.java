package eu.domibus.ebsi;

import eu.domibus.ebsi.conf.EBSIConfiguration;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class EBSINotaryServiceTest {

    @Mocked
    EBSIConfiguration configuration;


    @Tested
    EBSINotaryService testInstance;


    @Before
    public void setup() {
        testInstance = new EBSINotaryService(configuration);
    }

    @Test
    public void testGetEbsiRestValidationUrlForHashValue() {

       // given
        String hashId = UUID.randomUUID().toString();
        String ebsiULR = "http://" + UUID.randomUUID().toString();
        new Expectations(testInstance) {{
            configuration.getEbsiUrl();
            result = ebsiULR;
        }};
        // when
        String result  = testInstance.getEbsiRestValidationUrlForHashValue(hashId);
        // then
        assertEquals(ebsiULR + "/timestamp/v1/hashes/" +hashId, result);
    }
}