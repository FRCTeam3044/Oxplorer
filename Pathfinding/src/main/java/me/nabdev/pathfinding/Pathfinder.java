package me.nabdev.pathfinding;

import me.nabdev.pathfinding.algorithms.Astar;
import me.nabdev.pathfinding.algorithms.SearchAlgorithm;
import me.nabdev.pathfinding.algorithms.SearchAlgorithm.SearchAlgorithmType;
import me.nabdev.pathfinding.modifiers.ObstacleModifier.ObstacleModifierTypes;
import me.nabdev.pathfinding.structures.Edge;
import me.nabdev.pathfinding.structures.ImpossiblePathException;
import me.nabdev.pathfinding.structures.Map;
import me.nabdev.pathfinding.structures.Obstacle;
import me.nabdev.pathfinding.structures.Path;
import me.nabdev.pathfinding.structures.Vertex;
import me.nabdev.pathfinding.utilities.FieldLoader.FieldData;
import me.nabdev.pathfinding.utilities.FieldLoader.ObstacleData;

import java.util.ArrayList;
import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
    private double pointSpacing;
    /**
     * Space between points on corners of the path (percent of the curve length)
     */
    private double cornerPointSpacing;
    /**
     * How far back along the straightaway to dedicate to making corners
     */
    private double cornerDist;
    /**
     * How far to inflate obstacles
     */
    private final double clearance;
    /**
     * Whether or not to inject points on straightaways
     */
    private boolean injectPoints;
    /**
     * Whether or not to normalize distance between corner points
     */
    private boolean normalizeCorners;
    /**
     * How far each corner should move towards the
     * other point if the distance is too short to allow both corners the full
     * corner distance
     */
    private double cornerSplitPercent;

    /**
     * Time in seconds when the robot should start to consider
     * endgame obstacles. Note that this uses DriverStation.getMatchTime(), so it
     * is not perfectly accurate, and does not work properly when teleop/auto is
     * manually enabled from the driver station (practice mode works fine)
     */
    private static double endgameTime;

    /**
     * Whether or not to profile the pathfinding process
     */
    private boolean profiling;
    /**
     * The search algorithm to use
     */
    private SearchAlgorithmType searchAlgorithmType;

    private double lastMatchTime = DriverStation.getMatchTime();
    private Optional<Alliance> lastAlliance = DriverStation.getAlliance();
    private boolean lastIsAuto = DriverStation.isAutonomous();
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
     * @param field               The field json object to use
     * @param pointSpacing        The distance between points on the straightaways
     * @param cornerPointSpacing  The distance between points on the corners
     * @param cornerDist          How far back along the straightaway to dedicate to
     *                            corners
     * @param clearance           The clearance to use when inflating obstacles
     * @param cornerSplitPercent  How far back along the straightaway to dedicate to
     *                            a corner when the straightaway is too small to fit
     *                            both corners (percentage, should be less than 0.5)
     * @param injectPoints        Whether or not to inject points on straightaways
     * @param normalizeCorners    Whether or not to normalize distance between
     *                            corner points
     * @param searchAlgorithmType The search algorithm to use
     * @param profiling           Whether or not to profile the pathfinding process
     * @param endgameTime         The time in seconds when the robot should start to
     *                            consider endgame obstacles
     */
    public Pathfinder(FieldData field, double pointSpacing, double cornerPointSpacing, double cornerDist,
            double clearance, double cornerSplitPercent, boolean injectPoints, boolean normalizeCorners,
            SearchAlgorithmType searchAlgorithmType, boolean profiling, double endgameTime) {
        this.pointSpacing = pointSpacing;
        this.cornerPointSpacing = cornerPointSpacing;
        this.cornerDist = cornerDist;
        this.clearance = clearance;
        this.cornerSplitPercent = cornerSplitPercent;
        this.injectPoints = injectPoints;
        this.normalizeCorners = normalizeCorners;
        this.searchAlgorithmType = searchAlgorithmType;
        this.profiling = profiling;
        Pathfinder.endgameTime = endgameTime;

        // This is essentially a vertex and edge table, with some extra information.
        // Vertices are stored as an array [x, y]

        // Process the Double[] into Vertex objects
        for (int i = 0; i < field.vertices.size(); i++) {
            Double[] vertex = field.vertices.get(i);
            obstacleVertices.add(new Vertex(vertex[0], vertex[1]));
            uninflatedObstacleVertices.add(new Vertex(vertex[0], vertex[1]));
        }
        // Process the edges into Edge objects and make Obstacle objects
        for (int i = 0; i < field.obstacles.size(); i++) {
            ObstacleData obstacle = field.obstacles.get(i);
            ArrayList<Edge> curEdges = new ArrayList<Edge>();
            for (int x = 0; x < obstacle.edges.size(); x++) {
                Integer[] edgeRaw = obstacle.edges.get(x);
                edges.add(new Edge(edgeRaw[0], edgeRaw[1], obstacle.modifiers));
                curEdges.add(new Edge(edgeRaw[0], edgeRaw[1], obstacle.modifiers));
            }
            Obstacle newObs = new Obstacle(obstacleVertices, curEdges, obstacle.id, obstacle.modifiers);
            obstacles.add(newObs);
        }

        // Create the map object
        map = new Map(obstacles, obstacleVertices, edges, clearance, field.fieldX, field.fieldY);

        for (Obstacle obs : obstacles) {
            obs.initialize(map.getPathVerticesStatic());
            obs.modifiers.invalidateCache();
        }
    }

    /**
     * Updates the modifier cache based on data from the driver station and wpilib.
     * Should be called in robotPeriodic.
     */
    public void periodic() {
        boolean shouldInvalidate = false;
        if (DriverStation.getMatchTime() < endgameTime && !(lastMatchTime < endgameTime)) {
            shouldInvalidate = true;
        }
        lastMatchTime = DriverStation.getMatchTime();
        Optional<Alliance> alliance = DriverStation.getAlliance();
        if (!alliance.equals(lastAlliance)) {
            lastAlliance = alliance;
            shouldInvalidate = true;
        }
        boolean isAuto = DriverStation.isAutonomous();
        if (isAuto != lastIsAuto) {
            lastIsAuto = isAuto;
            shouldInvalidate = true;
        }
        if (shouldInvalidate) {
            for (Obstacle obs : obstacles) {
                obs.modifiers.invalidateCache();
                obs.iHateProgramming();
            }
            map.calculateStaticNeighbors();
        }
    }

    /**
     * Snaps the start and target vertices according to the snap mode, calculates
     * visibility graph for dynamic elements, and generates the best path.
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
        return generatePathInner(start, target, snapMode, dynamicVertices, true);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles and generates
     * the best path.
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
        return generatePathInner(start, target, PathfindSnapMode.SNAP_ALL, new ArrayList<Vertex>(), true);
    }

    /**
     * Snaps the start and target poses to be outside of obstacles and generates
     * the best path.
     * Defaults to PathfindSnapMode.SNAP_ALL
     * 
     * @param start  The starting poses
     * @param target The target poses
     * 
     * @return The shortest path from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Pose2d start, Pose2d target) throws ImpossiblePathException {
        return generatePathInner(new Vertex(start), new Vertex(target), PathfindSnapMode.SNAP_ALL,
                new ArrayList<Vertex>(), true);
    }

    /**
     * Snaps the start and target vertices according to the snap mode and generates
     * the best path.
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
        return generatePathInner(start, target, snapMode, new ArrayList<Vertex>(), true);
    }

    /**
     * Snaps the start and target poses according to the snap mode and generates
     * the best path.
     * 
     * @param start    The starting pose
     * @param target   The target pose
     * @param snapMode The snap mode to use
     * 
     * @return The shortest path from the starting pose to the target pose that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Pose2d start, Pose2d target, PathfindSnapMode snapMode) throws ImpossiblePathException {
        return generatePathInner(new Vertex(start), new Vertex(target), snapMode, new ArrayList<Vertex>(), true);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles and generates
     * the best path that passes through all waypoints.
     * Defaults to PathfindSnapMode.SNAP_ALL
     * 
     * @param start  The starting pose
     * @param target The target waypoints
     * 
     * @return A trajectory from the starting vertex passing through all waypoints
     *         that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Pose2d start, ArrayList<Pose2d> target)
            throws ImpossiblePathException {
        return generatePathInner(new Vertex(start), Vertex.fromPose2dArray(target), PathfindSnapMode.SNAP_ALL,
                new ArrayList<Vertex>());
    }

    /**
     * Snaps the start and target vertices according to the snap mode and generates
     * the best path a wpilib trajectory.
     * 
     * @param start    The starting pose
     * @param target   The target pose
     * @param snapMode The snap mode to use
     * @param config   The trajectory config to use when generating the trajectory
     * 
     * @return A trajectory from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Trajectory generateTrajectory(Pose2d start, Pose2d target, PathfindSnapMode snapMode,
            TrajectoryConfig config)
            throws ImpossiblePathException {
        Path path = generatePathInner(new Vertex(start), new Vertex(target), snapMode,
                new ArrayList<Vertex>(), true);
        return path.asTrajectory(config);
    }

    /**
     * Snaps the start and target vertices according to the snap mode, calculates
     * visibility graph for dynamic elements, and generates the best path as a
     * wpilib trajectory.
     * 
     * @param start           The starting pose
     * @param target          The target pose
     * @param snapMode        The snap mode to use
     * @param dynamicVertices An ArrayList of dynamic vertices
     * @param config          The trajectory config to use when generating the
     *                        trajectory
     * 
     * @return A trajectory from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Trajectory generateTrajectory(Pose2d start, Pose2d target, PathfindSnapMode snapMode,
            ArrayList<Vertex> dynamicVertices,
            TrajectoryConfig config)
            throws ImpossiblePathException {
        Path path = generatePathInner(new Vertex(start), new Vertex(target), snapMode, dynamicVertices, true);
        return path.asTrajectory(config);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles and generates
     * the best path as a wpilib trajectory.
     * Defaults to PathfindSnapMode.SNAP_ALL
     * 
     * @param start  The starting pose
     * @param target The target pose
     * @param config The trajectory config to use when generating the trajectory
     * 
     * @return A trajectory from the starting vertex to the target vertex that
     *         does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Trajectory generateTrajectory(Pose2d start, Pose2d target, TrajectoryConfig config)
            throws ImpossiblePathException {
        Path path = generatePathInner(new Vertex(start), new Vertex(target), PathfindSnapMode.SNAP_ALL,
                new ArrayList<Vertex>(), true);
        return path.asTrajectory(config);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles and generates
     * the best path that passes through all waypoints as a wpilib trajectory.
     * Defaults to PathfindSnapMode.SNAP_ALL
     * 
     * @param start  The starting pose
     * @param target The target pose
     * @param config The trajectory config to use when generating the trajectory
     * 
     * @return A trajectory from the starting vertex passing through all waypoints
     *         that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Trajectory generateTrajectory(Pose2d start, ArrayList<Pose2d> target, TrajectoryConfig config)
            throws ImpossiblePathException {
        Path path = generatePathInner(new Vertex(start), Vertex.fromPose2dArray(target), PathfindSnapMode.SNAP_ALL,
                new ArrayList<Vertex>());
        return path.asTrajectory(config);
    }

    /**
     * Snaps the start and target vertices accoridng to the snap mode
     * the best path that passes through all waypoints as a wpilib trajectory.
     * 
     * @param start    The starting pose
     * @param target   The target pose
     * @param snapMode The snap mode to use
     * @param config   The trajectory config to use when generating the
     *                 trajectory
     * 
     * @return A trajectory from the starting vertex passing through all waypoints
     *         that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Trajectory generateTrajectory(Pose2d start, ArrayList<Pose2d> target, PathfindSnapMode snapMode,
            TrajectoryConfig config)
            throws ImpossiblePathException {
        Path path = generatePathInner(new Vertex(start), Vertex.fromPose2dArray(target), snapMode,
                new ArrayList<Vertex>());
        return path.asTrajectory(config);
    }

    private Path generatePathInner(Vertex start, ArrayList<Vertex> waypoints, PathfindSnapMode snapMode,
            ArrayList<Vertex> dynamicVertices) throws ImpossiblePathException {
        Path path = generatePathInner(start, waypoints.get(0), snapMode, dynamicVertices, false);
        for (int i = 1; i < waypoints.size(); i++) {
            path.addPath(generatePathInner(waypoints.get(i - 1), waypoints.get(i), snapMode, dynamicVertices, false));
        }
        path.processPath(snapMode);
        return path;
    }

    // Using an inner function because java handles optional parameters poorly
    private Path generatePathInner(Vertex start, Vertex target, PathfindSnapMode snapMode,
            ArrayList<Vertex> dynamicVertices, boolean processPath) throws ImpossiblePathException {
        long startTime = System.nanoTime();
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
        // long snapEndTime = System.nanoTime();

        ArrayList<Vertex> additionalVertexs = new ArrayList<>();
        additionalVertexs.add(start);
        additionalVertexs.add(target);
        additionalVertexs.addAll(dynamicVertices);
        map.calculateDynamicNeighbors(additionalVertexs, true);

        // long visibilityEndTime = System.nanoTime();

        SearchAlgorithm searcher;
        if (searchAlgorithmType == SearchAlgorithmType.ASTAR) {
            searcher = new Astar(this);
        } else {
            throw new RuntimeException("Invalid search algorithm type");
        }
        // This could throw ImpossiblePathException
        Path path = searcher.run(start, target);

        // long searchEndTime = System.nanoTime();

        path.setUnsnappedTarget(unsnappedTarget);
        if (processPath)
            path.processPath(snapMode);
        if (profiling) {
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            double pathGenTime = totalTime / 1000000.0;
            SmartDashboard.putNumber("Pathfinding Time (ms)", pathGenTime);
        }
        // long snapTime = snapEndTime - startTime;
        // long visibilityTime = visibilityEndTime - snapEndTime;
        // long searchTime = searchEndTime - visibilityEndTime;
        // long processPathTime = endTime - searchEndTime;
        // System.out.println("Snapping time: " + snapTime / 1000000.0 + "ms ("
        // + Math.round((snapTime / (double) totalTime) * 100) + "%)");
        // System.out.println("Visibility graph time: " + visibilityTime / 1000000.0 +
        // "ms ("
        // + Math.round((visibilityTime / (double) totalTime) * 100) + "%)");
        // System.out.println(
        // "Search time: " + searchTime / 1000000.0 + "ms (" + Math.round((searchTime /
        // (double) totalTime) * 100)
        // + "%)");
        // System.out.println("Path processing time: " + processPathTime / 1000000.0 +
        // "ms ("
        // + Math.round((processPathTime / (double) totalTime) * 100) + "%)");

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
        targetObs.removeIf(obs -> obs.modifiers.hasModifier(ObstacleModifierTypes.ZONE_MODIFIER));
        Vertex tempNearestVertex = point;
        int i = 0;
        while (targetObs.size() > 0) {
            if (i > 10) {
                throw new ImpossiblePathException("Failed to snap point " + point);
            }
            for (Obstacle obs : targetObs) {
                tempNearestVertex = obs.calculateNearestPointFromInside(tempNearestVertex);
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
        if (map.getNeighbors() != null)
            list.addAll(map.getNeighbors());
        if (map.getNeighborsStatic() != null)
            list.addAll(map.getNeighborsStatic());
        return list;
    }

    /**
     * Intended for use with our custom advantagescope fork for debugging the
     * visibility graph.
     * 
     * @return An arraylist containing vertices to represent all vertices in the
     *         visibility graph. This is the same as visualizeVertices, but
     *         includes dynamic vertices like the start and end points.
     */
    public ArrayList<Vertex> visualizePathVertices() {
        ArrayList<Vertex> list = new ArrayList<>();
        if (map.getPathVertices() != null)
            list.addAll(map.getPathVertices());
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
        list.addAll(map.getPathVerticesStatic());
        return list;
    }

    /**
     * Space between injected points on straightaways in the path (meters)
     * 
     * @return The space between injected points on straightaways (meters)
     */
    public double getPointSpacing() {
        return pointSpacing;
    };

    /**
     * Space between points on corners of the path (percent of the curve length)
     * 
     * @return The space between points on corners of the path (percent of the curve
     */
    public double getCornerPointSpacing() {
        return cornerPointSpacing;
    };

    /**
     * How far back along the straightaway to dedicate to making corners
     * 
     * @return The distance back along the straightaway dedicated to making corners
     */
    public double getCornerDist() {
        return cornerDist;
    };

    /**
     * How far to inflate obstacles
     * 
     * @return The clearance to use when inflating obstacles
     */
    public final double clearance() {
        return clearance;
    };

    /**
     * Whether or not to inject points on straightaways
     * 
     * @return Whether or not to inject points on straightaways
     */
    public boolean getInjectPoints() {
        return injectPoints;
    };

    /**
     * Whether or not to normalize distance between corner points
     * 
     * @return Whether or not to normalize distance between corner points
     */
    public boolean getNormalizeCorners() {
        return normalizeCorners;
    };

    /**
     * How far each corner should move towards the
     * other point if the distance is too short to allow both corners the full
     * corner distance
     * 
     * @return The corner split percentage
     */
    public double getCornerSplitPercent() {
        return cornerSplitPercent;
    };

    /**
     * The search algorithm to use
     * 
     * @return The search algorithm to use
     */
    public SearchAlgorithmType getSearchAlgorithmType() {
        return searchAlgorithmType;
    };

    /**
     * Whether or not to profile the pathfinding process
     * 
     * @return Whether or not to profile the pathfinding process
     */
    public boolean getProfiling() {
        return profiling;
    };

    /**
     * Time in seconds when the robot should start to consider endgame obstacles.
     * 
     * @return The time in seconds when the robot should start to consider endgame
     */
    public static double getEndgameTime() {
        return endgameTime;
    }

    /**
     * Space between injected points on straightaways in the path (meters)
     * 
     * @param newPointSpacing The new space between injected points on straightaways
     *                        (meters)
     */
    public void setPointSpacing(double newPointSpacing) {
        if (pointSpacing <= 0)
            throw new IllegalArgumentException("Point spacing must be greater than 0");
        pointSpacing = newPointSpacing;
    };

    /**
     * Space between points on corners of the path (percent of the curve length)
     * 
     * @param newCornerPointSpacing The new space between points on corners of the
     *                              path (percent of the curve length)
     */
    public void setCornerPointSpacing(double newCornerPointSpacing) {
        if (cornerPointSpacing <= 0)
            throw new IllegalArgumentException("Corner point spacing must be greater than 0");
        cornerPointSpacing = newCornerPointSpacing;
    };

    /**
     * How far back along the straightaway to dedicate to making corners
     * 
     * @param newCornerDist The new distance back along the straightaway dedicated
     *                      to making corners
     */
    public void setCornerDist(double newCornerDist) {
        if (cornerDist < 0)
            throw new IllegalArgumentException("Corner distance must be positive");
        cornerDist = newCornerDist;
    };

    /**
     * Whether or not to inject points on straightaways
     * 
     * @param newInjectPoints Whether or not to inject points on straightaways
     */
    public void setInjectPoints(boolean newInjectPoints) {
        injectPoints = newInjectPoints;
    };

    /**
     * Whether or not to normalize distance between corner points
     * 
     * @param newNormalizeCorners Whether or not to normalize distance between
     *                            corner points
     */
    public void setNormalizeCorners(boolean newNormalizeCorners) {
        normalizeCorners = newNormalizeCorners;
    };

    /**
     * How far each corner should move towards the other point if the distance is
     * too short to allow both corners the full corner distance
     * 
     * @param newCornerSplitPercent The new corner split percentage
     */
    public void setCornerSplitPercent(double newCornerSplitPercent) {
        if (newCornerSplitPercent > 0.5)
            throw new IllegalArgumentException("Corner split percent must be less than or equal to 0.5");
        cornerSplitPercent = newCornerSplitPercent;
    };

    /**
     * The search algorithm to use
     * 
     * @param newSearchAlgorithm The new search algorithm to use
     */
    public void setSearchAlgorithmType(SearchAlgorithmType newSearchAlgorithm) {
        searchAlgorithmType = newSearchAlgorithm;
    };

    /**
     * Whether or not to profile the pathfinding process
     * 
     * @param newProfiling Whether or not to profile the pathfinding process
     */
    public void setProfiling(boolean newProfiling) {
        profiling = newProfiling;
    };

    /**
     * Time in seconds when the robot should start to consider endgame obstacles.
     * 
     * @param newEndgameTime The new time in seconds when the robot should start to
     *                       consider endgame obstacles
     */
    public static void setEndgameTime(double newEndgameTime) {
        endgameTime = newEndgameTime;
    }
}
