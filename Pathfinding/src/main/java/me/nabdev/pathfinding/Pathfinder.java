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

import edu.wpi.first.math.geometry.Pose2d;

/**
 * The main pathfinder class, and the only one you should need to interact with.
 */
public class Pathfinder
{
    // Every obstacle verticy (ORDER IS IMPORTANT)
    ArrayList<Vertex> obstacleVerticies = new ArrayList<> ();
    // An edge table that has the indexes of connected verticies
    ArrayList<Edge> edges = new ArrayList<>();

    ArrayList<Obstacle> obstacles = new ArrayList<>();

    public Map map;

    /**
     * Create a new pathfinder. Should only be done once, at the start of the program.
     * @param fieldType The field to load
     */
    public Pathfinder(Field fieldType) {

        JSONObject field = FieldLoader.loadField(fieldType);
        // This is essentially a vertex and edge table, with some extra information.
        // Verticies are stored as an array [x, y]
        JSONArray verticiesRaw = field.getJSONArray("verticies");
        /*
         * Obstacles are stored as an array of 4 edges, each edge being an array of 2 verticies.
         * The verticies are stored as the index of the vertex in the vertex array.
         * Example:
         * [
         *    [0,1],
         *    [1,2],
         *    [2,3],
         *    [3,0]
         * ]
         * This is so that we can detect when the robot or target is inside an obstacle and snap it. 
         * Obstacles MUST be convex, and the verticies MUST be in clockwise order.
         */
        JSONArray obstaclesRaw = field.getJSONArray("obstacles");

        // Convert from JSONArray to arraylist to make math easier
        for(int i=0; i<verticiesRaw.length(); i++){
            JSONArray vertex = verticiesRaw.getJSONArray(i);
            obstacleVerticies.add(new Vertex(vertex.getDouble(0),vertex.getDouble(1)));
        }
        for(int i=0; i<obstaclesRaw.length(); i++){
            JSONArray obstacle = obstaclesRaw.getJSONArray(i);
            for(int x=0; x<4; x++){
                JSONArray edgeRaw = obstacle.getJSONArray(x);
                edges.add(new Edge(edgeRaw.getInt(0),edgeRaw.getInt(1)));
            }
        }

        // Create the map object
        map = new Map(obstacleVerticies, edges, PathfindingConfig.clearance);

        // Unfortunatley, a seconds iteration is required. This is because we need the path verticies to be created before we can create the obstacles.
        // If you can think of a better way to do this, please let me know.
        for(int i=0; i<obstaclesRaw.length(); i++){
            JSONArray obstacle = obstaclesRaw.getJSONArray(i);
            // Edges are expected to have four points, in clockwise order
            // Technically this is not required, and may be changed for future field designs
            Edge[] curEdges = new Edge[4];
            for(int x=0; x<4; x++){
                JSONArray edgeRaw = obstacle.getJSONArray(x);
                curEdges[x] = new Edge(edgeRaw.getInt(0),edgeRaw.getInt(1));
            }
            Obstacle newObs = new Obstacle(map.pathVerticiesStatic, curEdges);
            // TODO: This whole thing is a mess, needs to be completely redone
            newObs.setScoringVertex((i == 1 || i == 5));
            obstacles.add(newObs);
        }
    }


