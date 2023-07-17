package pathfinding.Structures;

public class OrderedPair {
    public double x;
    public double y;
    public OrderedPair(double _x, double _y){
        x = _x;
        y = _y;
    }

    public String print(){
        // Easier debugging. Not required for final code.
        return "(" + x + ", " + y + ")";
    }
}
