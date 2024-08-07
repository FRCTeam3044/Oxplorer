package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

/**
 * Represents all the obstacles on the map as well as the visibility graph that
 * the robot can use to navigate.
 */
public class Map {
    /**
     * The x dimension of the field (meters)
     */
    public final double fieldx;
    /**
     * The y dimension of the field (meters)
     */
    public final double fieldy;
    /**
     * The x coordinate of the origin of the field (meters)
     * Used to compute if a point is inside of the field bounds.
     */
    public static final double originx = 0;
    /**
     * The y coordinate of the origin of the field (meters)
     * Used to compute if a point is inside of the field bounds.
     */
    public static final double originy = 0;
    /**
     * A small epsilon value that that is used to slightly inflate the path vertices
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
     * The edges of obstacles that are inside of the field bounds.
     */
    ArrayList<Edge> validObstacleEdges = new ArrayList<>();
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
     * It is fully implemented, if you have a robot detector.
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
     * @param fieldx     The x dimension of the field (meters)
     * @param fieldy     The y dimension of the field (meters)
     */
    public Map(ArrayList<Obstacle> obs, ArrayList<Vertex> obVertices, ArrayList<Edge> obEdges, double clearance,
            double fieldx, double fieldy) {
        obstacleEdges = obEdges;
        obstacleVertices = obVertices;
        obstacles = obs;
        this.fieldx = fieldx;
        this.fieldy = fieldy;
        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle o = obstacles.get(i);
            if (!o.isConvexAndClockwise()) {
                throw new IllegalArgumentException(
                        "Obstacle at index " + i + " is invalid (not clockwise and/or convex).");
            }
        }
        // Uses vectors to make a list of points around the vertices of obstacles,
        // offset by the clearance parameter.
        pathVerticesStatic = calculateStaticPathVertices(clearance);
        validObstacleEdges = getValidObstacleEdges(obstacleEdges, obstacleVertices);
        regenerateVisibilityGraph();
    }

    /**
     * Regenerates the visibility graph with the current path vertices and
     * obstacles.
     */
    public void regenerateVisibilityGraph() {
        checkPathVertices(pathVerticesStatic, obstacles);
        neighborsStatic = calculateStaticNeighbors(pathVerticesStatic, validObstacleEdges, obstacleVertices);
    }

    /**
     * Check all obstacle vertices to see if they are outside of field bounds or
     * inside of another obstacle, in which case, mark them to be skipped during
     * visibility graph generation.
     * 
     * @param vertices  The vertices to check.
     * @param obstacles The obstacles to check against.
     */
    public void checkPathVertices(ArrayList<Vertex> vertices, ArrayList<Obstacle> obstacles) {
        for (Vertex v : vertices) {
            if (v.x < originx || v.x > fieldx || v.y < originy || v.y > fieldy) {
                v.validVisibility = false;
            } else if (Obstacle.isRobotInObstacle(obstacles, v).size() > 0) {
                v.validVisibility = false;
            } else {
                v.validVisibility = true;
            }
        }
    }

    /**
     * Check all obstacle edges to see if they are completely outside of field
     * bounds, and if they aren't add them to the validObstacleEdges list.
     * This currently only covers some cases, but is good enough for now.
     * 
     * @param obstacleEdges    The edges of the obstacles.
     * @param obstacleVertices The vertices of the obstacles.
     * 
     * @return The edges that are inside of the field bounds.
     */
    public ArrayList<Edge> getValidObstacleEdges(ArrayList<Edge> obstacleEdges, ArrayList<Vertex> obstacleVertices) {
        ArrayList<Edge> validObstacleEdges = new ArrayList<Edge>();
        for (Edge e : obstacleEdges) {
            Vertex v1 = e.getVertexOne(obstacleVertices);
            Vertex v2 = e.getVertexTwo(obstacleVertices);
            if (!((v1.x < originx && v2.x < originx) || (v1.x > fieldx && v2.x > fieldx)
                    || (v1.y < originy && v2.y < originy) || (v1.y > fieldy && v2.y > fieldy))) {
                validObstacleEdges.add(e);
            }
        }
        return validObstacleEdges;
    }

    /**
     * Calculates the vertices of the path that the robot can use as valid travel
     * points as well as inflated obstacle vertices to generate the visibility graph
     * with.
     * Generated with a modifed Minowski Sums approach.
     * 
     * @param clearance The clearance parameter to inflate the obstacles by.
     * @return The obstacle vertices inflated by clearance + eps (also modifies
     *         obstacleVertices to be inflated by the clearance parameter)
     */
    private ArrayList<Vertex> calculateStaticPathVertices(double clearance) {
        ArrayList<Vertex> inflated = new ArrayList<>();
        ArrayList<Vertex> inflatedPlusEps = new ArrayList<>();

        for (Obstacle obs : obstacles) {
            // Calculate the center of the obstacle.
            double avgX = 0;
            double avgY = 0;
            for (Vertex v : obs.getVertices()) {
                avgX += v.x;
                avgY += v.y;
            }
            avgX /= obs.getVertices().size();
            avgY /= obs.getVertices().size();
            Vertex center = new Vertex(avgX, avgY);

            for (Vertex v : obs.getVertices()) {
                // Get the two vertices connected to the current vertex.
                Vertex connection1 = null;
                Vertex connection2 = null;
                for (Edge e : obs.getEdges()) {
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

    /**
     * Calculates the neighbors of the static path vertices (regenerates cached
     * visibility graph)
     * 
     * Will save the neighbors to the vertices themselves.
     * 
     * @param vertices    The vertices to calculate the neighbors of.
     * @param obsEdges    The edges of the obstacles.
     * @param obsVertices The vertices of the obstacles.
     * 
     * @return The neighbors of the vertices.
     */
    public ArrayList<Edge> calculateStaticNeighbors(ArrayList<Vertex> vertices, ArrayList<Edge> obsEdges,
            ArrayList<Vertex> obsVertices) {
        ArrayList<Edge> newNeighbors = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertex v1 = vertices.get(i);
                Vertex v2 = vertices.get(j);
                boolean visible = lineOfSight(v1, v2, obsEdges, obsVertices);
                if (visible) {
                    newNeighbors.add(new Edge(i, j));
                }
            }
        }
        for (Vertex v : pathVerticesStatic) {
            v.staticNeighbors.clear();
        }
        for (Edge e : newNeighbors) {
            Vertex v1 = e.getVertexOne(pathVerticesStatic);
            Vertex v2 = e.getVertexTwo(pathVerticesStatic);
            v1.staticNeighbors.add(v2);
            v2.staticNeighbors.add(v1);
        }
        return newNeighbors;
    }

    /**
     * Calculates the neighbors of the non-static path vertices.
     * 
     * @param additionalVertices The vertices to add to the path vertices.
     * @param reset              Whether or not to reset the path vertices and
     *                           neighbors to their static values (For when
     *                           generating a new path)
     */
    public void calculateDynamicVisibilityGraph(ArrayList<Vertex> additionalVertices, boolean reset) {
        if (reset || pathVertices == null)
            pathVertices = new ArrayList<>(pathVerticesStatic);
        if (reset || neighbors == null)
            neighbors = new ArrayList<>(neighborsStatic);
        pathVertices.addAll(additionalVertices);
        if (reset) {
            for (Vertex v : pathVertices) {
                v.dynamicNeighbors.clear();
            }
        }
        ArrayList<Edge> dynamicNeighbors = calculateDynamicNeighbors(pathVertices, additionalVertices.size(),
                validObstacleEdges, obstacleVertices);
        for (Edge e : dynamicNeighbors) {
            Vertex v1 = e.getVertexOne(pathVertices);
            Vertex v2 = e.getVertexTwo(pathVertices);
            v1.dynamicNeighbors.add(v2);
            v2.dynamicNeighbors.add(v1);
        }
        neighbors.addAll(dynamicNeighbors);
    }

    /**
     * Calculates the neighbors of the non-static path vertices.
     * 
     * @param additionalVertices The vertices to add to the path vertices.
     * @param obsEdges           The edges of the obstacles.
     * @param obsVertices        The vertices of the obstacles.
     */
    private ArrayList<Edge> calculateDynamicNeighbors(ArrayList<Vertex> vertices, int numAdditional,
            ArrayList<Edge> obsEdges,
            ArrayList<Vertex> obsVertices) {
        ArrayList<Edge> dynamicNeighbors = new ArrayList<>();

        for (int i = vertices.size() - numAdditional; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (i == j)
                    continue;
                Vertex v1 = vertices.get(i);
                Vertex v2 = vertices.get(j);
                boolean visible = lineOfSight(v1, v2, obsEdges, obsVertices);
                if (visible) {
                    dynamicNeighbors.add(new Edge(i, j));
                }
            }
        }
        return dynamicNeighbors;
    }

    /**
     * Checks if there is a line of sight between two vertices.
     * 
     * @param v1          The first vertex.
     * @param v2          The second vertex.
     * @param obsEdges    The edges of the obstacles.
     * @param obsVertices The vertices of the obstacles.
     * @return Whether there is a line of sight between the two vertices.
     */
    private boolean lineOfSight(Vertex v1, Vertex v2, ArrayList<Edge> obsEdges, ArrayList<Vertex> obsVertices) {
        if (!v1.validVisibility || !v2.validVisibility)
            return false;

        boolean intersect = false;

        for (Edge e : obsEdges) {
            if (!e.isActive())
                continue;
            if (Vector.dotIntersectFast(v1, v2, e.getVertexOne(obsVertices), e.getVertexTwo(obsVertices))) {
                intersect = true;
                break;
            }
        }
        return !intersect;
    }

    /**
     * Get the uninflated vertices of the obstacles.
     * 
     * @return The vertices of the obstacles.
     */
    public ArrayList<Vertex> getPathVertices() {
        return pathVertices;
    }

    /**
     * Get the inflated vertices of the obstacles.
     * 
     * @return The inflated vertices of the obstacles.
     */
    public ArrayList<Vertex> getPathVerticesStatic() {
        return pathVerticesStatic;
    }

    /**
     * Get the neighbors of the vertices of the static obstacles.
     * 
     * @return The neighbors of the vertices of the static obstacles.
     */
    public ArrayList<Edge> getNeighbors() {
        return neighbors;
    }

    /**
     * Get the neighbors of the dynamic vertices.
     * 
     * @return The neighbors of the dynamic vertices.
     */
    public ArrayList<Edge> getNeighborsStatic() {
        return neighborsStatic;
    }

    /**
     * Get the obstacles for the current map
     *
     * @return the obstacles of the current map
     */
    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * Get all obstacle edges which could actually obstruct the robot
     *
     * @return all obstacle edges that could obstruct the robot
     */
    public ArrayList<Edge> getValidObstacleEdges() {
        return validObstacleEdges;
    }
}
