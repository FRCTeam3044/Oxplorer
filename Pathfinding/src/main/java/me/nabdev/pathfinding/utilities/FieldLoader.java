package me.nabdev.pathfinding.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import me.nabdev.pathfinding.modifiers.ModifierCollection;
import me.nabdev.pathfinding.structures.Vector;
import me.nabdev.pathfinding.structures.Vertex;

/**
 * Loads a field from a JSON file
 */
public class FieldLoader {
    /**
     * The fields that can be loaded
     */
    public enum Field {
        /**
         * The 2024 field (Crescendo)
         */
        CRESCENDO_2024,
        /**
         * The 2023 field (Charged Up)
         */
        CHARGED_UP_2023,

        /**
         * A debug field for testing
         */
        DEBUG_FIELD,

        /**
         * A field with no obstacles
         */
        EMPTY_FIELD
    }

    /**
     * Temporary data structure for holding the field data
     */
    public static class FieldData {
        /**
         * The vertices of the field. Each vertex is an array of two doubles, the x and
         * y coordinates.
         */
        public ArrayList<Double[]> vertices;
        /**
         * The obstacles of the field. Each obstacle is an array of edges and an id.
         */
        public ArrayList<ObstacleData> obstacles;

        /**
         * The field's x dimension (meters)
         */
        public double fieldX;

        /**
         * The field's y dimension (meters)
         */
        public double fieldY;

        /**
         * Creates a new FieldData
         * 
         * @param vertices  The vertices of the field
         * @param obstacles The obstacles of the field
         * @param fieldX    The field's x dimension (meters)
         * @param fieldY    The field's y dimension (meters)
         */
        public FieldData(ArrayList<Double[]> vertices, ArrayList<ObstacleData> obstacles, double fieldX,
                double fieldY) {
            this.vertices = vertices;
            this.obstacles = obstacles;
            this.fieldX = fieldX;
            this.fieldY = fieldY;
        }
    }

    /**
     * Temporary data structure for holding obstacle data
     */
    public static class ObstacleData {
        /**
         * The edges of the obstacle. Each edge is an array of two integers, the
         * indices of the vertices in the field data.
         */
        public ArrayList<Integer[]> edges;
        /**
         * The id of the obstacle
         */
        public String id;

        /**
         * The modifiers on this obstacle
         */
        public ModifierCollection modifiers;

        /**
         * Creates a new ObstacleData
         * 
         * @param edges     The edges of the obstacle
         * @param id        The id of the obstacle
         * @param modifiers The modifiers on this obstacle
         */
        public ObstacleData(ArrayList<Integer[]> edges, String id, ModifierCollection modifiers) {
            this.edges = edges;
            this.id = id;
            this.modifiers = modifiers;
        }
    }

    /**
     * Load a field from the resources folder
     * 
     * @param field         The field to load
     * @param cornerCutDist The distance to cut corners by
     * @return The field JSON
     */
    public static FieldData loadField(Field field, double cornerCutDist) {
        JSONTokener tokener = new JSONTokener(
                FieldLoader.class.getClassLoader().getResourceAsStream(field.name().toLowerCase() + ".json"));
        return processField(new JSONObject(tokener), cornerCutDist);
    }

    /**
     * Load a field from a file
     * 
     * @param fieldPath     The path to the field JSON file
     * @param cornerCutDist The distance to cut corners by
     * @return The field JSON
     * @throws FileNotFoundException If the file does not exist
     */
    public static FieldData loadField(String fieldPath, double cornerCutDist) throws FileNotFoundException {
        // Load like a normal file, not a resource
        JSONTokener tokener;
        FileInputStream input = new FileInputStream(fieldPath);
        tokener = new JSONTokener(input);
        return processField(new JSONObject(tokener), cornerCutDist);
    }

