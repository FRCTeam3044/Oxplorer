package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

import org.json.JSONArray;

import me.nabdev.pathfinding.modifiers.ModifierCollection;

/**
 * Represents an obstacle on the map. An obstacle is a collection of vertices
 * and edges.
 */
public class Obstacle {
    /**
     * The edges that make up the obstacle. The edges contain indices of the
     * vertices array.
     * MUST BE IN CLOCKWISE ORDER! (This is important for the isInside method)
     */
    private ArrayList<Edge> edges;
    /**
     * The vertices that make up the obstacle.
     * MUST BE IN CLOCKWISE ORDER! (This is important for the isInside method)
     */
    private ArrayList<Vertex> myVertices = new ArrayList<Vertex>();

    /**
     * The id of the obstacle. This will be used to add special behavior to
     * obstacles which may only be used in certain situations.
     */
    public String id;

    // ALL VERTICES FOR THE ENTIRE MAP! NOT JUST THE OBSTACLE!
    private ArrayList<Vertex> vertices;
    // Vectors along the edges of the obstacle
    private ArrayList<Vector> vectors = new ArrayList<Vector>();

    /**
     * The modifiers on this obstacle.
     */
    public ModifierCollection modifiers;

    /**
     * Creates a new obstacle.
     * 
     * @param vertices  All vertices on the map.
     * @param edges     The edges that make up the obstacle.
     * @param id        The id of the obstacle.
     * @param modifiers The modifiers on this obstacle.
     */
    public Obstacle(ArrayList<Vertex> vertices, ArrayList<Edge> edges, String id, ModifierCollection modifiers) {
        this.edges = edges;
        this.vertices = vertices;
        this.id = id;
        this.modifiers = modifiers;
        for (Edge edge : edges) {
            myVertices.add(vertices.get(edge.getVertexOne()));
        }
    }

    /**
     * Creates a new obstacle with the always active modifier.
     * 
     * @param vertices All vertices on the map.
     * @param edges    The edges that make up the obstacle.
     * @param id       The id of the obstacle.
     */
    public Obstacle(ArrayList<Vertex> vertices, ArrayList<Edge> edges, String id) {
        this.edges = edges;
        this.vertices = vertices;
        this.id = id;
        JSONArray modifiersArr = new JSONArray();
        modifiersArr.put("ALWAYS_ACTIVE");
        this.modifiers = new ModifierCollection(modifiersArr);
        for (Edge edge : edges) {
            myVertices.add(vertices.get(edge.getVertexOne()));
        }
    }

    /**
     * Re-initializes the obstacle. This is used when the vertices are updated
     * (usually via inflation)
     * 
     * @param vertices The new vertices.
     */
    public void initialize(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
        myVertices.clear();
        for (Edge edge : edges) {
            vectors.add(vertices.get(edge.getVertexOne()).createVectorFrom(vertices.get(edge.getVertexTwo())));
            myVertices.add(vertices.get(edge.getVertexOne()));
        }
    }

    /**
     * Detect if a vertex is inside any obstacle in the list.
     * 
     * @param obstacles The list of obstacles to check.
     * @param vertex    The vertex to check.
     * @return The list of obstacles that the vertex is inside, empty if none.
     */
    public static ArrayList<Obstacle> isRobotInObstacle(ArrayList<Obstacle> obstacles, Vertex vertex) {
        ArrayList<Obstacle> inside = new ArrayList<Obstacle>();
        for (Obstacle obs : obstacles) {
            if (obs.isInside(vertex)) {
                inside.add(obs);
            }
        }
        return inside;
    }

    /**
     * Checks if the given vertex is inside the obstacle.
     * 
     * @param pos The vertex to check.
     * @return True if the vertex is inside the obstacle, false otherwise.
     */
    public boolean isInside(Vertex pos) {
        int windingNumber = 0;
        int n = edges.size();

        for (int i = 0; i < n; i++) {
            Vertex v1 = vertices.get(edges.get(i).getVertexOne());
            Vertex v2 = vertices.get(edges.get(i).getVertexTwo());

            if (v1.y <= pos.y) {
                if (v2.y > pos.y && isLeft(v1, v2, pos) > 0) {
                    windingNumber++;
                }
            } else {
                if (v2.y <= pos.y && isLeft(v1, v2, pos) < 0) {
                    windingNumber--;
                }
            }
        }

        return windingNumber != 0;
    }

    private double isLeft(Vertex v1, Vertex v2, Vertex point) {
        return ((v2.x - v1.x) * (point.y - v1.y)) - ((point.x - v1.x) * (v2.y - v1.y));
    }

