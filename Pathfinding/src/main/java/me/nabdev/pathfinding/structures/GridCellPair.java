package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

public class GridCellPair {
    private GridCell a;
    private GridCell b;

    public ArrayList<Edge> possibleEdges = new ArrayList<>();

    private int hashCode = -1;

    public GridCellPair(GridCell a, GridCell b) {
        this.a = a;
        this.b = b;
    }

    // This could probably be made more efficient but its precomputed so it doesn't
    // really matter
    public ArrayList<Edge> getPossibleEdges(ArrayList<Edge> edges, ArrayList<Vertex> vertices) {
        if (a.equals(b))
            return getPossibleEdgesIn1(edges, vertices);
        return getPossibleEdgesBetween2(edges, vertices);
    }

    private ArrayList<Edge> getPossibleEdgesIn1(ArrayList<Edge> edges, ArrayList<Vertex> vertices) {
        ArrayList<Edge> possibleEdges = new ArrayList<>();

        Vertex bottomLeft = a.center.moveByVector(GridCell.bottomLeft);
        Vertex bottomRight = a.center.moveByVector(GridCell.bottomRight);
        Vertex topLeft = a.center.moveByVector(GridCell.topLeft);
        Vertex topRight = a.center.moveByVector(GridCell.topRight);
        for (Edge e : edges) {
            Vertex v1 = e.getVertexOne(vertices);
            Vertex v2 = e.getVertexTwo(vertices);
            if (Vector.dotIntersectFast(topLeft, topRight, v1, v2) ||
                    Vector.dotIntersectFast(topRight, bottomRight, v1, v2) ||
                    Vector.dotIntersectFast(bottomRight, bottomLeft, v1, v2) ||
                    Vector.dotIntersectFast(bottomLeft, topLeft, v1, v2)) {
                possibleEdges.add(e);
            } else {
                // If both vertices are inside the cell, the edge is possible
                if (a.contains(v1) && a.contains(v2)) {
                    possibleEdges.add(e);
                }
            }
        }

        return possibleEdges;
    }

    private ArrayList<Edge> getPossibleEdgesBetween2(ArrayList<Edge> edges, ArrayList<Vertex> vertices) {
        ArrayList<Edge> possibleEdges = new ArrayList<>();

        Vector normal = a.center.createVectorTo(b.center).calculateNormal().normalize();

        double topLeft = normal.dotProduct(GridCell.topLeft);
        double topRight = normal.dotProduct(GridCell.topRight);
        double bottomLeft = normal.dotProduct(GridCell.bottomLeft);
        double bottomRight = normal.dotProduct(GridCell.bottomRight);

        Vector toCorner1 = getVectorFromDots(topLeft, topRight, bottomLeft, bottomRight, false);
        Vector toCorner2 = getVectorFromDots(topLeft, topRight, bottomLeft, bottomRight, true);

        Vertex aCorner1 = a.center.moveByVector(toCorner1);
        Vertex aCorner2 = a.center.moveByVector(toCorner2);
        Vertex bCorner1 = b.center.moveByVector(toCorner1);
        Vertex bCorner2 = b.center.moveByVector(toCorner2);

        for (Edge e : edges) {
            Vector v1 = e.getVertexOne(vertices).createVectorFrom(aCorner1);
            Vector v2 = e.getVertexTwo(vertices).createVectorFrom(aCorner1);
            Vector v3 = e.getVertexOne(vertices).createVectorFrom(aCorner2);
            Vector v4 = e.getVertexTwo(vertices).createVectorFrom(aCorner2);

            Vector norm1 = Vector.calculateNormalWithRespectToShape(a.center, aCorner1, bCorner1).normalize();
            Vector norm2 = Vector.calculateNormalWithRespectToShape(a.center, aCorner2, bCorner2).normalize();

            if (!(v1.dotProduct(norm1) > 0 && v2.dotProduct(norm1) > 0)
                    && !(v3.dotProduct(norm2) > 0 && v4.dotProduct(norm2) > 0)) {
                possibleEdges.add(e);
            }
        }

        return possibleEdges;
    }

    /**
     * Return the vector from the center of the cell to the corner with the largest
     * (or smallest if negative is true) dot product
     * 
     * @param tL       Dot product of the normal with the top left corner
     * @param tR       Dot product of the normal with the top right corner
     * @param bL       Dot product of the normal with the bottom left corner
     * @param bR       Dot product of the normal with the bottom right corner
     * @param negative Whether to return the vector with the largest or smallest dot
     *                 product
     * @return The vector from the center of the cell to the corner with the largest
     *         (or smallest if negative is true) dot product
     */
    private Vector getVectorFromDots(double tL, double tR, double bL, double bR, boolean negative) {
        if (negative) {
            double min = Math.min(tL, Math.min(tR, Math.min(bL, bR)));
            if (tL == min)
                return GridCell.topLeft;
            if (tR == min)
                return GridCell.topRight;
            if (bL == min)
                return GridCell.bottomLeft;
            if (bR == min)
                return GridCell.bottomRight;
        } else {
            double max = Math.max(tL, Math.max(tR, Math.max(bL, bR)));
            if (tL == max)
                return GridCell.topLeft;
            if (tR == max)
                return GridCell.topRight;
            if (bL == max)
                return GridCell.bottomLeft;
            if (bR == max)
                return GridCell.bottomRight;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GridCellPair))
            return false;
        GridCellPair p = (GridCellPair) o;
        return (p.a.equals(a) && p.b.equals(b)) || (p.a.equals(b) && p.b.equals(a));
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = 31 * a.hashCode() + b.hashCode();
        return hashCode;
    }
}
