package me.nabdev.pathfinding.structures;

/**
 * Class to represent a two dimensional vector
 */
public class Vector {
    /**
     * The x component of the vector
     */
    public double x;
    /**
     * The y component of the vector
     */
    public double y;

    /**
     * Creates a new vector with the given x and y components
     * 
     * @param _x The x component of the vector
     * @param _y The y component of the vector
     */
    public Vector(double _x, double _y) {
        x = _x;
        y = _y;
    }

    /**
     * Calculate the intersection of two rays, given their starting points and
     * directions.
     * 
     * @param P1 The starting point of the first ray
     * @param V1 The direction of the first ray
     * @param P2 The starting point of the second ray
     * @param V2 The direction of the second ray
     * @return The intersection point of the two rays, or null if they are parallel
     *         or coincident
     */
    public static Vertex calculateRayIntersection(Vertex P1, Vector V1, Vertex P2, Vector V2) {
        double denom = V1.crossProduct(V2);

        if (denom == 0) {
            return null;
        }

        double s = (P2.createVectorFrom(P1).crossProduct(V2)) / denom;

        return P1.moveByVector(V1.scale(s));
    }

    /**
     * Subtract two vectors
     * 
     * @param v2 The vector to subtract from this vector
     * @return The result of subtracting v2 from this vector
     */
    public Vector subtract(Vector v2) {
        return new Vector(x - v2.x, y - v2.y);
    }

    /**
     * Add two vectors
     * 
     * @param v2 The vector to add to this vector
     * @return The result of adding v2 to this vector
     */
    public Vector add(Vector v2) {
        return new Vector(x + v2.x, y + v2.y);
    }

    /**
     * Normalize this vector
     * 
     * @return The normalized vector
     */
    public Vector normalize() {
        double magnitude = magnitude();
        if (Math.abs(magnitude) < 1e-6) {
            System.out.println("Warning: Vector could not be normalized");
            return new Vector(0, 0);
        }
        double newX = x / magnitude;
        double newY = y / magnitude;
        return new Vector(newX, newY);
    }

    /**
     * Calculate the magnitude of this vector
     * 
     * @return The magnitude of this vector
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Scale this vector by a factor (To set a specific magnitude, use normalize()
     * first)
     * 
     * @param factor The factor to scale this vector by
     * @return The scaled vector
     */
    public Vector scale(double factor) {
        double newX = x * factor;
        double newY = y * factor;
        return new Vector(newX, newY);
    }

    /**
     * Calculate the normal (perpendicular) of this vector (-y, x)
     * 
     * @return The normal of this vector
     */
    public Vector calculateNormal() {
        return new Vector(-y, x);
    }

    /**
     * Calculate the normal, but invert if the normal is not facing away from the
     * shape center
     * 
     * @param shapeCenter The center of the shape
     * @param v1          The first vertex of the vector to calculate the normal of
     * @param v2          The second vertex of the vector to calculate the normal of
     * @return The normal of the vector
     */
    public static Vector calculateNormalWithRespectToShape(Vertex shapeCenter, Vertex v1, Vertex v2) {
        Vector vertexToCenter = v1.createVectorFrom(shapeCenter);
        Vector normal = v1.createVectorFrom(v2).calculateNormal();
        if (vertexToCenter.dotProduct(normal) < 0) {
            normal = normal.scale(-1);
        }
        return normal;
    }

    /**
     * Calculate the dot product of this vector and another vector
     * 
     * @param v2 The other vector
     * @return The dot product of this vector and v2
     */
    public double dotProduct(Vector v2) {
        return x * v2.x + y * v2.y;
    }

    /**
     * Calculate the cross product of this vector and another vector
     * 
     * @param v2 The other vector
     * @return The cross product of this vector and v2
     */
    public double crossProduct(Vector v2) {
        return x * v2.y - y * v2.x;
    }

