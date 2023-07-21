package me.nabdev.pathfinding;

import org.json.JSONObject;
import org.json.JSONTokener;

public class FieldLoader {
    public enum Field {
        CHARGED_UP_2023
    }

    public static JSONObject loadField(Field field){
        JSONTokener tokener;
        tokener = new JSONTokener(FieldLoader.class.getClassLoader().getResourceAsStream(field.name().toLowerCase() + ".json")); 
        return new JSONObject(tokener);
    }
}
