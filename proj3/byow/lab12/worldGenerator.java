package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class worldGenerator {
    public static TETile[][] creatNothingTile(int w,int h) {
        TETile[][] nothingWorld = new TETile[w][h];
        fillWithNothingTiles(nothingWorld);
        return nothingWorld;
    }

    public static void fillWithNothingTiles (TETile[][] world) {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
    }

    public static void mapRender(TETile[][] world) {
        TERenderer map = new TERenderer();
        map.initialize(world.length, world[0].length);
        map.renderFrame(world);
    }
}
