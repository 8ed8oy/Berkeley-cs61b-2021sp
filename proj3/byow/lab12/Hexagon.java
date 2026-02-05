package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;


public class Hexagon {

    public static void fillWithOneHexagon(TETile[][] world, int side, int x, int y, TETile tile) {
        if (side < 2) {
            throw new IllegalArgumentException("Hexagon size must be at least 2");
        }
        int totalRows = 2 * side;
        if( checkHexagonFit(world, side, x, y) == false) {
            throw new IllegalArgumentException("Hexagon at the given position does not fit in the world");
        }
        for (int row = 0; row < totalRows; row++) {
            int rowY = y + row;
            int rowLength;
            int rowStartPosition;
            if (row < side) {
                rowLength = side + 2 * row;
                rowStartPosition = x - row;
            } else {
                int invertedRow = totalRows - row - 1;
                rowLength = side + 2 * invertedRow;
                rowStartPosition = x - invertedRow;
            }
            for (int col = 0; col < rowLength; col++) {
                world[rowStartPosition + col][rowY] = tile;
            }
        }
    }

    private static boolean checkHexagonFit(TETile[][] world, int side, int x, int y) {
        int totalRows = 2 * side;
        return !(y + totalRows > world[0].length || x - side + 1 < 0 || x + side - 1 >= world.length);
    }

}
