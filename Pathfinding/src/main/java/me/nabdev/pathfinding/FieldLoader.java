package me.nabdev.pathfinding;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Loads a field from a JSON file
 */
public class FieldLoader {
    /**
     * The fields that can be loaded
     */
    public enum Field {
        /**
         * The 2023 field (Charged Up)
         */
        CHARGED_UP_2023,
        /**
         * A debug field for testing
         */
        DEBUG_FIELD
    }

    static JSONObject loadField(Field field) {
        JSONTokener tokener = new JSONTokener(
                FieldLoader.class.getClassLoader().getResourceAsStream(field.name().toLowerCase() + ".json"));
        return new JSONObject(tokener);
    }

    static JSONObject loadField(String fieldPath) throws FileNotFoundException {
        // Load like a normal file, not a resource
        JSONTokener tokener;
        FileInputStream input = new FileInputStream(fieldPath);
        tokener = new JSONTokener(input);
        return new JSONObject(tokener);
    }
}
