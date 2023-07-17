package me.nabdev.pathfinding;


import me.nabdev.pathfinding.Structures.Edge;
import me.nabdev.pathfinding.Structures.ImpossiblePathException;
import me.nabdev.pathfinding.Structures.Vertex;
import me.nabdev.pathfinding.Structures.Obstacle;
import me.nabdev.pathfinding.Structures.Path;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pathfinder
{

    ArrayList<Vertex> obstacleVerticies = new ArrayList<> ();
    ArrayList<Edge> edges = new ArrayList<>();
    ArrayList<Obstacle> obstacles = new ArrayList<>();

    public Map map;

    /**
     * Create a new pathfinder. Should only be done once, at the start of the program.
     * @param field The field JSON object
     */
    public Pathfinder(double clearance, JSONObject field) {
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
        map = new Map(obstacleVerticies, edges, clearance);

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
     * Generates a path from the starting Vertex to the target Vertex
     * 
     * @param start The starting Vertex
     * @param target The target Vertex
     * @param snapMode The snap mode to use
     * 
     * @return The shortest path from the starting Vertex to the target Vertex that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath (Vertex start, Vertex target, PathfindSnapMode snapMode, boolean isRed, ArrayList<Vertex> dynamicVerticies) throws ImpossiblePathException
    {
        long snapStartTime = System.nanoTime();
        // Snapping start and target to be outside of obstacles
        start = snap(start, false, isRed);
        System.out.println("Snapped start: " + start.print());
        boolean snapToScoringVertexs = (snapMode == PathfindSnapMode.SNAP_TO_SCORING_VertexS);
        Vertex unsnappedTarget = target;
        if(snapMode == PathfindSnapMode.SNAP || snapMode == PathfindSnapMode.SNAP_THEN_POINT || snapToScoringVertexs){
            target = snap(unsnappedTarget, snapToScoringVertexs, isRed);
            System.out.println("Snapped target (iter 1): " + target.print());
            if(snapToScoringVertexs){
                unsnappedTarget = target;
                target = snap(unsnappedTarget, false, isRed);
                System.out.println("Snapped target (iter 2): " + target.print());
            }
        }
        long snapEndTime = System.nanoTime();
        System.out.println("Snap time: " + (snapEndTime - snapStartTime) / 1000000.0 + "ms");

        // Time the pathfinding
        long startTime = System.nanoTime();

        ArrayList<Vertex> additionalVertexs = new ArrayList<>();
        additionalVertexs.add(start);
        additionalVertexs.add(target);
        additionalVertexs.addAll(dynamicVerticies);
        map.calculateDynamicNeighbors(additionalVertexs, true);

        // Debug code - Sends all the edges of path verticies via smartdashboard
        // SmartDashboard.putNumber("Field/LineOfSightCount", map.neighbors.size());
        // for(int i = 0; i < map.neighbors.size(); i++){
        //     SmartDashboard.putNumberArray("Field/LineOfSight" + i, new double[]{map.pathVerticies.get(map.neighbors.get(i).vertexOne).x, map.pathVerticies.get(map.neighbors.get(i).vertexOne).y, map.pathVerticies.get(map.neighbors.get(i).vertexTwo).x, map.pathVerticies.get(map.neighbors.get(i).vertexTwo).y});
        // }
        Astar astar = new Astar(map);
        // Solve for the best path using a-star. Chargepad handling is temporary and will be replaced with a cleaner solution.
        Path path = astar.run(start, target);
        if(path == null){
            throw new ImpossiblePathException("No possible solutions");
        }
        path.pursuitPrepare();
        path.unsnappedTarget = unsnappedTarget;
        long endTime = System.nanoTime();
        System.out.println("Path generation time: " + (endTime - startTime)/1000000 + "ms");
        
        return path;
    }

    private Vertex snap(Vertex point, boolean snapToScoringVertexs, boolean isRed){
        ArrayList<Obstacle> targetObs = Obstacle.isRobotInObstacle(obstacles, point, snapToScoringVertexs);
        if(targetObs.size() == 0) return point;
        Vertex tempNearestVertex = point;
        for(Obstacle obs : targetObs){
            tempNearestVertex = obs.calculateNearestPoint(tempNearestVertex, snapToScoringVertexs, isRed);
        }
        return tempNearestVertex;
    }

    public enum PathfindSnapMode {
        NONE, SNAP, SNAP_THEN_POINT, SNAP_TO_SCORING_VertexS
    }
}
