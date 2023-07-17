package pathfinding;

import java.util.ArrayList;

import pathfinding.Structures.Node;
import pathfinding.Structures.Path;

public class Astar {
    private ArrayList<Node> toSearch = new ArrayList<>();
    private ArrayList<Node> processed = new ArrayList<>();
    public boolean started;
    public boolean solved;
    public Map map;

    // The maximum number of times A* will attempt to go backwards from the target point to the start.
    // Only in place to ensure that the bot is never stuck in an infinite loop.
    private final int maxPathIterations = 20;

    public Astar(Map _map){
        map = _map;
    }

    public Path run(Node Start, Node End){
        while(!solved){
            if(!started){
                toSearch.add(Start);
                started = true;
            }
            Node current;
            try {
                current = toSearch.get(0);
            } catch(IndexOutOfBoundsException exception){
                solved = true;
                return null;
            }
            for(Node node : toSearch){
                if(node.F() < current.F()) current = node;
                else if(node.F() == current.F() && node.H < current.H) current = node;
            }
    
            toSearch.remove(current);
            processed.add(current);
            for(Node neighbor : map.getNeighbors(current)){
                if(neighbor == End){
                    solved = true;
                    Path path = new Path(Start, End);
                    Node cur = current;
                    int i = 0;
                    while(true){
                        i++;
                        if(i > maxPathIterations){
                            System.out.println("Warning: Unable to connect target to start point - pathfinding bug.");
                            toSearch.clear();
                            processed.clear();
                            started = false;
                            solved = false;
                            return null;
                        }
                        if(cur.connection != null){
                            path.add(0, cur);
                            cur = cur.connection;
                        } else {
                            toSearch.clear();
                            processed.clear();
                            started = false;
                            solved = false;
                            return path;
                        }
                    }
                }
                if(processed.contains(neighbor)) continue;
                boolean inSearch = toSearch.contains(neighbor);
                if(current.G + current.distance(neighbor) < neighbor.G || !inSearch){
                    neighbor.G = current.G + current.distance(neighbor);
                    neighbor.connection = current;
                }
                if(!inSearch){
                    neighbor.H = neighbor.distance(End);
                    toSearch.add(neighbor);
                }
            }
    
        }
        return null;
    }
    
}
