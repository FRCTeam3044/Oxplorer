package pathfinding.Structures;

import java.util.ArrayList;

public class PathSegment {
    public ArrayList<Vertex> points = new ArrayList<Vertex>();
    public boolean corner;

    public PathSegment(){
        this.corner = true;
    }

    public PathSegment(Vertex v1, Vertex v2){
        points.add(v1);
        points.add(v2);
    }

    public Vertex end(){
        return points.get(points.size() - 1);
    }

    public Vertex start(){
        return points.get(0);
    }

    public void add(Vertex v){
        points.add(v);
    }

    public Vertex get(int i){
        return points.get(i);
    }

    public void replace(ArrayList<Vertex> newArr){
        points.clear();
        points.addAll(newArr);
    }
}
