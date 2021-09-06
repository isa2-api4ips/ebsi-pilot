package eu.domibus.core.util;

import com.google.gson.reflect.TypeToken;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDto;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public class JsonUtilTest {

    JsonUtilImpl jsonUtil = new JsonUtilImpl();

    @Test
    public void testJsonToList() throws Exception {
        String json = "['1', '2']";
        List<String> strings = jsonUtil.jsonToList(json);
        Assert.assertTrue(strings.get(0).equals("1"));
    }

    @Test
    public void serializaDeserializeIT() {

        String id1 = "id1";
        String id2 = "id2";
        List<String> ids = Arrays.asList(id1, id2);
        String json = jsonUtil.listToJson(ids);
        List<String> list = jsonUtil.jsonToList(json);

        Assert.assertTrue(list.size() == 2);
        Assert.assertTrue(list.get(0).equals(id1));
        Assert.assertTrue(list.get(1).equals(id2));
    }

    @Test
    public void serializaDeserializeUserMessageLogDtoIT() {
        String id1 = "id1";
        String id2 = "id2";
        String backend = "ws";
        UserMessageLogDto uml1 = new UserMessageLogDto(id1, MessageSubtype.TEST, backend);
        UserMessageLogDto uml2 = new UserMessageLogDto(id2, MessageSubtype.TEST, backend);
        List<UserMessageLogDto> umls = Arrays.asList(uml1, uml2);
        String json = jsonUtil.listToJson(umls);

        Type type = new TypeToken<ArrayList<UserMessageLogDto>>() {
        }.getType();
        List<UserMessageLogDto> list = jsonUtil.jsonToList(json, type);

        Assert.assertTrue(list.size() == 2);
        Assert.assertTrue(list.get(0).getMessageId().equals(id1));
        Assert.assertTrue(list.get(1).getMessageId().equals(id2));
        Assert.assertTrue(list.get(1).getBackend().equals(backend));
    }
}
