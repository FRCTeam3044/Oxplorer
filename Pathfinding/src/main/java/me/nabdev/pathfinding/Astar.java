package me.nabdev.pathfinding;

import java.util.ArrayList;

import me.nabdev.pathfinding.Structures.Vertex;
import me.nabdev.pathfinding.Structures.ImpossiblePathException;
import me.nabdev.pathfinding.Structures.Path;

/**
 * A class to represent all the logic behind the A* pathfinding algorithm.
 */
public class Astar {
    private ArrayList<Vertex> toSearch = new ArrayList<>();
    private ArrayList<Vertex> processed = new ArrayList<>();
    private boolean started;
    private boolean solved;
    private Map map;
    private Pathfinder pathfinder;

    // The maximum number of times A* will attempt to go backwards from the target
    // point to the start.
    // Only in place to ensure that the bot is never stuck in an infinite loop.
    private final int maxPathIterations = 20;

    /**
     * Creates a new Astar object.
     * 
     * @param map        The map to run the algorithm on.
     * @param pathfinder The Pathfinder object that created this Astar object.
     */
    public Astar(Map map, Pathfinder pathfinder) {
        this.map = map;
        this.pathfinder = pathfinder;
    }

    /**
     * Runs the A* algorithm.
     * 
     * @param start The starting point.
     * @param end   The target point.
     * @return A Path object containing the path from the start to the target.
     * @throws ImpossiblePathException If there is no possible path from the start
     */
    public Path run(Vertex start, Vertex end) throws ImpossiblePathException {
        while (!solved) {
            if (!started) {
                toSearch.add(start);
                started = true;
            }
            Vertex current;
            try {
                current = toSearch.get(0);
            } catch (IndexOutOfBoundsException exception) {
                // If there are no more nodes to search, then there is no valid path.
                solved = true;
                throw new ImpossiblePathException("No possible path found.");
            }
            for (Vertex Vertex : toSearch) {
                if (Vertex.F() < current.F())
                    current = Vertex;
                else if (Vertex.F() == current.F() && Vertex.H < current.H)
                    current = Vertex;
            }

            toSearch.remove(current);
            processed.add(current);
            for (Vertex neighbor : map.getNeighbors(current)) {
                if (neighbor == end) {
                    solved = true;
                    Path path = new Path(start, end, pathfinder);
                    Vertex cur = current;
                    int i = 0;
                    while (true) {
                        i++;
                        if (i > maxPathIterations) {
                            toSearch.clear();
                            processed.clear();
                            started = false;
                            solved = false;
                            throw new ImpossiblePathException(
                                    "Failed to trace path after solving - this is most likely a bug.");
                        }
                        if (cur.connection != null) {
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
                if (processed.contains(neighbor))
                    continue;
                boolean inSearch = toSearch.contains(neighbor);
                if (current.G + current.distance(neighbor) < neighbor.G || !inSearch) {
                    neighbor.G = current.G + current.distance(neighbor);
                    neighbor.connection = current;
                }
                if (!inSearch) {
                    neighbor.H = neighbor.distance(end);
                    toSearch.add(neighbor);
                }
            }

        }
        throw new ImpossiblePathException("No possible path found.");
    }

}
