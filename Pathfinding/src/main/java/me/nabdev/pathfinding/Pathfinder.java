package me.nabdev.pathfinding;

import me.nabdev.pathfinding.FieldLoader.Field;
import me.nabdev.pathfinding.Structures.Edge;
import me.nabdev.pathfinding.Structures.ImpossiblePathException;
import me.nabdev.pathfinding.Structures.Vertex;
import me.nabdev.pathfinding.Structures.Obstacle;
import me.nabdev.pathfinding.Structures.Path;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The main pathfinder class, and the only one you should need to interact with.
 */
public class Pathfinder {
    /**
     * The map this pathfinder will use
     */
    public final Map map;

    /**
     * Space between injected points on straightaways in the path (meters)
     */
    public final double pointSpacing;
    /**
     * Space between points on corners of the path (percent of the curve length)
     */
    public final double cornerPointSpacing;
    /**
     * How far back along the straightaway to dedicate to making corners
     */
    public final double cornerDist;
    /**
     * How far to inflate obstacles
     */
    public final double clearance;
    /**
     * Whether or not to inject points on straightaways
     */
    public final boolean injectPoints;
    /**
     * Whether or not to normalize distance between corner points
     */
    public final boolean normalizeCorners;
    /**
     * How far each corner should move towards the
     * other point if the distance is too short to allow both corners the full
     * corner distance
     */
    public final double cornerSplitPercent;

    // Every obstacle vertex (ORDER IS IMPORTANT)
    ArrayList<Vertex> obstacleVertices = new ArrayList<>();
    ArrayList<Vertex> uninflatedObstacleVertices = new ArrayList<>();
    // An edge table that has the indexes of connected vertices
    ArrayList<Edge> edges = new ArrayList<>();

    ArrayList<Obstacle> obstacles = new ArrayList<>();

    /**
     * Create a new pathfinder. Should only be done once, at the start of the
     * program.
     * 
     * @param fieldType          The field to load
     * @param pointSpacing       The distance between points on the straightaways
     * @param cornerPointSpacing The distance between points on the corners
     * @param cornerDist         How far back along the straightaway to dedicate to
     *                           corners
     * @param clearance          The clearance to use when inflating obstacles
     * @param cornerSplitPercent How far back along the straightaway to dedicate to
     *                           a corner when the straightaway is too small to fit
     *                           both corners (percentage, should be less than 0.5)
     */
    public Pathfinder(Field fieldType, double pointSpacing, double cornerPointSpacing, double cornerDist,
            double clearance, double cornerSplitPercent, boolean injectPoints, boolean normalizeCorners) {
        this.pointSpacing = pointSpacing;
        this.cornerPointSpacing = cornerPointSpacing;
        this.cornerDist = cornerDist;
        this.clearance = clearance;
        this.cornerSplitPercent = cornerSplitPercent;
        this.injectPoints = injectPoints;
        this.normalizeCorners = normalizeCorners;

        JSONObject field = FieldLoader.loadField(fieldType);
        // This is essentially a vertex and edge table, with some extra information.
        // Vertices are stored as an array [x, y]
        JSONArray verticesRaw = field.getJSONArray("vertices");
        /*
         * Obstacles are stored as an array of edges, each edge being an array of 2
         * vertices.
         * The vertices are stored as the index of the vertex in the vertex array.
         * Example:
         * [
         * [0,1],
         * [1,2],
         * [2,3],
         * [3,0]
         * ]
         * This is so that we can detect when the robot or target is inside an obstacle
         * and snap it.
         * Obstacles MUST be convex, and the vertices MUST be in clockwise order.
         */
        JSONArray obstaclesRaw = field.getJSONArray("obstacles");

        // Convert from JSONArray to arraylist to make math easier
        for (int i = 0; i < verticesRaw.length(); i++) {
            JSONArray vertex = verticesRaw.getJSONArray(i);
            obstacleVertices.add(new Vertex(vertex.getDouble(0), vertex.getDouble(1)));
            uninflatedObstacleVertices.add(new Vertex(vertex.getDouble(0), vertex.getDouble(1)));
        }
        for (int i = 0; i < obstaclesRaw.length(); i++) {
            JSONArray obstacle = obstaclesRaw.getJSONArray(i);
            ArrayList<Edge> curEdges = new ArrayList<Edge>();
            for (int x = 0; x < obstacle.length(); x++) {
                JSONArray edgeRaw = obstacle.getJSONArray(x);
                edges.add(new Edge(edgeRaw.getInt(0), edgeRaw.getInt(1)));
                curEdges.add(new Edge(edgeRaw.getInt(0), edgeRaw.getInt(1)));
            }
            Obstacle newObs = new Obstacle(obstacleVertices, curEdges);
            obstacles.add(newObs);
        }

        // Create the map object
        map = new Map(obstacles, obstacleVertices, edges, clearance);

        for (Obstacle obs : obstacles) {
            obs.initialize(map.pathVerticesStatic);
        }
    }

