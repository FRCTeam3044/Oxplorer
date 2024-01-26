package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

import me.nabdev.pathfinding.modifiers.ModifierCollection;

/**
 * Edge class, used to represent an edge between two vertices.
 */
public class Edge {
    /**
     * The index in the obstacleVertices array of the first vertex.
     */
    private int vertexOne;
    /**
     * The index in the obstacleVertices array of the second vertex.
     */
    private int vertexTwo;

    /**
     * The obstacle this edge is a part of.
     */
    private ModifierCollection myModifiers;

    /**
     * Constructor for the Edge class.
     * 
     * @param _x        The index of the first vertex in the obstacleVertices array.
     * @param _y        The index of the second vertex in the obstacleVertices
     *                  array.
     * @param modifiers The modifiers on this edge.
     */
    public Edge(int _x, int _y, ModifierCollection modifiers) {
        vertexOne = _x;
        vertexTwo = _y;
        myModifiers = modifiers;
    }

    /**
     * Constructor for the Edge class.
     * 
     * @param _x The index of the first vertex in the obstacleVertices array.
     * @param _y The index of the second vertex in the obstacleVertices
     *           array.
     */
    public Edge(int _x, int _y) {
        vertexOne = _x;
        vertexTwo = _y;
    }

    // Allows use of .contains on an arraylist, as usually it checks for the given
    // object to be the same instance which we can't always rely on.
    /**
     * Overridden equals method, used to check if two edges are equal.
     * 
     * @param o The object to compare to.
     * @return Whether the two edges are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Edge))
            return false;
        Edge compare = (Edge) o;

        return (compare.vertexOne == this.vertexOne && compare.vertexTwo == this.vertexTwo)
                || (compare.vertexOne == this.vertexTwo && compare.vertexTwo == this.vertexOne);
    }

    /**
     * Check if an index is contained in the edge.
     * 
     * @param vertex The index to check.
     * @return The index of the other vertex if the index is contained in the edge,
     *         otherwise -1.
     */
    public int containsVertex(int vertex) {
        if (vertex == vertexOne)
            return vertexTwo;
        if (vertex == vertexTwo)
            return vertexOne;
        return -1;
    }

    /**
     * Check if a vertex is contained in the edge, used when the index is not known.
     * 
     * @param vertex   The vertex to check.
     * @param vertices The list of vertices to check against.
     * @return The index of the other vertex if the vertex is contained in the edge,
     *         otherwise -1.
     */
    public int containsVertex(Vertex vertex, ArrayList<Vertex> vertices) {
        if (vertex.equals(vertices.get(vertexOne)))
            return vertexTwo;
        if (vertex.equals(vertices.get(vertexTwo)))
            return vertexOne;
        return -1;
    }

    /**
     * Get the index in the obstacleVertices list of the first vertex.
     * 
     * @return The index of the first vertex.
     */
    public int getVertexOne() {
        return vertexOne;
    }

    /**
     * Get the index in the obstacleVertices list of the second vertex.
     * 
     * @return The index of the second vertex.
     */
    public int getVertexTwo() {
        return vertexTwo;
    }

    /**
     * Get the first vertex.
     * 
     * @param vertices The list of vertices to get the vertex from.
     * @return The first vertex.
     */
    public Vertex getVertexOne(ArrayList<Vertex> vertices) {
        return vertices.get(vertexOne);
    }

    /**
     * Get the second vertex.
     * 
     * @param vertices The list of vertices to get the vertex from.
     * @return The second vertex.
     */
    public Vertex getVertexTwo(ArrayList<Vertex> vertices) {
        return vertices.get(vertexTwo);
    }

    /**
     * Whether or not the edge is active right now based on the modifiers
     * 
     * @return true if the edge is active (or has no modifiers)
     */
    public boolean isActive() {
        if (myModifiers == null)
            return true;
        return myModifiers.isActive();
    }
}