    /**
     * Calculates the nearest point on the obstacle to the given vertex, assuming
     * the given vertex is inside the obstacle
     * 
     * @param v The vertex to calculate the nearest point to.
     * @return The nearest point on the obstacle to the given vertex.
     */
    public Vertex calculateNearestPointFromInside(Vertex v) {
        double[] distances = new double[edges.size()];
        Vertex[] closestPoints = new Vertex[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            Vertex vertexOne = vertices.get(edge.getVertexOne());
            Vector vect = v.createVectorFrom(vertexOne);
            Vector edgeVector = vertices.get(edge.getVertexTwo()).createVectorFrom(vertexOne);
            double dot = vect.dotProduct(edgeVector.normalize());
            closestPoints[i] = vertexOne.moveByVector(edgeVector.normalize().scale(dot));
            distances[i] = v.distance(closestPoints[i]);
        }
        int lowest = 0;
        for (int i = 1; i < edges.size(); i++) {
            if (distances[i] < distances[lowest]) {
                lowest = i;
            }
        }
        Vertex finalPoint = closestPoints[lowest];
        Vector finalVector = finalPoint.createVectorFrom(v);
        Vector normalizedFinalVector = finalVector.normalize().scale(0.001);

        return v.moveByVector(finalVector.add(normalizedFinalVector));
    }

    /**
     * Calculates the nearest point on the obstacle to the given vertex.
     * 
     * @param v The vertex to calculate the nearest point to.
     * @return The nearest point on the obstacle to the given vertex.
     */
    public Vertex calculateNearestPoint(Vertex v) {
        double[] distances = new double[edges.size()];
        Vertex[] closestPoints = new Vertex[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            Vertex vertexOne = vertices.get(edge.getVertexOne());
            Vertex vertexTwo = vertices.get(edge.getVertexTwo());
            Vector edgeVector = vertexTwo.createVectorFrom(vertexOne);
            Vector vect = v.createVectorFrom(vertexOne);
            double dot = vect.dotProduct(edgeVector.normalize());

            if (dot < 0) {
                closestPoints[i] = vertexOne;
            } else if (dot > edgeVector.magnitude()) {
                closestPoints[i] = vertexTwo;
            } else {
                closestPoints[i] = vertexOne.moveByVector(edgeVector.normalize().scale(dot));
            }

            distances[i] = v.distance(closestPoints[i]);
        }

        int lowest = 0;
        for (int i = 1; i < edges.size(); i++) {
            if (distances[i] < distances[lowest]) {
                lowest = i;
            }
        }

        return closestPoints[lowest];
    }

    /**
     * Checks if the obstacle is convex and clockwise, used to validate the map.
     * 
     * @return True if the obstacle is convex and clockwise, false otherwise.
     */
    public boolean isConvexAndClockwise() {
        int n = edges.size();

        for (int i = 0; i < n; i++) {
            Vertex v1 = vertices.get(edges.get(i).getVertexOne());
            Vertex v2 = vertices.get(edges.get(i).getVertexTwo());
            Vertex v3 = vertices.get(edges.get((i + 1) % n).getVertexTwo());

            double crossProduct = isLeft(v1, v2, v3);

            if (crossProduct > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create and initializes a new obstacle from the given vertices with the
     * AlWAYS_ACTIVE modifier. Generates the edge table automatically.
     * 
     * @param vertices The list of vertices in the obstacle.
     * @return The new obstacle.
     */
    public static Obstacle createObstacle(Vertex... vertices) {
        ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        for (int j = 0; j < vertices.length; j++) {
            Vertex vertex = vertices[j];
            vertexList.add(vertex);

            if (j != vertices.length - 1) {
                edges.add(new Edge(vertexList.size() - 1, vertexList.size()));
            } else {
                edges.add(new Edge(vertexList.size() - 1, vertexList.size() - vertices.length));
            }
        }
        Obstacle obs = new Obstacle(vertexList, edges, "test");
        obs.initialize(vertexList);
        return obs;
    }

    /**
     * Get all vertices this obstacle knows about. (The entire field, if it is a
     * field obstacle)
     * 
     * @return The master list of vertices.
     */
    public ArrayList<Vertex> getMasterVertices() {
        return vertices;
    }

    /**
     * Get all vertices in this obstacle.
     * 
     * @return The list of vertices.
     */
    public ArrayList<Vertex> getVertices() {
        return myVertices;
    }

    /**
     * Get all edges in this obstacle.
     * 
     * @return The list of edges.
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }
}