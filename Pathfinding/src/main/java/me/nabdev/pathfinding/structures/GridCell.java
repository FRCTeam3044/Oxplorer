package me.nabdev.pathfinding.structures;

/**
 * Represents a cell in a {@link Grid}
 */
public class GridCell {
    public static double xSize;
    public static double ySize;

    public static Vector topLeft;
    public static Vector topRight;
    public static Vector bottomLeft;
    public static Vector bottomRight;

    public Vertex center;

    public GridCell(Vertex center) {
        this.center = center;
    }

    public boolean contains(Vertex v) {
        return v.x >= center.x - xSize / 2 && v.x <= center.x + xSize / 2 &&
                v.y >= center.y - ySize / 2 && v.y <= center.y + ySize / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GridCell))
            return false;
        GridCell c = (GridCell) o;
        return c.center.equals(center);
    }

    @Override
    public int hashCode() {
        return center.hashCode();
    }

    public static void recomputeVectors() {
        topLeft = new Vector(-xSize / 2, ySize / 2);
        topRight = new Vector(xSize / 2, ySize / 2);
        bottomLeft = new Vector(-xSize / 2, -ySize / 2);
        bottomRight = new Vector(xSize / 2, -ySize / 2);
    }
}
