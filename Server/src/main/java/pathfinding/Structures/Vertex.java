package pathfinding.Structures;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

// Helper class to neaten up the actual calculations.
public class Vertex extends OrderedPair {
    public Rotation2d rotation;
    
    public Vertex(Pose2d pose){
        super(pose.getX(), pose.getY());
        rotation = pose.getRotation();
    }

    public Vertex(double _x, double _y){
        super(_x, _y);
        rotation = new Rotation2d(0);
    }
    public Vertex(double _x, double _y, Rotation2d rot){
        super(_x, _y);
        rotation = rot;
    }

    public Vector createVector(Vertex starting){
        if(starting == null){
            System.out.println("Warning: Vector creation passed null starting point");
            return new Vector(0,0);
        }
        return new Vector(x - starting.x, y - starting.y);
    }

    public Vertex moveByVector(Vector vector){
        if(vector == null){
            System.out.println("Warning: Move by vector passed null vector");
            return this;
        }
        return new Vertex(x + vector.x, y + vector.y);
    }

    public Vertex average(Vertex point){
        if(point == null){
            System.out.println("Warning: Vertex average passed null point");
            return this;
        }
        return new Vertex((x + point.x) / 2, (y + point.y) / 2);
    }

    public Vertex subtract(Vertex v2){
        if(v2 == null){
            System.out.println("Warning: Vertex subtraction passed null point");
            return this;
        }
        return new Vertex(x - v2.x, y - v2.y);
    }

    // Pythagorean theorem
    public double distance(Vertex target){
        double xDist = x - target.x;
        double yDist = y - target.y;
        return Math.sqrt(xDist*xDist + yDist*yDist);
    }

    public double distance(Pose2d target){
        double xDist = x - target.getX();
        double yDist = y - target.getY();
        return Math.sqrt(xDist*xDist + yDist*yDist);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex compare = (Vertex)o;

        return compare.x == this.x && compare.y == this.y;
    }

    public Vertex moveByTwoVectors(Vector vect1, Vector vect2){
        Vertex pos1 = this.moveByVector(vect1);
        Vertex pos2 = this.moveByVector(vect2);
        return pos1.average(pos2);
    }
}
