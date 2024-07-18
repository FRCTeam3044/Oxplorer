package me.nabdev.pathfinding.structures;

/**
 * Represents a cell in a {@link Grid}
 */
public class GridCell {
    /**
     * The size of the cell on the x axis
     */
    public static double xSize;
    /**
     * The size of the cell on the y axis
     */
    public static double ySize;

    /**
     * 1 divided by the xSize, used to slightly speed up
     * {@link Grid#getCellPairOf}
     */
    public static double xSizeDividend;
    /**
     * 1 divided by the ySize, used to slightly speed up
     * {@link Grid#getCellPairOf}
     */
    public static double ySizeDividend;

    /**
     * Vector to the top left corner of the cell from the center
     */
    public static Vector topLeft;
    /**
     * Vector to the top right corner of the cell from the center
     */
    public static Vector topRight;
    /**
     * Vector to the bottom left corner of the cell from the center
     */
    public static Vector bottomLeft;
    /**
     * Vector to the bottom right corner of the cell from the center
     */
    public static Vector bottomRight;

    /**
     * The center of the cell
     */
    public Vertex center;

    /**
     * Creates a new GridCell with the given center
     * 
     * @param center The center of the cell
     */
    public GridCell(Vertex center) {
        this.center = center;
    }

    /**
     * Checks if the given vertex is contained in this cell
     * 
     * @param v The vertex to check
     * @return Whether or not the vertex is contained in this cell
     */
    public boolean contains(Vertex v) {
        return v.x >= center.x - xSize / 2 && v.x <= center.x + xSize / 2 &&
                v.y >= center.y - ySize / 2 && v.y <= center.y + ySize / 2;
    }

    /**
     * Tells you if two GridCells are equal (same centers)
     * 
     * @param o The other GridCell
     * @return Whether or not the two GridCells are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GridCell))
            return false;
        GridCell c = (GridCell) o;
        return c.center.equals(center);
    }

    /**
     * Gets the hashcode of the GridCell
     * 
     * @return The hashcode of the GridCell
     */
    @Override
    public int hashCode() {
        return center.hashCode();
    }

    /**
     * Computes the corner vectors and the dividends for the x and y sizess
     */
    public static void recomputeVectors() {
        xSizeDividend = 1 / xSize;
        ySizeDividend = 1 / ySize;
        topLeft = new Vector(-xSize / 2, ySize / 2);
        topRight = new Vector(xSize / 2, ySize / 2);
        bottomLeft = new Vector(-xSize / 2, -ySize / 2);
        bottomRight = new Vector(xSize / 2, -ySize / 2);
    }
}