    /**
     * Snaps the start and target vertices according to the snap mode, calculates
     * visibility graph for dynamic elements, and generates the best path using
     * A-star algorithm.
     * 
     * @param start           The starting vertex
     * @param target          The target vertex
     * @param snapMode        The snap mode to use
     * @param dynamicVertices An ArrayList of dynamic vertices
     * 
     * @return The shortest path from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target, PathfindSnapMode snapMode, ArrayList<Vertex> dynamicVertices)
            throws ImpossiblePathException {
        return generatePathInner(start, target, snapMode, dynamicVertices);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles and generates
     * the best path using A-star algorithm.
     * Defaults to PathfindSnapMode.SNAP_ALL
     * 
     * @param start  The starting vertex
     * @param target The target vertex
     * 
     * @return The shortest path from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target) throws ImpossiblePathException {
        return generatePathInner(start, target, PathfindSnapMode.SNAP_ALL, new ArrayList<Vertex>());
    }

    /**
     * Snaps the start and target vertices according to the snap mode and generates
     * the best path using A-star algorithm.
     * 
     * @param start    The starting vertex
     * @param target   The target vertex
     * @param snapMode The snap mode to use
     * 
     * @return The shortest path from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target, PathfindSnapMode snapMode) throws ImpossiblePathException {
        return generatePathInner(start, target, snapMode, new ArrayList<Vertex>());
    }

    // Using an inner function because java handles optional parameters poorly
    private Path generatePathInner(Vertex start, Vertex target, PathfindSnapMode snapMode,
            ArrayList<Vertex> dynamicVertices) throws ImpossiblePathException {
        // Snapping is done because the center of the robot can be inside of the
        // inflated obstacle edges
        // In the case where this happened the start needs to be snapped outside
        // otherwise a* will fail
        Vertex unsnappedTarget = target;
        if (snapMode == PathfindSnapMode.SNAP_ALL || snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE
                || snapMode == PathfindSnapMode.SNAP_START) {
            start = snap(start);
        }

        if (snapMode == PathfindSnapMode.SNAP_ALL || snapMode == PathfindSnapMode.SNAP_TARGET) {
            target = snap(target);
        } else if (snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE
                || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE) {
            target = snap(target);
        }

        long startTime = System.nanoTime();

        ArrayList<Vertex> additionalVertexs = new ArrayList<>();
        additionalVertexs.add(start);
        additionalVertexs.add(target);
        additionalVertexs.addAll(dynamicVertices);
        map.calculateDynamicNeighbors(additionalVertexs, true);

        Astar astar = new Astar(map, this);
        // This could throw ImpossiblePathException
        Path path = astar.run(start, target);
        path.setUnsnappedTarget(unsnappedTarget);
        path.processPath(snapMode);
        long endTime = System.nanoTime();
        System.out.println("Path generation time: " + (endTime - startTime) / 1000000 + "ms");

        return path;
    }

    /**
     * Snap a vertex to the nearest obstacle edge if it's inside of one
     * 
     * @param point Point to snap
     * @return
     */
    private Vertex snap(Vertex point) throws ImpossiblePathException {
        ArrayList<Obstacle> targetObs = Obstacle.isRobotInObstacle(obstacles, point);
        Vertex tempNearestVertex = point;
        int i = 0;
        while (targetObs.size() > 0) {
            if (i > 10) {
                throw new ImpossiblePathException("Failed to snap point " + point);
            }
            for (Obstacle obs : targetObs) {
                tempNearestVertex = obs.calculateNearestPoint(tempNearestVertex);
            }
            targetObs = Obstacle.isRobotInObstacle(obstacles, tempNearestVertex);
            i++;
        }
        return tempNearestVertex;
    }

