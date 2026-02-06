package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Trap extends GameEntity {
    private int durability;

    public Trap(Position position) {
        super(position, Tileset.TRAP, "Trap");
        this.durability = 1; // Default durability
    }

    public int getDurability() {
        return durability;
    }

    public void decreaseDurability() {
        this.durability--;
    }
}
