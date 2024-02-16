package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * Represents a point in 2D space
 */
public class Vertex implements Comparable<Vertex> {
    /**
     * The x coordinate of the vertex
     */
    public double x;
    /**
     * The y coordinate of the vertex
     */
    public double y;

    /**
     * The rotation of the vertex
     */
    public Rotation2d rotation;

    /**
     * Used with A*. The distance from the start vertex to the current vertex along
     * the path
     */
    public double G;
    /**
     * Used with A*. The distance from the current vertex to the end vertex
     */
    public double H;

    /**
     * Used with A*. The sum of G and H
     * 
     * @return G + H
     */
    public double F() {
        return G + H;
    }

    /**
     * Used with A*. The previous vertex in the path, saved so that the path can be
     * traced backwards after reaching the target.
     */
    public Vertex connection;

    /**
     * All of the static neighbors of this vertex in the visibility graph
     */
    public ArrayList<Vertex> staticNeighbors = new ArrayList<Vertex>();

    /**
     * All of the dynamic neighbors of this vertex in the visibility graph
     */
    public ArrayList<Vertex> dynamicNeighbors = new ArrayList<Vertex>();

    /**
     * This will be set to false if the point is inside of an obstacle or outside of
     * the field bounds, since the robot could never be there.
     */
    public boolean validVisiblity = true;

    /**
     * Creates a new Vertex from a Pose2d
     * 
     * @param pose The Pose2d to create the Vertex from
     */
    public Vertex(Pose2d pose) {
        x = pose.getX();
        y = pose.getY();
        rotation = pose.getRotation();
    }

    /**
     * Creates a new Vertex from a Translation2d with a rotation of 0
     * 
     * @param translation The Translation2d to create the Vertex from
     */
    public Vertex(Translation2d translation) {
        x = translation.getX();
        y = translation.getY();
        rotation = new Rotation2d(0);
    }

    /**
     * Creates a new Vertex from a Translation2d with a rotation of 0
     * 
     * @param translation The Translation2d to create the Vertex from
     * @param rot         The rotation of the vertex, in degrees
     */
    public Vertex(Translation2d translation, double rot) {
        x = translation.getX();
        y = translation.getY();
        rotation = Rotation2d.fromDegrees(rot);
    }

    /**
     * Creates a new Vertex from a Translation2d with a rotation of 0
     * 
     * @param translation The Translation2d to create the Vertex from
     * @param rot         The rotation of the vertex
     */
    public Vertex(Translation2d translation, Rotation2d rot) {
        x = translation.getX();
        y = translation.getY();
        rotation = rot;
    }

    /**
     * Creates a new Vertex from an x and y coordinate, initialized with a rotation
     * of 0
     * 
     * @param _x The x coordinate of the vertex
     * @param _y The y coordinate of the vertex
     */
    public Vertex(double _x, double _y) {
        x = _x;
        y = _y;
        rotation = new Rotation2d(0);
    }

    /**
     * Creates a new Vertex from an x and y coordinate and a rotation
     * 
     * @param _x  The x coordinate of the vertex
     * @param _y  The y coordinate of the vertex
     * @param rot The rotation of the vertex
     */
    public Vertex(double _x, double _y, Rotation2d rot) {
        x = _x;
        y = _y;
        rotation = rot;
    }

    /**
     * Creates a new Vertex from an x and y coordinate and a rotation
     * 
     * @param _x  The x coordinate of the vertex
     * @param _y  The y coordinate of the vertex
     * @param rot The rotation of the vertex, in degrees
     */
    public Vertex(double _x, double _y, double rot) {
        x = _x;
        y = _y;
        rotation = Rotation2d.fromDegrees(rot);
    }

    /**
     * Converts an array of Pose2ds to an array of Vertices
     * 
     * @param poses The array of Pose2ds to convert
     * @return The array of Vertices
     */
    public static ArrayList<Vertex> fromPose2dArray(ArrayList<Pose2d> poses) {
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        for (Pose2d pose : poses) {
            vertices.add(new Vertex(pose));
        }
        return vertices;
    }