    /**
     * Determines how vertices will be snapped to the nearest obstacle edge if they
     * are inside of an obstacle.
     * Snapping is useful in case the robot center is inside of the inflated
     * obstacle vertices.
     */
    public enum PathfindSnapMode {
        /**
         * No snapping. If a the start or target is inside of an obstacle, an
         * ImpossiblePathException will be thrown.
         */
        NONE,
        /**
         * Snap start and target vertices
         */
        SNAP_ALL,
        /**
         * Snap start and
         * target vertices. If the target is inside an obstacle, draw a
         * straight line from the snapped target to the original target to drive there
         * anyways
         * 
         * @deprecated Can create very sharp corners as it just draws a straight line,
         *             not recommended. Instead, use
         *             {@link Path#getUnsnappedTarget()} and handle moving
         *             there yourself after you follow the path if you need to. This may
         *             be replaced with a better solution in the future.
         */
        @Deprecated
        SNAP_ALL_THEN_LINE,
        /**
         * Snap start vertex
         */
        SNAP_START,
        /**
         * Snap target vertex
         */
        SNAP_TARGET,

        /**
         * If the target is
         * inside an obstacle, snap it and draw a straight line from
         * the snapped target to the original target to drive there anyways.
         * 
         * @deprecated Can create very sharp corners as it just draws a straight line,
         *             not recommended. Instead, use
         *             {@link Path#getUnsnappedTarget()} and handle moving
         *             there yourself after you follow the path if you need to. This may
         *             be replaced with a better solution in the future.
         */
        @Deprecated
        SNAP_TARGET_THEN_LINE

    }

    /**
     * Intended for use with our custom advantagescope fork for debugging the
     * visibility graph.
     * 
     * @return An arraylist containing edges to represent all neighbors, showing the
     *         visibility graph
     */
    public ArrayList<Edge> visualizeNeighbors() {
        ArrayList<Edge> list = new ArrayList<>();
        if (map.neighbors != null)
            list.addAll(map.neighbors);
        if (map.neighborsStatic != null)
            list.addAll(map.neighborsStatic);
        return list;
    }

    /**
     * Intended for use with our custom advantagescope fork for debugging the
     * visibility graph.
     * 
     * @return An arraylist containing vertices to represent all vertices in the
     *         visibility graph. This is the same as visualizeVerticies, but
     *         includes dynamic vertices like the start and end points.
     */
    public ArrayList<Vertex> visualizePathVertices() {
        ArrayList<Vertex> list = new ArrayList<>();
        if (map.pathVertices != null)
            list.addAll(map.pathVertices);
        return list;
    }

    /**
     * Intended for use with our custom advantagescope fork for debugging new
     * fields.
     * 
     * @return An arraylist containing edges to represent all defined field edges
     */
    public ArrayList<Edge> visualizeEdges() {
        ArrayList<Edge> list = new ArrayList<>();
        list.addAll(edges);
        return list;
    }

    /**
     * Intended for use with our custom advantagescope fork for debugging new
     * fields.
     * 
     * @return An arraylist containing vertices to represent all defined, uninflated
     *         obstacle vertices
     */
    public ArrayList<Vertex> visualizeVertices() {
        ArrayList<Vertex> list = new ArrayList<>();
        list.addAll(uninflatedObstacleVertices);
        return list;
    }

    /**
     * Intended for use with our custom advantagescope fork for debugging new
     * fields.
     * 
     * @return An arraylist containing vertices to represent all uninflated obstacle
     *         vertices
     */
    public ArrayList<Vertex> visualizeInflatedVertices() {
        ArrayList<Vertex> list = new ArrayList<>();
        list.addAll(map.pathVerticesStatic);
        return list;
    }
}
