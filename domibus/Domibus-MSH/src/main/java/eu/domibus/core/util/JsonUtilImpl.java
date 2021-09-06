package eu.domibus.core.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eu.domibus.api.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
@Component
public class JsonUtilImpl implements JsonUtil {

    @Override
    public Map<String, Object> jsonToMap(String map) {
        if (map == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return new Gson().fromJson(map, type);
    }

    @Override
    public String listToJson(List list) {
        if (list == null) {
            return null;
        }
        return new Gson().toJson(list);
    }

    @Override
    public List jsonToList(String list) {
        if (list == null) {
            return null;
        }

        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        return new Gson().fromJson(list, type);
    }

    @Override
    public List jsonToList(String list, Type type) {
        if (list == null) {
            return null;
        }

        return new Gson().fromJson(list, type);
    }

}