    /**
     * Snaps the start and target vertices to be outside of obstacles, calculates dynamic neighbors, and generates the best path using A-star algorithm.
     * 
     * @param start The starting vertex
     * @param target The target vertex
     * @param snapMode The snap mode to use
     * @param dynamicVerticies An ArrayList of dynamic vertices
     * 
     * @return The shortest path from the starting vertex to the target vertex that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target, PathfindSnapMode snapMode, ArrayList<Vertex> dynamicVerticies) throws ImpossiblePathException {
        return generatePathInner(start, target, snapMode, dynamicVerticies);
    }

    /**
     * Snaps the start and target vertices to be outside of obstacles, calculates dynamic neighbors, and generates the best path using A-star algorithm.
     * 
     * @param start The starting vertex
     * @param target The target vertex
     * 
     * @return The shortest path from the starting vertex to the target vertex that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target) throws ImpossiblePathException {
        return generatePathInner(start, target, PathfindSnapMode.SNAP_ALL, new ArrayList<Vertex>());
    }

        /**
     * Snaps the start and target vertices to be outside of obstacles, calculates dynamic neighbors, and generates the best path using A-star algorithm.
     * 
     * @param start The starting vertex
     * @param target The target vertex
     * @param snapMode The snap mode to use
     * 
     * @return The shortest path from the starting vertex to the target vertex that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath(Vertex start, Vertex target, PathfindSnapMode snapMode) throws ImpossiblePathException {
        return generatePathInner(start, target, snapMode, new ArrayList<Vertex>());
    }

    private Path generatePathInner(Vertex start, Vertex target, PathfindSnapMode snapMode, ArrayList<Vertex> dynamicVerticies) throws ImpossiblePathException {
        // Snapping is done because the center of the robot can be inside of the inflated obstacle edges
        // In the case where this happened the start needs to be snapped outside otherwise a* will fail
        Vertex unsnappedTarget = target;
        if(snapMode == PathfindSnapMode.SNAP_ALL || snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_START){
            start = snap(start);
        }

        if(snapMode == PathfindSnapMode.SNAP_ALL || snapMode == PathfindSnapMode.SNAP_TARGET){
            target = snap(target);
        } else if (snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE){
            target = snap(target);
        }
        
        // if(snapMode == PathfindSnapMode.SNAP || snapMode == PathfindSnapMode.SNAP_THEN_POINT || snapToScoringNodes){
        //     target = snap(unsnappedTarget, snapToScoringNodes);
        //     System.out.println("Snapped target (iter 1): " + target.print());
        //     if(snapToScoringNodes){
        //         unsnappedTarget = target;
        //         target = snap(unsnappedTarget, false);
        //         System.out.println("Snapped target (iter 2): " + target.print());
        //     }
        // }

        // Time the pathfinding
        long startTime = System.nanoTime();

        ArrayList<Vertex> additionalVertexs = new ArrayList<>();
        additionalVertexs.add(start);
        additionalVertexs.add(target);
        additionalVertexs.addAll(dynamicVerticies);
        map.calculateDynamicNeighbors(additionalVertexs, true);

        Astar astar = new Astar(map);
        // Solve for the best path using a-star. Chargepad handling is temporary and will be replaced with a cleaner solution.
        Path path = astar.run(start, target);
        if(path == null){
            throw new ImpossiblePathException("No possible solutions");
        }
        if(snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE){
            path.setUnsnappedTarget(unsnappedTarget);
        }
        path.processPath();
        long endTime = System.nanoTime();
        System.out.println("Path generation time: " + (endTime - startTime)/1000000 + "ms");

        return path;
    }

    /**
     * Snap a vertex to the nearest obstacle edge if it's inside of one
     * @param point Point to snap
     * @return
     */
    private Vertex snap(Vertex point) throws ImpossiblePathException {
        ArrayList<Obstacle> targetObs = Obstacle.isRobotInObstacle(obstacles, point);
        Vertex tempNearestVertex = point;
        int i = 0;
        while(targetObs.size() > 0){
            if(i > 10){
                throw new ImpossiblePathException("Failed to snap point " + point.print());
            }
            for(Obstacle obs : targetObs){
                tempNearestVertex = obs.calculateNearestPoint(tempNearestVertex);
            }
            targetObs = Obstacle.isRobotInObstacle(obstacles, tempNearestVertex);
            i++;
        }
        return tempNearestVertex;
    }

    /**
     * Determines how verticies will be snapped to the nearest obstacle edge if they are inside of an obstacle. 
     * Snapping is useful in case the robot center is inside of the inflated obstacle verticies.
     */
    public enum PathfindSnapMode {
        /**
         * No snapping. If a the start or target is inside of an obstacle, an ImpossiblePathException will be thrown.
         */
        NONE, 
        /**
         * Snap start and target verticies
         */
        SNAP_ALL, 
        /**
         * Snap start and target verticies. If the target is inside an obstacle, draw a straight line from the snapped target to the original target to drive there anyways
         */
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
         * If the target is inside an obstacle, snap it and draw a straight line from the snapped target to the original target to drive there anyways.
         */
        SNAP_TARGET_THEN_LINE

    }
    /**
     * It's a bit messy, but it's used if you want to see all of the obstacles you mapped in the sim field.
     * @return An arraylist containing an arraylist of pose2ds for each obstacle. Each pose2d represents one vertex of the obstacle.
     */
    public ArrayList<ArrayList<Pose2d>> visualizeField(){
        ArrayList<ArrayList<Pose2d>> listList = new ArrayList<>();
        for(int i = 0; i < obstacles.size(); i++){
            listList.add(obstacles.get(i).asPose2dList());
        }
        return listList;
    }
}