    /**
     * Whether or not the line segment from d1 to d2 intersects the line segment
     * from c1 to c2
     * 
     * @param d1 The first vertex of the first line segment
     * @param d2 The second vertex of the first line segment
     * @param c1 The first vertex of the second line segment
     * @param c2 The second vertex of the second line segment
     * @return True if the line segments intersect, false otherwise
     */
    public static boolean dotIntersectFast(Vertex d1, Vertex d2, Vertex c1, Vertex c2) {
        double normaldy = d2.y - d1.y;
        double normaldx = d2.x - d1.x;
        double normalcy = c2.y - c1.y;
        double normalcx = c2.x - c1.x;
        double p1d = (c1.x - d1.x) * normaldy - (c1.y - d1.y) * normaldx;
        double p2d = (c2.x - d1.x) * normaldy - (c2.y - d1.y) * normaldx;
        double p1c = (d1.x - c1.x) * normalcy - (d1.y - c1.y) * normalcx;
        double p2c = (d2.x - c1.x) * normalcy - (d2.y - c1.y) * normalcx;

        // This is gross but I can't think of a proper
        // This misses when the lines are on the same line, but only if that line is
        // diagonal.
        if (p1d == 0 && p2d == 0 && p1c == 0 && p2c == 0) {
            if (d1.x == d2.x) {
                return (c1.x == d1.x && c1.y <= d2.y && c1.y >= d1.y) || (c2.x == d1.x &&
                        c2.y <= d2.y && c2.y >= d1.y);
            } else {
                return (c1.y == d1.y && c1.x <= d2.x && c1.x >= d1.x) || (c2.y == d1.y &&
                        c2.x <= d2.x && c2.x >= d1.x);
            }
        }
        if ((p1d == 0 || p2d == 0) && (p1c == 0 || p2c == 0))
            return true;
        return !(p1c < 0 == p2c < 0 || p1d < 0 == p2d < 0);
    }

    /**
     * Whether or not the line segment from d1 to d2 intersects the line segment
     * from c1 to c2
     * 
     * @param d1 The first vertex of the first line segment
     * @param d2 The second vertex of the first line segment
     * @param c1 The first vertex of the second line segment
     * @param c2 The second vertex of the second line segment
     * @return True if the line segments intersect, false otherwise
     * 
     * @deprecated This method is left here to clarify how dotIntersectFast works,
     *             but it creates a lot of garbage and is slow. Use
     *             {@link Vector#dotIntersectFast} instead.
     */
    @Deprecated
    public static boolean dotIntersect(Vertex d1, Vertex d2, Vertex c1, Vertex c2) {
        Vector Normald = d2.createVectorFrom(d1).calculateNormal();
        Vector Normalc = c2.createVectorFrom(c1).calculateNormal();
        Vector p1 = c1.createVectorFrom(d1);
        Vector p2 = c2.createVectorFrom(d2);
        double p1d = p1.dotProduct(Normald);
        double p2d = p2.dotProduct(Normald);
        double p1c = p1.dotProduct(Normalc);
        double p2c = p2.dotProduct(Normalc);
        // This is gross but I can't think of a proper
        // This misses when the lines are on the same line, but only if that line is
        // diagonal.
        if (p1d == 0 && p2d == 0 && p1c == 0 && p2c == 0) {
            if (d1.x == d2.x) {
                return (c1.x == d1.x && c1.y <= d2.y && c1.y >= d1.y) || (c2.x == d1.x &&
                        c2.y <= d2.y && c2.y >= d1.y);
            } else {
                return (c1.y == d1.y && c1.x <= d2.x && c1.x >= d1.x) || (c2.y == d1.y &&
                        c2.x <= d2.x && c2.x >= d1.x);
            }
        }
        if ((p1d == 0 || p2d == 0) && (p1c == 0 || p2c == 0))
            return true;
        return !(p1c < 0 == p2c < 0 || p1d < 0 == p2d < 0);
    }

    /**
     * Check if this vector has zero x and y components
     * 
     * @return True if this vector has zero x and y components, false otherwise
     */
    public boolean zero() {
        return this.x == 0 && this.y == 0;
    }

    /**
     * Check if any component of this vector is NaN
     * 
     * @param warning The warning to print if this vector is NaN
     * @return True if this vector is NaN, false otherwise
     */
    public Vector ensureNotNaN(String warning) {
        if (Double.isNaN(this.x) || Double.isNaN(this.y)) {
            System.out.println(warning);
            return new Vector(0, 0);
        } else
            return this;
    }

    /**
     * Rotate this vector by a given angle
     * 
     * @param angle The angle to rotate this vector by
     * @return The rotated vector
     */
    public Vector rotate(double angle) {
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        return new Vector(newX, newY);
    }

    /**
     * Calculate the average of this vector and another vector
     * 
     * @param vec2 The other vector
     * @return The average of this vector and vec2
     */
    public Vector average(Vector vec2) {
        return new Vector((x + vec2.x) / 2, (y + vec2.y) / 2);
    }

    /**
     * Get a string representation of this vector
     * 
     * @return "(x, y)"
     */
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
