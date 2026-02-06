package byow.Core;

import byow.TileEngine.TETile;

public class HexagonalRoom implements Room {
    private int side;
    private int x;
    private int y;

    public HexagonalRoom(int x, int y, int side) {
        this.x = x;
        this.y = y;
        this.side = side;
    }

    public Position getCenter() {
        // Hexagon height is 2*side. Center Y is y + side.
        // Hexagon is drawn centered around x roughly.
        // Based on Hexagon.java: center X is x?
        // Let's assume x is the "center-ish" column.
        return new Position(x, y + side);
    }

    public void draw(TETile[][] world, TETile floorTile) {
        // Use the static method from Hexagon class, or copy logic.
        // Copying logic to avoid exception handling issues
        // regarding "fit" which might crash generator.
        // We will just draw and ignore bounds to avoid crash, or clamp.

        int totalRows = 2 * side;
        for (int row = 0; row < totalRows; row++) {
            int rowY = y + row;
            if (rowY < 0 || rowY >= world[0].length) continue;

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
                int worldX = rowStartPosition + col;
                if (worldX >= 0 && worldX < world.length) {
                    world[worldX][rowY] = floorTile;
                }
            }
        }
    }
}
