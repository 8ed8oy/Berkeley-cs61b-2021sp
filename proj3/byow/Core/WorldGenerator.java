package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private Random random;
    private int width;
    private int height;
    private TETile[][] world;

    public WorldGenerator(long seed, int width, int height) {
        this.random = new Random(seed);
        this.width = width;
        this.height = height;
        this.world = new TETile[width][height];
    }

    public TETile[][] generate() {
        fillWorldWith(Tileset.WALL); // Fill with walls initially
        List<Room> rooms = generateRooms(20, 30); // Try to generate between 20-50 rooms
        connectRooms(rooms);
        return world;
    }

    private void fillWorldWith(TETile tile) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = tile;
            }
        }
    }

    private List<Room> generateRooms(int minRooms, int maxRooms) {
        List<Room> rooms = new ArrayList<>();
        int numRooms = random.nextInt(maxRooms - minRooms) + minRooms;

        for (int i = 0; i < numRooms * 2; i++) {
            if (rooms.size() >= numRooms) break;

            // Randomly decide between Rectangle and Hexagon
            if (random.nextBoolean()) {
                // Rectangle
                int w = random.nextInt(8) + 4; // Width 4-11
                int h = random.nextInt(8) + 4; // Height 4-11
                int x = random.nextInt(width - w - 2) + 1;
                int y = random.nextInt(height - h - 2) + 1;
                RectangularRoom r = new RectangularRoom(x, y, w, h);
                rooms.add(r);
                r.draw(world, Tileset.FLOOR);
            } else {
                // Hexagon
                int side = random.nextInt(3) + 2; // Side 2-4
                int x = random.nextInt(width - 2 * side - 2) + side;
                int y = random.nextInt(height - 2 * side - 2) + 1;
                HexagonalRoom r = new HexagonalRoom(x, y, side);
                rooms.add(r);
                r.draw(world, Tileset.FLOOR);
            }

            // Overlap check removed to allow merging
        }
        return rooms;
    }

    private void connectRooms(List<Room> rooms) {
        // Simple connectivity: connect room i to room i+1
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i + 1);
            connectPoints(r1.getCenter(), r2.getCenter());
        }
    }

    private void connectPoints(Position p1, Position p2) {
        // L-shape connection with random turning point
        int x = p1.x;
        int y = p1.y;

        // Random corridor width 1-3
        int thickness = random.nextInt(3) + 1;

        // Move horizontally then vertically, or vice versa? Randomly decide.
        if (random.nextBoolean()) {
            // Horizontal first
            drawHorizontalCorridor(x, p2.x, y, thickness);
            drawVerticalCorridor(y, p2.y, p2.x, thickness);
        } else {
            // Vertical first
            drawVerticalCorridor(y, p2.y, x, thickness);
            drawHorizontalCorridor(x, p2.x, p2.y, thickness);
        }
    }

    private void drawHorizontalCorridor(int x1, int x2, int y, int thickness) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            for (int t = 0; t < thickness; t++) {
               if (y + t < height) world[x][y + t] = Tileset.FLOOR;
            }
        }
    }

    private void drawVerticalCorridor(int y1, int y2, int x, int thickness) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
             for (int t = 0; t < thickness; t++) {
               if (x + t < width) world[x + t][y] = Tileset.FLOOR;
            }
        }
    }
}