    /**
     * Get a pose2d representation of the current Vertex
     * 
     * @return The current Vertex as a Pose2d
     */
    public Pose2d asPose2d() {
        return new Pose2d(x, y, rotation);
    }

    /**
     * Get all of the neighbors of this vertex in the visibility graph
     * 
     * @return All static and dynamic neighbors
     */
    public ArrayList<Vertex> getNeighbors() {
        ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
        neighbors.addAll(staticNeighbors);
        neighbors.addAll(dynamicNeighbors);
        return neighbors;
    }

    /**
     * Create a vector to this vertex from a starting vertex
     * 
     * @param starting The starting vertex of the vector
     * @return The vector from the starting vertex to this vertex
     */
    public Vector createVectorFrom(Vertex starting) {
        if (starting == null) {
            System.out.println("Warning: Vector creation passed null starting vertex");
            return new Vector(0, 0);
        }
        return new Vector(x - starting.x, y - starting.y);
    }

    /**
     * Create a vector from this vertex to an ending vertex
     * 
     * @param ending The ending vertex of the vector
     * @return The vector from this vertex to the ending vertex
     */
    public Vector createVectorTo(Vertex ending) {
        if (ending == null) {
            System.out.println("Warning: Vector creation passed null ending vertex");
            return new Vector(0, 0);
        }
        return new Vector(ending.x - x, ending.y - y);
    }

    /**
     * Move this vertex by a vector (add the vector to the vertex)
     * 
     * @param vector The vector to move the vertex by
     * @return The new vertex after moving
     */
    public Vertex moveByVector(Vector vector) {
        if (vector == null) {
            System.out.println("Warning: Move by vector passed null vector");
            return this;
        }
        return new Vertex(x + vector.x, y + vector.y);
    }

    /**
     * Average this vertex with another vertex
     * 
     * @param vertex The vertex to average with
     * @return The position average of the two vertices
     */
    public Vertex average(Vertex vertex) {
        if (vertex == null) {
            System.out.println("Warning: Vertex average passed null vertex");
            return this;
        }
        return new Vertex((x + vertex.x) / 2, (y + vertex.y) / 2);
    }

    /**
     * Subtract a vertex from this vertex
     * 
     * @param v2 The vertex to subtract
     * @return The difference between this vertex and v2
     */
    public Vertex subtract(Vertex v2) {
        if (v2 == null) {
            System.out.println("Warning: Vertex subtraction passed null vertex");
            return this;
        }
        return new Vertex(x - v2.x, y - v2.y);
    }

    /**
     * Add a vertex to this vertex
     * 
     * @param v2 The vertex to add
     * @return The sum of this vertex and v2
     */
    public Vertex add(Vertex v2) {
        if (v2 == null) {
            System.out.println("Warning: Vertex addition passed null vertex");
            return this;
        }
        return new Vertex(x + v2.x, y + v2.y);
    }

    /**
     * Get the euclidian distance between this vertex and another vertex
     * (Pythagorean theorem)
     * 
     * @param target The vertex to get the distance to
     * @return The distance between this vertex and target
     */
    public double distance(Vertex target) {
        double xDist = x - target.x;
        double yDist = y - target.y;
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }

    /**
     * Get the euclidian distance between this vertex and a pose (Pythagorean
     * theorem)
     * 
     * @param target The pose to get the distance to
     * @return The distance between this vertex and target
     */
    public double distance(Pose2d target) {
        double xDist = x - target.getX();
        double yDist = y - target.getY();
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }

    /**
     * Check if this vertex represents the same spot as another vertex
     * 
     * @param o The vertex to compare to
     * @return True if the vertices are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Vertex))
            return false;
        Vertex compare = (Vertex) o;

        return compare.x == this.x && compare.y == this.y;
    }

    /**
     * Get a string representation of the vertex
     * 
     * @return "(x, y)"
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(Vertex o) {
        if (this.F() < o.F())
            return -1;
        else if (this.F() > o.F())
            return 1;
        else if (this.H < o.H)
            return -1;
        else if (this.H > o.H)
            return 1;
        else
            return 0;
    }
}
