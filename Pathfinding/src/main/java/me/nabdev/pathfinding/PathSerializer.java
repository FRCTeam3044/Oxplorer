package me.nabdev.pathfinding;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import me.nabdev.pathfinding.structures.Path;
import me.nabdev.pathfinding.structures.Vertex;

public class PathSerializer {
    public static final int VERSION = 1;

    public static Path fromJSON(JSONObject jsonObject) {
        if (jsonObject.getInt("version") != VERSION) {
            throw new IllegalArgumentException("Invalid version! This version of oxplorer uses the v" + VERSION
                    + " JSON format, found v" + jsonObject.getInt("version") + " instead");
        }

        JSONArray pathJsonArray = jsonObject.getJSONArray("path");

        if (pathJsonArray.length() < 2) {
            throw new IllegalArgumentException("Path must have at least 2 points");
        }

        Vertex start = null;
        Vertex end = null;
        ArrayList<Vertex> path = new ArrayList<>();
        for (int i = 0; i < pathJsonArray.length(); i++) {
            JSONObject pointJsonObject = pathJsonArray.getJSONObject(i);
            Vertex v = new Vertex(pointJsonObject.getDouble("x"), pointJsonObject.getDouble("y"),
                    pointJsonObject.getDouble("rotation"));
            if (i == 0) {
                start = v;
            } else if (i == pathJsonArray.length() - 1) {
                end = v;
            } else {
                path.add(v);
            }
        }

        return new Path(start, end, path);
    }

    public static JSONObject toJSON(Path path, String displayName) {
        JSONObject pathJsonObject = new JSONObject();
        JSONArray pathJsonArray = new JSONArray();

        // Represented as [x, y, rotation, x, y, rotation, ...]
        double[] pathPoints = path.toDoubleArray();
        for (int i = 0; i < pathPoints.length; i += 3) {
            JSONObject pointJsonObject = new JSONObject();
            pointJsonObject.put("x", pathPoints[i]);
            pointJsonObject.put("y", pathPoints[i + 1]);
            pointJsonObject.put("rotation", pathPoints[i + 2]);
            pathJsonArray.put(pointJsonObject);
        }

        pathJsonObject.put("version", VERSION);
        pathJsonObject.put("lastModified", System.currentTimeMillis());
        pathJsonObject.put("displayName", displayName);
        pathJsonObject.put("path", pathJsonArray);

        return pathJsonObject;
    }
}
