package me.nabdev.pathfinding.Structures;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Represents a point in 2D space
 */
public class Vertex {
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
     * Get a pose2d representation of the current Vertex
     * 
     * @return The current Vertex as a Pose2d
     */
    public Pose2d asPose2d() {
        return new Pose2d(x, y, rotation);
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

    /*
     * This code is temporarily copied from
     * https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/.
     * I will do testing against this method and my dot product method to fix bugs
     * in either and find the faster one.
     * This one is probably a lot more accurate, but I'm not sure if it's faster.
     */
    /**
     * Whether or not the line segment from p1 to q1 intersects the line segment
     * from p2 to q2
     * 
     * @param p1 The first vertex of the first line segment
     * @param q1 The second vertex of the first line segment
     * @param p2 The first vertex of the second line segment
     * @param q2 The second vertex of the second line segment
     * @return True if the line segments intersect, false otherwise
     */
    public static boolean doesIntersect(Vertex p1, Vertex q1, Vertex p2, Vertex q2) {
        // Find the four orientations needed for general and
        // special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
            return true;

        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
            return true;

        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
            return true;

        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2))
            return true;

        return false; // Doesn't fall in any of the above cases
    }

    private static boolean onSegment(Vertex p, Vertex q, Vertex r) {
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
            return true;

        return false;
    }

    private static int orientation(Vertex p, Vertex q, Vertex r) {
        double val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (Math.abs(val) < 0.00001)
            return 0; // collinear

        return (val > 0) ? 1 : 2; // clock or counterclock wise
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
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
