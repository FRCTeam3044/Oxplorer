package me.nabdev.pathfinding.utilities;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * I hate that this class has to exist, and I'm sorry I wrote it.
 */
public class OrderedJSONObject extends JSONObject {

    /**
     * Creates a new OrderedJSONObject
     */
    public OrderedJSONObject() {
        super();
        try {
            Field jsonMap = getClass().getSuperclass().getDeclaredField("map");
            jsonMap.setAccessible(true);
            jsonMap.set(this, new LinkedHashMap<>());
            jsonMap.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
