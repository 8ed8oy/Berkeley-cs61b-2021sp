package byow.Core;

import byow.TileEngine.TETile;

public interface Room {
    Position getCenter();
    void draw(TETile[][] world, TETile floorTile);
}
