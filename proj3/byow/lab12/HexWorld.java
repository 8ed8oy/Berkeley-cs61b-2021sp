package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Hexagon;

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
        TETile[][] HexgonTile = worldGenerator.creatNothingTile(WIDTH, HEIGHT);

        Hexagon.fillWithOneHexagon(HexgonTile, 5, 25, 25, Tileset.WALL);

        worldGenerator.mapRender(HexgonTile);
    }


}
