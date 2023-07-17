package pathfinding;

import java.util.ArrayList;

import pathfinding.Structures.Edge;
import pathfinding.Structures.Node;
import pathfinding.Structures.Vector;
import pathfinding.Structures.Vertex;

public class Map {
    public ArrayList<Vertex> obstacleVerticies;
    public ArrayList<Edge> obstacleEdges;

    public ArrayList<Node> pathVerticiesStatic = new ArrayList<>();
    public ArrayList<Node> pathVerticies;

    public ArrayList<Edge> neighborsStatic = new ArrayList<>();
    public ArrayList<Edge> neighbors;

    public final double fieldx = 16.5;
    public final double fieldy = 8;
    public final double eps = 0.001;

    public Map(ArrayList<Vertex> obVerticies, ArrayList<Edge> obEdges, double clearance){
        obstacleEdges = obEdges;
        obstacleVerticies = obVerticies;
        // Uses vectors to make a list of points around the verticies of obstacles, offset by the clearance parameter.
        calculateStaticPathVerticies(obEdges, clearance);
        // Calculate the edges between these path verticies, so that the robot can't phase through obstacles.
        calculateStaticNeighbors();
    }
    public void calculateStaticPathVerticies(ArrayList<Edge> edges, double clearance){
        ArrayList<Vertex> inflated = new ArrayList<>();
        for(int i = 0; i<obstacleVerticies.size(); i++){
            Vertex vertex = obstacleVerticies.get(i);
            Vector vector1 = null;
            Vector vector2 = null;
            for(Edge edge : edges){
                int vertexTwo = edge.containsVertex(i);
                if(vertexTwo < 0) continue;
                Vector v = vertex.createVector(obstacleVerticies.get(vertexTwo)).normalize();
                if(vector1 == null){
                    vector1 = v;
                } else {
                    vector2 = v;
                    break;
                }
            }
            if(vector1 != null && vector2 != null){
                Vertex avg = vertex.moveByTwoVectors(vector1.scale(clearance + eps), vector2.scale(clearance + eps));
                pathVerticiesStatic.add(new Node(avg.x, avg.y));
                inflated.add(vertex.moveByTwoVectors(vector1.scale(clearance), vector2.scale(clearance)));
            } else {
                System.out.println("One point has <2 edges");
            }
        }
        obstacleVerticies.clear();
        obstacleVerticies.addAll(inflated);
    }
    public void calculateStaticNeighbors(){
        for (int cur=0; cur<pathVerticiesStatic.size(); cur++) {
            for (int i=0; i<pathVerticiesStatic.size(); i++) {
                lineOfSight(cur, i, neighborsStatic, pathVerticiesStatic);
            }
        }
    }

    public void calculateDynamicNeighbors(ArrayList<Node> additionalVerticies, boolean reset){
        if(reset || pathVerticies == null) pathVerticies = new ArrayList<>(pathVerticiesStatic);
        if(reset || neighbors == null) neighbors = new ArrayList<>(neighborsStatic);
        pathVerticies.addAll(additionalVerticies);
        for (int cur=pathVerticies.size() - additionalVerticies.size(); cur<pathVerticies.size(); cur++) {
            for (int i=0; i<pathVerticies.size(); i++) {
                lineOfSight(cur, i, neighbors, pathVerticies);
            }
        }
    }

    private void lineOfSight (int cur, int i, ArrayList<Edge> neighborArray, ArrayList<Node> pathVerticiesArray){
        if(cur == i) return;
        if(neighborArray.contains(new Edge(i, cur))) return;
        boolean intersect = false;
        for(int x = 0; x<obstacleEdges.size(); x++){
            if(Vector.dotIntersect(pathVerticiesArray.get(cur), pathVerticiesArray.get(i), obstacleVerticies.get(obstacleEdges.get(x).vertexOne), obstacleVerticies.get(obstacleEdges.get(x).vertexTwo))){
                intersect = true;
                break;
            }
        }
        if(!intersect){
            neighborArray.add(new Edge(cur, i));
        }
    }

    // Little helper function to get all nodes connected via an edge to the given node.
    public ArrayList<Node> getNeighbors(Node node){
        int index = pathVerticies.indexOf(node);
        if(index < 0){
            System.out.println("Given node not in path");
            return null;
        }
        ArrayList<Node> curNeighbors = new ArrayList<>();
        for(Edge edge : neighbors){
            int contained = edge.containsVertex(index);
            if(contained < 0) continue;
            curNeighbors.add(pathVerticies.get(contained));
        }
        return curNeighbors;
    }
}