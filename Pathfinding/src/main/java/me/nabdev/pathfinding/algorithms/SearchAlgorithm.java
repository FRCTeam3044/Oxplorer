package me.nabdev.pathfinding.algorithms;

import me.nabdev.pathfinding.structures.ImpossiblePathException;
import me.nabdev.pathfinding.structures.Path;
import me.nabdev.pathfinding.structures.Vertex;

/**
 * An interface that represents a search algorithm (like A*)
 */
public interface SearchAlgorithm {
    /**
     * Finds a path from the start to the end.
     * 
     * @param start The starting point.
     * @param end   The target point.
     * @return A Path object containing the path from the start to the target.
     * @throws ImpossiblePathException If there is no possible path from the start
     */
    public Path run(Vertex start, Vertex end) throws ImpossiblePathException;

    /**
     * The different types of search algorithms available.
     * Will hopefully add more in the future.
     */
    public enum SearchAlgorithmType {
        /**
         * The A* search algorithm.
         */
        ASTAR
    }
}