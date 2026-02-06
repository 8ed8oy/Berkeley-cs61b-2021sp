package byow.lab12;

import byow.Core.Hexagon;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.TileEngine.TERenderer;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 114514;
    private static final Random RANDOM = new Random(SEED);

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        Hexagon.fillWithOneHexagon(world, 5, 25, 25, Tileset.WALL);

        ter.renderFrame(world);
    }


}
