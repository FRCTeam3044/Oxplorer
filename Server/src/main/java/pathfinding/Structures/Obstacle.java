package pathfinding.Structures;

import java.util.ArrayList;

public class Obstacle {
    private ArrayList<Vertex> verticies;

    public Edge[] edges;

    private Vector[] vectors = new Vector[4];
    
    public boolean isScoringNode = false;
    public Obstacle(ArrayList<Vertex> verticies, Edge[] edges){
        this.edges = edges;
        this.verticies = verticies;
        for(int i = 0; i < 4; i++){
            Edge edge = edges[i];
            vectors[i] = verticies.get(edge.vertexOne).createVector(verticies.get(edge.vertexTwo));
        }
    }

    public void setScoringNode(boolean isScoringNode){
        this.isScoringNode = isScoringNode;
    }

    public boolean isInside(Vertex pos){
        // Check if the given vertex lays inside of this obstacle using the dot product to check if the point is on the same side of all edges.
        boolean positive = false;
        boolean negative = false;
        for(int i = 0; i < 4; i++){
            Vector vector = vectors[i];
            Vertex vertex = verticies.get(edges[i].vertexOne);
            Vector pointVector = vertex.createVector(pos);
            if(vector.dotProduct(pointVector) < 0){
                if(positive) return false;
                negative = true;
            } else {
                if(negative) return false;
                positive = true;
            }
        }
        return true;
    }

    public Vertex calculateNearestPoint(Vertex v, boolean snapToScoringNodes, boolean isRed){
        if(snapToScoringNodes){
            Edge edge = edges[isRed ? 2 : 0];
            Vertex vertexOne = verticies.get(edge.vertexOne);
            Vector vect = v.createVector(vertexOne);
            Vector edgeVector = verticies.get(edge.vertexTwo).createVector(vertexOne);
            double dot = vect.dotProduct(edgeVector.normalize());
            return vertexOne.moveByVector(edgeVector.normalize().scale(dot));
        }
        double[] distances = new double[4];
        Vertex[] closestPoints = new Vertex[4];
        for(int i = 0; i < 4; i++){
            Edge edge = edges[i];
            Vertex vertexOne = verticies.get(edge.vertexOne);
            Vector vect = v.createVector(vertexOne);
            Vector edgeVector = verticies.get(edge.vertexTwo).createVector(vertexOne);
            double dot = vect.dotProduct(edgeVector.normalize());
            closestPoints[i] = vertexOne.moveByVector(edgeVector.normalize().scale(dot));
            distances[i] = v.distance(closestPoints[i]);
        }
        int lowest = 0;
        for(int i = 1; i < 4; i++){
            if(distances[i] < distances[lowest]){
                lowest = i;
            }
        }
        
        return closestPoints[lowest];
    }
    public static ArrayList<Obstacle> isRobotInObstacle(ArrayList<Obstacle> obstacles, Vertex vertex, boolean isScoringNodeMode){
        ArrayList<Obstacle> inside = new ArrayList<Obstacle>();
        for(Obstacle obs : obstacles){
            if(isScoringNodeMode && !obs.isScoringNode) continue;
            if(obs.isInside(vertex)){
                inside.add(obs);
            }
        }
        return inside;
    }
}