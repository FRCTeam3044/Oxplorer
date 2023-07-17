package pathfinding;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import pathfinding.Structures.Edge;
import pathfinding.Structures.ImpossiblePathException;
import pathfinding.Structures.Node;
import pathfinding.Structures.Obstacle;
import pathfinding.Structures.Path;
import pathfinding.Structures.Vertex;
import java.util.ArrayList;

public class Pathfinder
{
    // Minimum distance between center of robot and obstacles
    public final static double clearance = 1.1;
    
    // Red or Blue Alliance Map; true = red, false = blue
    public boolean isRed;

    public ArrayList<Vertex> obstacleVerticies = new ArrayList<> ();
    public ArrayList<Edge> edges = new ArrayList<>();
    public ArrayList<Obstacle> obstacles = new ArrayList<>();

    public Map map;

    public Pathfinder() throws FileNotFoundException {
        JSONTokener tokener;
        try {
            FileInputStream input = new FileInputStream(getWorkingDir() + "pathfinding/field.json");
            tokener = new JSONTokener(input); 
        } catch (FileNotFoundException e) {
            System.out.println("Field data not found");
            throw e;
        }
        JSONObject field = new JSONObject(tokener);
        JSONArray obstaclesRaw = field.getJSONArray("obstacles");
        JSONArray verticiesRaw = field.getJSONArray("verticies");

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
        map = new Map(obstacleVerticies, edges, clearance);

        ArrayList<Vertex> pathVerticies = new ArrayList<>();
        for(Node node : map.pathVerticiesStatic){
            pathVerticies.add(new Vertex(node.x, node.y));
        }
        for(int i=0; i<obstaclesRaw.length(); i++){
            JSONArray obstacle = obstaclesRaw.getJSONArray(i);
            Edge[] curEdges = new Edge[4];
            for(int x=0; x<4; x++){
                JSONArray edgeRaw = obstacle.getJSONArray(x);
                curEdges[x] = new Edge(edgeRaw.getInt(0),edgeRaw.getInt(1));
            }
            Obstacle newObs = new Obstacle(pathVerticies, curEdges);
            newObs.setScoringNode((i == 1 || i == 5));
            obstacles.add(newObs);
        }
    }

    /**
     * Generates a path from the starting node to the target node
     * 
     * @param start The starting node
     * @param target The target node
     * @param snapMode The snap mode to use
     * 
     * @return The shortest path from the starting node to the target node that does not intersect any obstacles
     * 
     * @throws ImpossiblePathException If no path can be found
     */
    public Path generatePath (Node start, Node target, PathfindSnapMode snapMode, boolean isRed, ArrayList<Node> dynamicVerticies) throws ImpossiblePathException
    {
        long snapStartTime = System.nanoTime();
        // Snapping start and target to be outside of obstacles
        start = Node.fromVertex(snap(start, false, isRed));
        System.out.println("Snapped start: " + start.print());
        boolean snapToScoringNodes = (snapMode == PathfindSnapMode.SNAP_TO_SCORING_NODES);
        Vertex unsnappedTarget = target;
        if(snapMode == PathfindSnapMode.SNAP || snapMode == PathfindSnapMode.SNAP_THEN_POINT || snapToScoringNodes){
            target = Node.fromVertex(snap(unsnappedTarget, snapToScoringNodes, isRed));
            System.out.println("Snapped target (iter 1): " + target.print());
            if(snapToScoringNodes){
                unsnappedTarget = target;
                target = Node.fromVertex(snap(unsnappedTarget, false, isRed));
                System.out.println("Snapped target (iter 2): " + target.print());
            }
        }
        long snapEndTime = System.nanoTime();
        System.out.println("Snap time: " + (snapEndTime - snapStartTime) / 1000000.0 + "ms");

        // Time the pathfinding
        long startTime = System.nanoTime();

        ArrayList<Node> additionalNodes = new ArrayList<>();
        additionalNodes.add(start);
        additionalNodes.add(target);
        additionalNodes.addAll(dynamicVerticies);
        map.calculateDynamicNeighbors(additionalNodes, true);

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

    private Vertex snap(Vertex point, boolean snapToScoringNodes, boolean isRed){
        ArrayList<Obstacle> targetObs = Obstacle.isRobotInObstacle(obstacles, point, snapToScoringNodes);
        if(targetObs.size() == 0) return point;
        Vertex tempNearestNode = point;
        for(Obstacle obs : targetObs){
            tempNearestNode = obs.calculateNearestPoint(tempNearestNode, snapToScoringNodes, isRed);
        }
        return tempNearestNode;
    }

    public enum PathfindSnapMode {
        NONE, SNAP, SNAP_THEN_POINT, SNAP_TO_SCORING_NODES
    }

    private String getWorkingDir(){
        String workingDir = System.getProperty("user.dir");
        return workingDir + "/src/main/java/";
    }
}
