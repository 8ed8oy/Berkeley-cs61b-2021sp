package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;

public class Monster extends GameEntity {
    private boolean active;
    private int health;

    public Monster(Position position) {
        super(position, Tileset.MOUNTAIN, "Monster");
        this.active = false;
        this.health = 3;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void moveTowards(Position target, TETile[][] world) {
        if (!active) {
            return;
        }

        int dx = Integer.compare(target.x, position.x);
        int dy = Integer.compare(target.y, position.y);

        // Simple pathfinding: try X, then Y, or random choice
        // If dx != 0, try moving x
        // If dy != 0, try moving y

        int newX = position.x;
        int newY = position.y;

        // Try to move closer in one axis
        // Prioritize the axis with larger distance? Or random?
        // Let's try to move along X first if deltaX > deltaY roughly

        if (dx != 0 && isWalkable(world, position.x + dx, position.y)) {
            newX += dx;
        } else if (dy != 0 && isWalkable(world, position.x, position.y + dy)) {
            newY += dy;
        } else {
            // Stupid random move if blocked
            // Or try the other axis if X was blocked
            if (dx != 0 && dy != 0) {
                 if (isWalkable(world, position.x, position.y + dy)) {
                     newY += dy;
                 }
            }
        }

        position = new Position(newX, newY);
    }

    private boolean isWalkable(TETile[][] world, int x, int y) {
        if (x < 0 || x >= world.length || y < 0 || y >= world[0].length) {
            return false;
        }
        TETile t = world[x][y];
        return !t.equals(Tileset.WALL) && !t.equals(Tileset.LOCKED_DOOR) && !t.equals(Tileset.NOTHING);
    }
}
