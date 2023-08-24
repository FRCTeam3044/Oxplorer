package me.nabdev.pathfinding.Structures;

import java.util.ArrayList;

/**
 * Represents one segment of a path.
 * A segment is either a corner or a straightaway between two corners.
 */
public class PathSegment {
    /**
     * The points that make up this segment.
     */
    public ArrayList<Vertex> points = new ArrayList<Vertex>();
    /**
     * Whether this segment is a corner.
     * If true, it is a corner. If false, it is a straightaway.
     */
    public boolean corner;

    /**
     * Creates a new corner segment.
     */
    public PathSegment() {
        this.corner = true;
    }

    /**
     * Creates a new straightaway segment.
     * 
     * @param v1 The first vertex in the segment.
     * @param v2 The second vertex in the segment.
     */
    public PathSegment(Vertex v1, Vertex v2) {
        points.add(v1);
        points.add(v2);
    }

    /**
     * Get the last vertex in the segment.
     * 
     * @return The last vertex in the segment.
     */
    public Vertex end() {
        return points.get(points.size() - 1);
    }

    /**
     * Get the first vertex in the segment.
     * 
     * @return The first vertex in the segment.
     */
    public Vertex start() {
        return points.get(0);
    }

    /**
     * Add a vertex to the segment.
     * 
     * @param v The vertex to add.
     */
    public void add(Vertex v) {
        points.add(v);
    }

    /**
     * Get a vertex in the segment.
     * 
     * @param i The index of the vertex to get.
     * @return The vertex at the given index.
     */
    public Vertex get(int i) {
        return points.get(i);
    }

    /**
     * Replace the points in the segment with a new set of points.
     * 
     * @param newArr The new set of points.
     */
    public void replace(ArrayList<Vertex> newArr) {
        points.clear();
        points.addAll(newArr);
    }
}
