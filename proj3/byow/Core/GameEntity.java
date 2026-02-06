package byow.Core;

import byow.TileEngine.TETile;

/**
 * Base class for all dynamic objects in the game (Player, Monsters, Items).
 * Separates logic (Position, stats) from appearance (TETile).
 */
public abstract class GameEntity {
    protected Position position;
    protected TETile appearance;
    protected String name;

    public GameEntity(Position position, TETile appearance, String name) {
        this.position = position;
        this.appearance = appearance;
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position p) {
        this.position = p;
    }

    public TETile getAppearance() {
        return appearance;
    }

    // Logic to move the entity - checks bounds but validation should be done by the Engine/Map
    public void move(int dx, int dy) {
        this.position = new Position(position.x + dx, position.y + dy);
    }
}
