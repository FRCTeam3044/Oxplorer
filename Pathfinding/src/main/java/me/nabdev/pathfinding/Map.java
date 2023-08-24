package me.nabdev.pathfinding;

import java.util.ArrayList;

import me.nabdev.pathfinding.Structures.Edge;
import me.nabdev.pathfinding.Structures.Obstacle;
import me.nabdev.pathfinding.Structures.Vertex;
import me.nabdev.pathfinding.Structures.Vector;

/**
 * Represents all the obstacles on the map as well as the visibility graph that
 * the robot can use to navigate.
 */
public class Map {
    /**
     * The x dimension of the field (meters)
     */
    public static final double fieldx = 16.5;
    /**
     * The y dimension of the field (meters)
     */
    public static final double fieldy = 8;
    /**
     * A small epsilon value that is used to slightly inflate the path vertices
     * so that points on the same edge can have a valid line of sight.
     */
    public static final double eps = 0.001;

    /**
     * The vertices of the obstacles, inflated by the clearance parameter.
     * This is so that a robot won't attempt to shove its side into an obstacle.
     * When the class is first initialized, this represents the original uninflated
     * obstacle vertices, but it is shortly inflated by the clearance parameter.
     */
    ArrayList<Vertex> obstacleVertices;

    /**
     * The edges of the obstacles.
     */
    ArrayList<Edge> obstacleEdges;

    /**
     * The obstacles themselves.
     */
    ArrayList<Obstacle> obstacles;

    /**
     * Represents the vertices and edges of the path that the robot can use as valid
     * travel points.
     * It is the same as obstacleVertices but inflated by a small epsilon, so that
     * points on the same edge can have a valid line of sight.
     */
    ArrayList<Vertex> pathVerticesStatic = new ArrayList<>();
    /**
     * Intended to represent dynamic obstacle detections (like another robot) but is
     * currently unused.
     * It is fully implemented if you have a robot detector.
     */
    ArrayList<Vertex> pathVertices;

    /**
     * Represents the valid connections between the path vertices.
     * If two path vertices have a valid line of sight, they are considered
     * neighbors.
     */
    ArrayList<Edge> neighborsStatic = new ArrayList<>();

    /**
     * Represents the valid connections between the non-static path vertices.
     */
    ArrayList<Edge> neighbors;

    /**
     * Create a new map with the given obstacles, vertices, and clearance parameter.
     * 
     * @param obs        The obstacles.
     * @param obVertices The vertices of the obstacles.
     * @param obEdges    The edges of the obstacles.
     * @param clearance  The clearance parameter to inflate the obstacles by.
     */
    Map(ArrayList<Obstacle> obs, ArrayList<Vertex> obVertices, ArrayList<Edge> obEdges, double clearance) {
        obstacleEdges = obEdges;
        obstacleVertices = obVertices;
        obstacles = obs;
        // Uses vectors to make a list of points around the vertices of obstacles,
        // offset by the clearance parameter.
        pathVerticesStatic = calculateStaticPathVertices(obEdges, clearance);
        // Calculate the edges between these path vertices, so that the robot can't
        // phase through obstacles.
        calculateStaticNeighbors();
    }