    /**
     * The raw field only contains a list of verticies and id for each obstacle.
     * This method processes the raw field into a more usable format by adding the
     * edge table.
     * 
     * @param rawField      The raw field JSON
     * @param cornerCutDist The distance to cut corners by
     * @return The processed field
     */
    private static FieldData processField(JSONObject rawField, double cornerCutDist) {
        if (!rawField.has("formatVersion")) {
            throw new IllegalArgumentException("Field does not have formatVersion");
        }
        if (rawField.getInt("formatVersion") != 2) {
            throw new IllegalArgumentException("This version of Oxplorer only supports field format version 2.");
        }
        if (!rawField.has("obstacles")) {
            throw new IllegalArgumentException("Field does not have obstacles");
        }

        /*
         * Obstacles are stored as an array of vertices (x, y) and an id (string).
         * The edge table is generated from the vertices, assumming that the vertices
         * connect one after the other and the last vertex connects to the first vertex.
         * Example as stored in json:
         * {
         * "id": "1",
         * "vertices": [
         * [0.3, 8],
         * [0.3, 5.45],
         * [0, 5.45],
         * [0, 8]
         * ]
         * },
         * This is so that we can detect when the robot or target is inside an obstacle
         * and snap it.
         * Obstacles MUST be convex, and the vertices MUST be in clockwise order.
         */

        JSONArray rawObstacles = rawField.getJSONArray("obstacles");
        ArrayList<ObstacleData> obstacles = new ArrayList<>();
        ArrayList<Double[]> vertices = new ArrayList<>();

        for (int i = 0; i < rawObstacles.length(); i++) {
            JSONObject rawObstacle = rawObstacles.getJSONObject(i);
            ArrayList<Integer[]> edges = new ArrayList<>();

            // This creates the edge table, treating the vertices as a circular array
            JSONArray rawVerticies = rawObstacle.getJSONArray("vertices");
            ArrayList<Double[]> myVertices = new ArrayList<Double[]>();
            ArrayList<Double[]> myProcessedVertices = new ArrayList<Double[]>();
            for (int j = 0; j < rawVerticies.length(); j++) {
                JSONArray rawVertex = rawVerticies.getJSONArray(j);
                Double[] vertex = new Double[] { rawVertex.getDouble(0), rawVertex.getDouble(1) };
                myVertices.add(vertex);
            }
            for (int j = 0; j < myVertices.size(); j++) {
                boolean cutCorners = false;
                if (rawObstacle.has("cutCorners") && (cornerCutDist > 0)) {
                    cutCorners = rawObstacle.getBoolean("cutCorners");
                }
                if (cutCorners) {
                    Vertex prevVertex;
                    Vertex nextVertex;
                    if (j > 0) {
                        prevVertex = new Vertex(myVertices.get(j - 1)[0], myVertices.get(j - 1)[1]);
                    } else {
                        prevVertex = new Vertex(myVertices.get(myVertices.size() - 1)[0],
                                myVertices.get(myVertices.size() - 1)[1]);
                    }
                    if (j < myVertices.size() - 1) {
                        nextVertex = new Vertex(myVertices.get(j + 1)[0], myVertices.get(j + 1)[1]);
                    } else {
                        nextVertex = new Vertex(myVertices.get(0)[0], myVertices.get(0)[1]);
                    }
                    Vertex thisVertex = new Vertex(myVertices.get(j)[0], myVertices.get(j)[1]);

                    Vector toPrev = thisVertex.createVectorTo(prevVertex).normalize().scale(cornerCutDist);
                    Vector toNext = thisVertex.createVectorTo(nextVertex).normalize().scale(cornerCutDist);

                    Vertex newPrev = thisVertex.moveByVector(toPrev);
                    Vertex newNext = thisVertex.moveByVector(toNext);

                    myProcessedVertices.add(new Double[] { newPrev.x, newPrev.y });
                    myProcessedVertices.add(new Double[] { newNext.x, newNext.y });
                } else {
                    myProcessedVertices.add(myVertices.get(j));
                }
            }
            for (int j = 0; j < myProcessedVertices.size(); j++) {
                Double[] vertex = myProcessedVertices.get(j);
                vertices.add(vertex);

                if (j != myProcessedVertices.size() - 1) {
                    edges.add(new Integer[] { vertices.size() - 1, vertices.size() });
                } else {
                    edges.add(new Integer[] { vertices.size() - 1, vertices.size() - myProcessedVertices.size() });
                }
            }

            JSONArray modifiersArr;
            if (rawObstacle.has("modifiers")) {
                modifiersArr = rawObstacle.getJSONArray("modifiers");
            } else {
                modifiersArr = new JSONArray();
                modifiersArr.put("ALWAYS_ACTIVE");
            }
            ModifierCollection modifiers = new ModifierCollection(modifiersArr);
            ObstacleData obstacle = new ObstacleData(edges, rawObstacle.getString("id"), modifiers);
            obstacles.add(obstacle);
        }
        double fieldX = rawField.getDouble("fieldX");
        double fieldY = rawField.getDouble("fieldY");
        return new FieldData(vertices, obstacles, fieldX, fieldY);
    }
}
