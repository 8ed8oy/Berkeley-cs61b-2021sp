package byow.Core;

import byow.TileEngine.TETile;

public class RectangularRoom implements Room {
    private int width;
    private int height;
    private int x;
    private int y;

    public RectangularRoom(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Position getCenter() {
        return new Position(x + width / 2, y + height / 2);
    }

    public void draw(TETile[][] world, TETile floorTile) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                // Check bounds to be safe
                if (i >= 0 && i < world.length && j >= 0 && j < world[0].length) {
                    world[i][j] = floorTile;
                }
            }
        }
    }
}