    /**
     * Calculates the vertices of the path that the robot can use as valid travel
     * points as well as inflated obstacle vertices to generate the visibility graph
     * with.
     * Generated with a modified Minowski Sums approach.
     * 
     * @param obEdges   The edges of the obstacles.
     * @param clearance The clearance parameter to inflate the obstacles by.
     * @return The obstacle vertices inflated by clearance + eps (also modifies
     *         obstacleVertices to be inflated by the clearance parameter)
     */
    private ArrayList<Vertex> calculateStaticPathVertices(ArrayList<Edge> obEdges, double clearance) {
        ArrayList<Vertex> inflated = new ArrayList<>();
        ArrayList<Vertex> inflatedPlusEps = new ArrayList<>();

        for (Obstacle obs : obstacles) {
            // Calculate the center of the obstacle.
            double avgX = 0;
            double avgY = 0;
            for (Vertex v : obs.myVertices) {
                avgX += v.x;
                avgY += v.y;
            }
            avgX /= obs.myVertices.size();
            avgY /= obs.myVertices.size();
            Vertex center = new Vertex(avgX, avgY);

            for (Vertex v : obs.myVertices) {
                // Get the two vertices connected to the current vertex.
                Vertex connection1 = null;
                Vertex connection2 = null;
                for (Edge e : obs.edges) {
                    int contains = e.containsVertex(v, obstacleVertices);
                    if (contains != -1) {
                        if (connection1 == null) {
                            connection1 = obstacleVertices.get(contains);
                        } else {
                            connection2 = obstacleVertices.get(contains);
                        }
                    }
                }

                // Calculate the normals of the two edges connected to the current vertex
                Vector normal1 = Vector.calculateNormalWithRespectToShape(center, v, connection1).normalize()
                        .scale(clearance);
                Vector normal2 = Vector.calculateNormalWithRespectToShape(center, v, connection2).normalize()
                        .scale(clearance);

                // Move out the edges by those normals
                Vertex conn1Inflated = connection1.moveByVector(normal1);
                Vertex curInflated1 = v.moveByVector(normal1);
                Vector inflatedEdge1 = curInflated1.createVectorFrom(conn1Inflated).normalize();

                Vertex conn2Inflated = connection2.moveByVector(normal2);
                Vertex curInflated2 = v.moveByVector(normal2);
                Vector inflatedEdge2 = curInflated2.createVectorFrom(conn2Inflated).normalize();

                // Calculate the intersection of the two inflated edges
                Vertex intersection = Vector.calculateRayIntersection(conn1Inflated, inflatedEdge1, conn2Inflated,
                        inflatedEdge2);
                if (intersection == null) {
                    System.out.println(
                            "No intersection found during obstacle inflation. This is either a mapping error or a bug in pathfinding.");
                    continue;
                }

                inflated.add(intersection);
                inflatedPlusEps.add(intersection.moveByVector(intersection.createVectorFrom(v).normalize().scale(eps)));
            }
        }

        obstacleVertices.clear();
        obstacleVertices.addAll(inflated);
        return inflatedPlusEps;
    }

    private void calculateStaticNeighbors() {
        for (int cur = 0; cur < pathVerticesStatic.size(); cur++) {
            for (int i = 0; i < pathVerticesStatic.size(); i++) {
                lineOfSight(cur, i, neighborsStatic, pathVerticesStatic);
            }
        }
    }

    /**
     * Calculates the neighbors of the non-static path vertices.
     * 
     * @param additionalVertices The vertices to add to the path vertices.
     * @param reset              Whether to reset the path vertices and
     *                           neighbors to their static values (For when
     *                           generating a new path)
     */
    void calculateDynamicNeighbors(ArrayList<Vertex> additionalVertices, boolean reset) {
        if (reset || pathVertices == null)
            pathVertices = new ArrayList<>(pathVerticesStatic);
        if (reset || neighbors == null)
            neighbors = new ArrayList<>(neighborsStatic);
        pathVertices.addAll(additionalVertices);
        for (int cur = pathVertices.size() - additionalVertices.size(); cur < pathVertices.size(); cur++) {
            for (int i = 0; i < pathVertices.size(); i++) {
                lineOfSight(cur, i, neighbors, pathVertices);
            }
        }
    }

    private void lineOfSight(int cur, int i, ArrayList<Edge> neighborArray, ArrayList<Vertex> pathVerticesArray) {
        if (cur == i)
            return;
        if (neighborArray.contains(new Edge(i, cur)))
            return;
        boolean intersect = false;
        for (int x = 0; x < obstacleEdges.size(); x++) {
            if (Vector.dotIntersect(pathVerticesArray.get(cur), pathVerticesArray.get(i),
                    obstacleVertices.get(obstacleEdges.get(x).getVertexOne()),
                    obstacleVertices.get(obstacleEdges.get(x).getVertexTwo()))) {
                intersect = true;
                break;
            }
        }
        if (!intersect) {
            neighborArray.add(new Edge(cur, i));
        }
    }

    /**
     * Little helper function to get all Vertices connected via an edge to the given
     * Vertex.
     * 
     * @param Vertex The Vertex to get the neighbors of.
     * @return The neighbors of the given Vertex.
     */
    ArrayList<Vertex> getNeighbors(Vertex Vertex) {
        int index = pathVertices.indexOf(Vertex);
        if (index < 0) {
            System.out.println("Given Vertex not in path");
            return null;
        }
        ArrayList<Vertex> curNeighbors = new ArrayList<>();
        for (Edge edge : neighbors) {
            int contained = edge.containsVertex(index);
            if (contained < 0)
                continue;
            curNeighbors.add(pathVertices.get(contained));
        }
        return curNeighbors;
    }
}