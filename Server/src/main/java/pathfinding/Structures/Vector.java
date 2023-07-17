package pathfinding.Structures;

// Helper class to neaten up the actual calculations.
public class Vector extends OrderedPair {
    public Vector(double _x, double _y){
        super(_x, _y);
    }

    public Vector subtract(Vector v2){
        return new Vector(x - v2.x, y - v2.y);
    }

    public Vector add(Vector v2){
        return new Vector(x + v2.x, y + v2.y);
    }

    public Vector normalize(){
        double magnitude = magnitude();
        if(Math.abs(magnitude) < 1e-6){
            System.out.println("Warning: Vector could not be normalized");
            return new Vector(0, 0);
        }
        double newX = x / magnitude;
        double newY = y / magnitude;
        return new Vector(newX, newY);
    }

    public double magnitude(){
        return Math.sqrt(x*x + y*y);
    }

    public Vector scale(double factor){
        double newX = x * factor;
        double newY = y * factor;
        return new Vector(newX, newY);
    }

    public Vector calculateNormal(){
        return new Vector(-y, x);
    }

    public double dotProduct(Vector v2){
        return x * v2.x + y * v2.y;
    }

    public static boolean dotIntersect(Vertex d1, Vertex d2, Vertex c1, Vertex c2){
        Vector Normald = d2.createVector(d1).calculateNormal();
        Vector Normalc = c2.createVector(c1).calculateNormal();
        Vector p1 = c1.createVector(d1);
        Vector p2 = c2.createVector(d2);
        double p1d = p1.dotProduct(Normald);
        double p2d = p2.dotProduct(Normald);
        double p1c = p1.dotProduct(Normalc);
        double p2c = p2.dotProduct(Normalc);
        // This is gross but I don't have time to think of a proper way to handle this.
        // This misses when the lines are on the same line, but only if that line is diagonal.
        if(p1d == 0 && p2d ==  0 && p1c == 0 && p2c == 0){
            if(d1.x == d2.x){
                return (c1.x == d1.x && c1.y <= d2.y && c1.y >= d1.y) || (c2.x == d1.x && c2.y <= d2.y && c2.y >= d1.y);
            } else {
                return (c1.y == d1.y && c1.x <= d2.x && c1.x >= d1.x) || (c2.y == d1.y && c2.x <= d2.x && c2.x >= d1.x);
            }
            
        }
        if((p1d == 0 || p2d == 0) && (p1c == 0 || p2c == 0)) return true;
        return !(p1c < 0 == p2c < 0 || p1d < 0 == p2d < 0);
    }

    public boolean zero(){
        return this.x == 0 && this.y == 0;
    }

    public Vector ensureNotNaN(String warning){
        if(Double.isNaN(this.x) || Double.isNaN(this.y)){
            System.out.println(warning);
            return new Vector(0, 0);
        } else return this;
    }
    
    public Vector rotate(double angle){
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        return new Vector(newX, newY);
    }
}
