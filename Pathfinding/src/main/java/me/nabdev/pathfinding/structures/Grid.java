package me.nabdev.pathfinding.structures;

import java.util.ArrayList;
import java.util.HashMap;

import me.nabdev.pathfinding.Map;

/**
 * Represent a grid with an arbitray number of cells on either dimension.
 * When created, the grid should be filled with {@link GridCell} with the size
 * calculated based on the field size and the number of cells.
 */
public class Grid {
    private GridCell[][] cells;
    public HashMap<GridCellPair, ArrayList<Edge>> possibleEdgeLookup = new HashMap<>();

    /**
     * Creates a new grid with the given number of cells on each dimension.
     * 
     * @param xCells The number of cells on the x axis
     * @param yCells The number of cells on the y axis
     */
    public Grid(int xCells, int yCells, ArrayList<Edge> edges, ArrayList<Vertex> vertices) {
        GridCell.xSize = (Map.fieldx - Map.originx) / (double) xCells;
        GridCell.ySize = (Map.fieldy - Map.originy) / (double) yCells;
        GridCell.recomputeVectors();
        cells = new GridCell[xCells][yCells];
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
                        if (!possibleEdgeLookup.containsKey(pair)) {
                            possibleEdgeLookup.put(pair,
                                    pair.getPossibleEdges(edges, vertices));
                        }
                    }
                }
            }
        }
    }

    // TODO: Clamp this inside field bounds
    public GridCell getCellOf(Vertex v) {
        int x = (int) Math.floor(v.x / GridCell.xSize);
        int y = (int) Math.floor(v.y / GridCell.ySize);
        return cells[x][y];
    }

    public GridCellPair getCellsOf(Vertex a, Vertex b) {
        return new GridCellPair(getCellOf(a), getCellOf(b));
    }
}
