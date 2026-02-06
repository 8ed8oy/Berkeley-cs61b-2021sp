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
    private List<RectangularRoom> rooms;
    private List<Position> corridors;
    private Position entrance;

    public WorldGenerator(long seed, int width, int height) {
        this.random = new Random(seed);
        this.width = width;
        this.height = height;
        this.world = new TETile[width][height];
        this.rooms = new ArrayList<>();
        this.corridors = new ArrayList<>();
    }

    public TETile[][] generate() {
        fillWorldWith(Tileset.WALL); // Fill with walls initially

        // BSP Initialization
        // Margin of 2 for "thick walls"
        BSPNode root = new BSPNode(2, 2, width - 4, height - 4);
        split(root);

        createRoomsAndCorridors(root);

        // Required elements
        placeEntranceAndExit();
        placeTreasures(random.nextInt(2) + 2); // 2 or 3 treasures
        placeMonsters();

        return world;
    }

    private void fillWorldWith(TETile tile) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = tile;
            }
        }
    }

    private class BSPNode {
        int x, y, w, h;
        BSPNode left, right;
        RectangularRoom room;

        BSPNode(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private void split(BSPNode node) {
        // Min size for a node (to contain a room + walls)
        int minSize = 10;

        // If node is too small, don't split
        if (node.w < minSize * 2 && node.h < minSize * 2) {
            return;
        }

        // Determine split direction
        // If one dimension is much larger, split that way. Otherwise random.
        boolean splitH = random.nextBoolean();
        if (node.w > node.h * 1.5) splitH = false; // Split vertically (width-wise)
        else if (node.h > node.w * 1.5) splitH = true; // Split horizontally (height-wise)

        // Max split ratio
        int splitPos;
        if (splitH) {
             // Split height
             if (node.h <= minSize * 2) return;
             splitPos = random.nextInt(node.h - 2 * minSize) + minSize;
             node.left = new BSPNode(node.x, node.y, node.w, splitPos);
             node.right = new BSPNode(node.x, node.y + splitPos, node.w, node.h - splitPos);
        } else {
             // Split width
             if (node.w <= minSize * 2) return;
             splitPos = random.nextInt(node.w - 2 * minSize) + minSize;
             node.left = new BSPNode(node.x, node.y, splitPos, node.h);
             node.right = new BSPNode(node.x + splitPos, node.y, node.w - splitPos, node.h);
        }

        split(node.left);
        split(node.right);
    }

    private void createRoomsAndCorridors(BSPNode node) {
        if (node.left != null || node.right != null) {
            // Internal node
            if (node.left != null) createRoomsAndCorridors(node.left);
            if (node.right != null) createRoomsAndCorridors(node.right);

            if (node.left != null && node.right != null) {
                connectNodes(node.left, node.right);
            }

            // Assign a representative room for this node (randomly from children)
            // This aids in connectivity if we connected differently,
            // but here we connect children directly.
            // We need to know "center" of this node for higher level connections?
            // - The recursion ensures siblings are connected.
            // - So the tree is connected.
            node.room = random.nextBoolean() ? node.left.room : node.right.room;
        } else {
            // Leaf node: allow room creation
            // Random room size within node, leaving at least 1 unit padding
            int roomW = random.nextInt(node.w - 4) + 3; // Min width 3
            int roomH = random.nextInt(node.h - 4) + 3; // Min height 3
            int roomX = node.x + random.nextInt(node.w - roomW - 1) + 1;
            int roomY = node.y + random.nextInt(node.h - roomH - 1) + 1;

            RectangularRoom room = new RectangularRoom(roomX, roomY, roomW, roomH);
            room.draw(world, Tileset.FLOOR);
            rooms.add(room);
            node.room = room;
        }
    }

    private void connectNodes(BSPNode node1, BSPNode node2) {
        if (node1.room == null || node2.room == null) return;

        Position p1 = node1.room.getCenter();
        Position p2 = node2.room.getCenter();

        drawLCorridor(p1, p2);
    }

    private void drawLCorridor(Position p1, Position p2) {
        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;

        // Randomly choose first direction
        if (random.nextBoolean()) {
            drawHorizontalTunnel(x1, x2, y1);
            drawVerticalTunnel(y1, y2, x2);
        } else {
            drawVerticalTunnel(y1, y2, x1);
            drawHorizontalTunnel(x1, x2, y2);
        }
    }

    private void drawHorizontalTunnel(int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            if (world[x][y] == Tileset.WALL) {
                world[x][y] = Tileset.FLOOR;
                corridors.add(new Position(x, y));
            }
        }
    }

    private void drawVerticalTunnel(int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            if (world[x][y] == Tileset.WALL) {
                world[x][y] = Tileset.FLOOR;
                corridors.add(new Position(x, y));
            }
        }
    }

    private void placeEntranceAndExit() {
        if (rooms.size() < 2) return;

        RectangularRoom r1 = null;
        RectangularRoom r2 = null;
        double maxDist = -1;

        // Brute force all pairs
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                double dist = distance(rooms.get(i).getCenter(), rooms.get(j).getCenter());
                if (dist > maxDist) {
                    maxDist = dist;
                    r1 = rooms.get(i);
                    r2 = rooms.get(j);
                }
            }
        }

        if (r1 != null && r2 != null) {
            Position p1 = r1.getCenter();
            Position p2 = r2.getCenter();
            world[p1.x][p1.y] = Tileset.UNLOCKED_DOOR; // Entrance
            this.entrance = p1;
            world[p2.x][p2.y] = Tileset.LOCKED_DOOR;   // Exit
        }
    }

    private double distance(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private void placeTreasures(int count) {
        for (int i = 0; i < count; i++) {
            if (rooms.isEmpty()) break;
            RectangularRoom r = rooms.get(random.nextInt(rooms.size()));
            Position p = getRandomPointInRoom(r);
            if (world[p.x][p.y] == Tileset.FLOOR) {
                world[p.x][p.y] = Tileset.FLOWER; // Treasure
            } else {
                i--; // Try again
            }
        }
    }

    private Position getRandomPointInRoom(RectangularRoom r) {
        int x = r.getX() + random.nextInt(r.getWidth());
        int y = r.getY() + random.nextInt(r.getHeight());
        return new Position(x, y);
    }

    private void placeMonsters() {
        // Only place monsters in rooms
        int numRoomMonsters = random.nextInt(10) + 5; // Increased: 5 to 14 monsters
        for (int i = 0; i < numRoomMonsters; i++) {
             if (rooms.isEmpty()) break;
             RectangularRoom r = rooms.get(random.nextInt(rooms.size()));
             Position p = getRandomPointInRoom(r);

             // Check ensure minimum distance from entrance (Player spawn)
             if (this.entrance != null && distance(p, this.entrance) < 10) {
                 i--; // Try again
                 continue;
             }

             if (world[p.x][p.y] == Tileset.FLOOR) {
                 world[p.x][p.y] = Tileset.MOUNTAIN;
             } else {
                 i--;
             }
        }
    }

    private boolean isIntersection(Position p) {
        if (world[p.x][p.y] != Tileset.FLOOR) return false;
        int neighbors = 0;
        int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        for (int[] d : dirs) {
            int nx = p.x + d[0];
            int ny = p.y + d[1];
            if (isInBounds(nx, ny) && isWalkable(nx, ny)) {
                neighbors++;
            }
        }
        return neighbors >= 3;
    }

    private boolean isWalkable(int x, int y) {
        TETile t = world[x][y];
        return t.equals(Tileset.FLOOR) || t.equals(Tileset.MOUNTAIN) ||
               t.equals(Tileset.FLOWER) || t.equals(Tileset.UNLOCKED_DOOR) ||
               t.equals(Tileset.LOCKED_DOOR);
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public List<RectangularRoom> getRooms() {
        return rooms;
    }
}
