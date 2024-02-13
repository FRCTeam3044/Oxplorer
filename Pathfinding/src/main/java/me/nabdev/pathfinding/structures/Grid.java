package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

import edu.wpi.first.math.MathUtil;

/**
 * Represents a grid used to speed up the visibility graph generation. It
 * generates a big list of possible edges that could be in the way between any
 * two cells, and then when generating the visibility graph, it only checks
 * those edges.
 */
public class Grid {
    private GridCell[][] cells;
    private GridCellPair[][][][] cellPairs;
    private boolean snapInField = false;

    /**
     * Creates a new grid with the given number of cells on each dimension.
     * 
     * @param xCells      The number of cells on the x axis
     * @param yCells      The number of cells on the y axis
     * @param edges       The edges to use when generating the possible edges
     * @param vertices    The vertices to use when generating the possible edges
     * @param fieldx      The field's x dimension (meters)
     * @param fieldy      The field's y dimension (meters)
     * @param snapInField Whether to snap the vertices to inside the field
     */
    public Grid(int xCells, int yCells, ArrayList<Edge> edges, ArrayList<Vertex> vertices, double fieldx,
            double fieldy, boolean snapInField) {
        this.snapInField = snapInField;
        GridCell.xSize = (fieldx - Map.originx) / (double) xCells;
        GridCell.ySize = (fieldy - Map.originy) / (double) yCells;
        GridCell.recomputeVectors();
        cells = new GridCell[xCells][yCells];
        cellPairs = new GridCellPair[xCells][yCells][xCells][yCells];
        for (int x = 0; x < xCells; x++) {
            for (int y = 0; y < yCells; y++) {
                cells[x][y] = new GridCell(new Vertex((GridCell.xSize / 2) + x * GridCell.xSize,
                        (GridCell.ySize / 2) + y * GridCell.ySize));
            }
        }

        ArrayList<Vertex> centers = new ArrayList<>();
        for (int x = 0; x < xCells; x++) {
            for (int y = 0; y < yCells; y++) {
                centers.add(cells[x][y].center);
                for (int x2 = 0; x2 < xCells; x2++) {
                    for (int y2 = 0; y2 < yCells; y2++) {
                        GridCellPair pair = new GridCellPair(cells[x][y], cells[x2][y2]);
                        pair.calculatePossibleEdges(edges, vertices);
                        cellPairs[x][y][x2][y2] = pair;
                        // if (!possibleEdgeLookup.containsKey(pair)) {
                        // possibleEdgeLookup.put(pair,
                        // pair.getPossibleEdges(edges, vertices));
                        // }
                    }
                }
            }
        }
    }

    // long calculatetimeAverage = 0;
    // long arrayLookupAverage = 0;
    // long totalTimeAverage = 0;
    // int iterations = 0;

    /**
     * Gets the {@link GridCellPair} for the given vertices and caches it
     * 
     * @param a The first vertex
     * @param b The second vertex
     * @return The {@link GridCellPair} for the given vertices
     * @throws ImpossiblePathException If the vertices are not in the field
     */
    public GridCellPair getCellPairOf(Vertex a, Vertex b) throws ImpossiblePathException {
        return getCellPairOf(a, b, false);
    }

    /**
     * Gets the {@link GridCellPair} for the given vertices and caches it
     * 
     * @param a                The first vertex
     * @param b                The second vertex
     * @param forceSnapInField Whether to force the vertices to snap to the grid
     * @return The {@link GridCellPair} for the given vertices
     * @throws ImpossiblePathException If the vertices are not in the field
     */
    public GridCellPair getCellPairOf(Vertex a, Vertex b, boolean forceSnapInField) throws ImpossiblePathException {
        // iterations++;
        // long startTime = System.nanoTime();
        int x = a.gridX;
        int y = a.gridY;
        int x2 = b.gridX;
        int y2 = b.gridY;
        if (a.gridX == -1) {
            x = (int) Math.floor(a.x * GridCell.xSizeDividend);
            y = (int) Math.floor(a.y * GridCell.ySizeDividend);
            int clampedX = MathUtil.clamp(x, 0, cells.length - 1);
            int clampedY = MathUtil.clamp(y, 0, cells[0].length - 1);
            if ((!snapInField && !forceSnapInField) && clampedX != x || clampedY != y) {
                throw new ImpossiblePathException("Vertex " + a + " is not in the field");
            }
            x = clampedX;
            y = clampedY;
            a.gridX = x;
            a.gridY = y;
        }
        if (b.gridX == -1) {
            x2 = (int) Math.floor(b.x * GridCell.xSizeDividend);
            y2 = (int) Math.floor(b.y * GridCell.ySizeDividend);
            int clampedX2 = MathUtil.clamp(x2, 0, cells.length - 1);
            int clampedY2 = MathUtil.clamp(y2, 0, cells[0].length - 1);
            if ((!snapInField && !forceSnapInField) && clampedX2 != x || clampedY2 != y) {
                throw new ImpossiblePathException("Vertex " + a + " is not in the field");
            }
            x2 = clampedX2;
            y2 = clampedY2;
            b.gridX = x2;
            b.gridY = y2;
        }
        // long endTime = System.nanoTime();
        // long arrayLookupStart = System.nanoTime();
        return cellPairs[x][y][x2][y2];
        // long arrayLookupEnd = System.nanoTime();
        // calculatetimeAverage += endTime - startTime;
        // arrayLookupAverage += arrayLookupEnd - arrayLookupStart;
        // totalTimeAverage += arrayLookupEnd - startTime;

        // double calcTime = calculatetimeAverage / iterations;
        // double lookupTime = arrayLookupAverage / iterations;
        // double totalTime = totalTimeAverage / iterations;
        // // Print out the percentage of time spent calculating the cell pair
        // System.out.println("Calculating: " + calcTime + " (" + (double) calcTime /
        // totalTime * 100 + "%)");
        // // Print out the percentage of time spent looking up the cell pair
        // System.out.println("Looking up: " + lookupTime + " (" + (double) lookupTime /
        // totalTime * 100 + "%)");
    }
}
