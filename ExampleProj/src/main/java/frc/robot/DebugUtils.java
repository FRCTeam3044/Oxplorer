package frc.robot;

import java.util.ArrayList;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import me.nabdev.pathfinding.Structures.Edge;
import me.nabdev.pathfinding.Structures.Vertex;

/**
 * A little helper class for debugging pathfinding with my custom advantagescope fork
 */
public class DebugUtils {
    /**
     * Draw a line on the field
     * @param key The key to put the line under in SmartDashboard
     * @param v1 The first vertex
     * @param v2 The second vertex
     */
    public static void drawLine(String key, Vertex v1, Vertex v2){
        SmartDashboard.putNumberArray(key, new double[]{v1.x, v1.y, 0, v2.x, v2.y, 0});
    }

    /**
     * Draw a line on the field
     * @param key The key to put the line under in SmartDashboard
     * @param e The edge to draw
     * @param vertices The list of vertices to get the vertices from
     */
    public static void drawLine(String key, Edge e, ArrayList<Vertex> vertices){
        drawLine(key, vertices.get(e.getVertexOne()), vertices.get(e.getVertexTwo()));
    }

    /**
     * Draw several lines on the field
     * @param key The key to put the lines under in SmartDashboard
     * @param edges The list of edges to draw
     * @param vertices The list of vertices to get the vertices from
     */
    public static void drawLines(String key, ArrayList<Edge> edges, ArrayList<Vertex> vertices){
        double[] arr = new double[edges.size() * 6];
        for(int i = 0; i < edges.size(); i++){
            Edge e = edges.get(i);
            arr[i * 6] = vertices.get(e.getVertexOne()).x;
            arr[i * 6 + 1] = vertices.get(e.getVertexOne()).y;
            arr[i * 6 + 2] = 0;
            arr[i * 6 + 3] = vertices.get(e.getVertexTwo()).x;
            arr[i * 6 + 4] = vertices.get(e.getVertexTwo()).y;
            arr[i * 6 + 5] = 0;
        }
        SmartDashboard.putNumberArray(key, arr);
    }
    
    /**
     * Draw a point on the field
     * @param key The key to put the point under in SmartDashboard
     * @param v The vertex to draw
     */
    public static void drawPoint(String key, Vertex v){
        SmartDashboard.putNumberArray(key, new double[]{v.x, v.y, 0});
    }
    
    /**
     * Draw points on the field
     * @param key The key to put the point under in SmartDashboard
     * @param v The vertices to draw
     */
    public static void drawPoints(String key, ArrayList<Vertex> vertices){
        double[] arr = new double[vertices.size() * 3];
        for(int i = 0; i < vertices.size(); i++){
            Vertex v = vertices.get(i);
            arr[i * 3] = v.x;
            arr[i * 3 + 1] = v.y;
            arr[i * 3 + 2] = 0;
        }
        SmartDashboard.putNumberArray(key, arr);
    }
}
