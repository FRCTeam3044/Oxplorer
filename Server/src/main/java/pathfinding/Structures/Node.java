package pathfinding.Structures;

import edu.wpi.first.math.geometry.Rotation2d;

// Representing a point on the field for use with the astar algorithim.
public class Node extends Vertex {
    public double G;
    // Dist from current point to target point 
    public double H;
    public double F(){
        return G + H;
    }

    public Node connection;

    public Node(double x, double y){
        super(x, y);
    }

    public Node(double x, double y, Rotation2d rot){
        super(x, y, rot);
    }

    public static Node fromVertex(Vertex v){
        return new Node(v.x, v.y, v.rotation);
    }
}
