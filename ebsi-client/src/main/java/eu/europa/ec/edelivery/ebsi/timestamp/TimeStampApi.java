package eu.europa.ec.edelivery.ebsi.timestamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.edelivery.ebsi.exceptions.TimestampException;
import eu.europa.ec.edelivery.ebsi.timestamp.entities.TimestampData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimeStampApi {


    public TimestampData geTimestampData(URL url) throws IOException, TimestampException {
        TimestampData timestampData;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        InputStream is = null;
        try {
            is = connection.getInputStream();
            timestampData = getTimestampData(is);
        } catch (IOException e) {
            String output;
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(
                    (connection.getErrorStream())));
            StringBuffer buffer = new StringBuffer();
            while ((output = br.readLine()) != null) {
                buffer.append(output);
            }
            throw new TimestampException(output, e);
        } finally {
            is.close();
            connection.disconnect();
        }

        return timestampData;
    }

    public TimestampData getTimestampData(InputStream is) throws TimestampException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(is, TimestampData.class);
        } catch (JsonProcessingException e) {
            throw new TimestampException("Error occurred while parsing JSON timestamp response", e);
        } catch (IOException e) {
            throw new TimestampException("Error occurred while reading JSON timestamp response", e);
        }
    }
}
