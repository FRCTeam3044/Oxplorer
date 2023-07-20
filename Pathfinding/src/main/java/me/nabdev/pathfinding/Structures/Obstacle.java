package me.nabdev.pathfinding.Structures;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;

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

    public void setScoringVertex(boolean isScoringVertex){
        this.isScoringNode = isScoringVertex;
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

    public Vertex calculateNearestPoint(Vertex v){
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
    public ArrayList<Pose2d> asPose2dList(){
        ArrayList<Pose2d> poses = new ArrayList<>();
        for(Edge edge : edges){
            poses.add(verticies.get(edge.vertexOne).asPose2d());
        }
        return poses;
    }

    public static ArrayList<Obstacle> isRobotInObstacle(ArrayList<Obstacle> obstacles, Vertex vertex){
        ArrayList<Obstacle> inside = new ArrayList<Obstacle>();
        for(Obstacle obs : obstacles){
            if(obs.isInside(vertex)){
                inside.add(obs);
            }
        }
        return inside;
    }
}