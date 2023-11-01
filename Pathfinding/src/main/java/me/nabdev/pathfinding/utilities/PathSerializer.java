package me.nabdev.pathfinding.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.wpi.first.wpilibj.Filesystem;
import me.nabdev.pathfinding.structures.Path;
import me.nabdev.pathfinding.structures.Vertex;

/**
 * Serializes and deserializes paths to and from JSON
 */
public class PathSerializer {
    /**
     * The current JSON format version, used to ensure that the JSON is compatible.
     */
    public static final int VERSION = 1;

    /**
     * Load a path from disk
     * 
     * @param path The path of the JSON file to load
     * @return The path
     * @throws FileNotFoundException If the file does not exist
     */
    public static Path fromDisk(String path) throws FileNotFoundException {
        // Load like a normal file, not a resource
        FileInputStream input = new FileInputStream(path);
        JSONTokener tokener = new JSONTokener(input);
        return fromJSON(new JSONObject(tokener));
    }

    /**
     * Load a path from the deploy folder by its filename
     * 
     * @param path The path of the JSON file to load, relative to
     *             deploy/oxplorer/paths
     * @return The path
     * @throws FileNotFoundException If the file does not exist
     */
    public static Path fromDeployFolderByFilename(String path) throws FileNotFoundException, IOException {
        java.nio.file.Path containingFolder = Filesystem.getDeployDirectory().toPath().resolve("oxplorer/paths/");
        // Ensures that oxplorer/paths exists
        Files.createDirectories(containingFolder);
        FileInputStream input = new FileInputStream(containingFolder.resolve(path).toString());
        JSONTokener tokener = new JSONTokener(input);
        return fromJSON(new JSONObject(tokener));
    }

    /**
     * Load a path from the deploy folder via its display name. Spaces and other
     * non-alphanumeric characters will be replaced with underscores when searching.
     * 
     * @param displayName The name of the path to load (without the .json extension)
     * @return The path
     * @throws FileNotFoundException If the file could not be found
     */
    public static Path fromDeployFolder(String displayName) throws FileNotFoundException, IOException {
        java.nio.file.Path containingFolder = Filesystem.getDeployDirectory().toPath().resolve("oxplorer/paths/");
        String fileName = displayName.replaceAll("[^a-zA-Z0-9.-]", "_").toLowerCase() + ".json";
        // Ensures that oxplorer/paths exists
        Files.createDirectories(containingFolder);
        FileInputStream input = new FileInputStream(containingFolder.resolve(fileName).toString());
        JSONTokener tokener = new JSONTokener(input);
        return fromJSON(new JSONObject(tokener));
    }

    /**
     * Save a path to disk
     * 
     * @param filePath    The file location to save to
     * @param path        The path to save
     * @param displayName The display name of the path
     * @throws IOException If the file cannot be written to
     */
    public static void toDisk(String filePath, Path path, String displayName) throws IOException {
        JSONObject pathJsonObject = toJSON(path, displayName);
        FileWriter file = new FileWriter(filePath);
        file.write(pathJsonObject.toString(1));
        file.close();
    }

    /**
     * Save a path to the deploy folder
     * 
     * @param path        The path to save
     * @param displayName The display name of the path (will be used as the file
     *                    name)
     * @throws IOException If the file cannot be written to
     */
    public static void toDeployFolder(Path path, String displayName)
            throws IOException {
        JSONObject pathJsonObject = toJSON(path, displayName);
        java.nio.file.Path containingFolder = Filesystem.getDeployDirectory().toPath().resolve("oxplorer/paths/");
        String fileName = displayName.replaceAll("[^a-zA-Z0-9.-]", "_").toLowerCase() + ".json";
        // Ensure that oxplorer/paths exists
        Files.createDirectories(containingFolder);
        // Write to deploy folder
        FileWriter file = new FileWriter(containingFolder.resolve(fileName).toString());
        file.write(pathJsonObject.toString(1));
        file.close();

    }

    /**
     * Save a path to the deploy folder
     * 
     * @param path        The path to save
     * @param displayName The display name of the path (will be used as the file
     *                    name)
     * @param fileName    The name of the file to save to
     * @throws IOException If the file cannot be written to
     */
    public static void toDeployFolder(Path path, String displayName, String fileName)
            throws IOException {
        JSONObject pathJsonObject = toJSON(path, displayName);
        java.nio.file.Path containingFolder = Filesystem.getDeployDirectory().toPath().resolve("oxplorer/paths/");
        // Ensure that oxplorer/paths exists
        Files.createDirectories(containingFolder);
        // Write to deploy folder
        FileWriter file = new FileWriter(containingFolder.resolve(fileName).toString());
        file.write(pathJsonObject.toString(1));
        file.close();
    }

    /**
     * Load a path from a JSON object
     * 
     * @param jsonObject The JSON object to load from
     * @return The path
     */
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
            Vertex v = deserializeVertex(pointJsonObject);
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

    /**
     * Save a path to a JSON object
     * 
     * @param path        The path to save
     * @param displayName The display name of the path
     * @return The JSON object
     */
    public static JSONObject toJSON(Path path, String displayName) {
        JSONObject pathJsonObject = new OrderedJSONObject();
        JSONArray pathJsonArray = serializePath(path);

        pathJsonObject.put("version", VERSION);
        pathJsonObject.put("displayName", displayName);
        pathJsonObject.put("lastModified", System.currentTimeMillis());
        pathJsonObject.put("path", pathJsonArray);

        return pathJsonObject;
    }

    private static JSONObject serializeVertex(Vertex v) {
        JSONObject pointJsonObject = new OrderedJSONObject();
        pointJsonObject.put("x", v.x);
        pointJsonObject.put("y", v.y);
        pointJsonObject.put("rotation", v.rotation.getDegrees());
        return pointJsonObject;
    }

    private static Vertex deserializeVertex(JSONObject jsonObject) {
        return new Vertex(jsonObject.getDouble("x"), jsonObject.getDouble("y"),
                jsonObject.getDouble("rotation"));
    }

    /**
     * Serialize a path to a JSON array
     * 
     * @param path The path to serialize
     * @return The JSON array representing the path
     */
    private static JSONArray serializePath(Path path) {
        JSONArray pathJsonArray = new JSONArray();

        path.getFullPath().forEach(v -> {
            pathJsonArray.put(serializeVertex(v));
        });

        return pathJsonArray;
    }
}
