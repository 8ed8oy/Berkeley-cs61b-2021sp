package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 100;
    public static final int HEIGHT = 50;

    private static final int TILE_SIZE = 16;

    private Player player;
    private int level;
    private long currentSeed;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        // Initialize StdDraw
        ter.initialize(WIDTH, HEIGHT);

        while (true) {
            drawMainMenu();
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N') {
                    long seed = handleSeedInput();
                    this.currentSeed = seed;
                    this.level = 1;
                    this.player = null; // New player
                    gameLoop();
                    break;
                } else if (c == 'Q') {
                    System.exit(0);
                }
                // 'L' for load game would go here
            }
        }
    }

    private void drawMainMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.75, "CS61B: THE GAME");

        Font smallFont = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "New Game (N)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.45, "Load Game (L)");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4, "Quit (Q)");
        StdDraw.show();
    }

    private long handleSeedInput() {
        String input = "";
        drawSeedFrame(input);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'S') {
                    break;
                } else if (Character.isDigit(c)) {
                    input += c;
                    drawSeedFrame(input);
                }
            }
            StdDraw.pause(10);
        }
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void drawSeedFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.6, "Enter Seed (Press S to start):");
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, s);
        StdDraw.show();
    }

    private void gameLoop() {
        while (true) {
            boolean nextLevel = runLevel();
            if (nextLevel) {
                level++;
                currentSeed++; // Simple seed increment for next level
            } else {
                // Game Over or unexpected exit
                break;
            }
        }
    }

    private boolean runLevel() {
        WorldGenerator generator = new WorldGenerator(currentSeed, WIDTH, HEIGHT);
        TETile[][] staticWorld = generator.generate();
        List<RectangularRoom> rooms = generator.getRooms();

        List<Monster> monsters = new ArrayList<>();
        List<Trap> traps = new ArrayList<>();
        Position startPos = null;
        Position exitPos = null;

        // Scan world for entities and features
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (staticWorld[x][y].equals(Tileset.MOUNTAIN)) {
                    monsters.add(new Monster(new Position(x, y)));
                    staticWorld[x][y] = Tileset.FLOOR; // Replace with floor
                } else if (staticWorld[x][y].equals(Tileset.UNLOCKED_DOOR)) {
                    startPos = new Position(x, y);
                    // Keep door visuals? Or replace?
                    // Let's keep it as "Entrance"
                } else if (staticWorld[x][y].equals(Tileset.LOCKED_DOOR)) {
                    exitPos = new Position(x, y);
                }
            }
        }

        if (player == null) {
            player = new Player(startPos != null ? startPos : new Position(WIDTH / 2, HEIGHT / 2));
        } else {
            player.setPosition(startPos != null ? startPos : new Position(WIDTH / 2, HEIGHT / 2));
            player.addTrap(1); // Give ammo on new level
        }

        // Main Level Loop
        boolean levelActive = true;

        // Final Level Logic: Change exit to Open Door
        if (level >= 5 && exitPos != null) { // Assuming 5 levels
             staticWorld[exitPos.x][exitPos.y] = Tileset.UNLOCKED_DOOR;
        }

        while (levelActive) {
            // Render
            renderGame(staticWorld, monsters, traps);
            drawHUD(staticWorld, monsters, traps);
            StdDraw.show(); // Show frame

            // Check Death
            if (player.getHealth() <= 0) {
                 drawDeathScreen();
                 return false;
            }

            // Input
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == ':') {
                     // Check for Quit command :Q
                     while (!StdDraw.hasNextKeyTyped()) {
                         // Wait for next key for combo
                         StdDraw.pause(10);
                     }
                     if (Character.toUpperCase(StdDraw.nextKeyTyped()) == 'Q') {
                         // Save and Quit logic here
                         return false;
                     }
                } else if (c == 'H') {
                    player.usePotion();
                } else if (c == 'P') {
                    if (player.placeTrap()) {
                        traps.add(new Trap(player.getPosition()));
                    }
                }

                // Movement
                int dx = 0, dy = 0;
                if (c == 'W') dy = 1;
                else if (c == 'S') dy = -1;
                else if (c == 'A') dx = -1;
                else if (c == 'D') dx = 1;

                if (dx != 0 || dy != 0) {
                    Position newPos = new Position(player.getPosition().x + dx, player.getPosition().y + dy);
                    TETile targetTile = staticWorld[newPos.x][newPos.y];

                    if (!targetTile.equals(Tileset.WALL) && !targetTile.equals(Tileset.NOTHING)) {
                        Monster collidedMonster = null;
                        for (Monster m : monsters) {
                            if (m.getPosition().x == newPos.x && m.getPosition().y == newPos.y) {
                                collidedMonster = m;
                                break;
                            }
                        }

                        if (collidedMonster != null) {
                            // Player defeats monster on contact but still takes collision damage.
                            player.takeDamage(1);
                            monsters.remove(collidedMonster);
                            player.setPosition(newPos);
                        } else {
                            player.setPosition(newPos);

                            if (exitPos != null && newPos.x == exitPos.x && newPos.y == exitPos.y) {
                                if (level >= 5) {
                                    drawWinScreen();
                                    return false;
                                }
                                return true;
                            }

                            if (targetTile.equals(Tileset.FLOWER)) {
                                double rand = Math.random();
                                if (rand < 0.4) {
                                    player.addPotion(1);
                                } else if (rand < 0.7) {
                                    player.addShield(2);
                                } else {
                                    player.addTrap(2);
                                }
                                player.addScore(50);
                                staticWorld[newPos.x][newPos.y] = Tileset.FLOOR;
                            }
                        }

                        updateMonsters(monsters, traps, rooms, staticWorld);
                    }
                }
            }

            StdDraw.pause(10); // Limit CPU usage
        }
        return false;
    }

    private void drawWinScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "YOU WIN! Score: " + player.getScore());
        StdDraw.show();
        StdDraw.pause(3000);
    }

    private void drawDeathScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.RED);
        Font font = new Font("Monaco", Font.BOLD, 60);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "YOU DIED");
        StdDraw.show();
        StdDraw.pause(3000);
    }

    private void updateMonsters(List<Monster> monsters, List<Trap> traps, List<RectangularRoom> rooms, TETile[][] world) {
        // Activate monsters
        RectangularRoom playerRoom = null;
        for (RectangularRoom room : rooms) {
            if (room.contains(player.getPosition())) {
                playerRoom = room;
                break;
            }
        }

        for (Monster m : monsters) {
            if (!m.isActive()) {
                // Check if player is in same room OR within REDUCED distance
                if (playerRoom != null && playerRoom.contains(m.getPosition())) {
                    m.setActive(true);
                } else if (Math.abs(m.getPosition().x - player.getPosition().x) + Math.abs(m.getPosition().y - player.getPosition().y) < 5) {
                     // Reduced Proximity activation (Manhattan dist < 5)
                     m.setActive(true);
                }
            }

            if (m.isActive()) {
                // Peek next move
                int dx = Integer.compare(player.getPosition().x, m.getPosition().x);
                int dy = Integer.compare(player.getPosition().y, m.getPosition().y);

                // If next move is player, attack
                if (m.getPosition().x + dx == player.getPosition().x && m.getPosition().y == player.getPosition().y) {
                    player.takeDamage(1);
                    // Don't move
                } else if (dx == 0 && m.getPosition().y + dy == player.getPosition().y) {
                    // Try Y move if X is aligned (or X didn't move)
                    player.takeDamage(1);
                    // Don't move
                } else {
                     // Check actual pathfinding logic from Monster class more accurately?
                     // Currently Monster.moveTowards just updates position.
                     // We need to change Monster logic or pre-calculate here.
                     // Making Monster NOT step on player:

                     // Saving old pos
                     int oldX = m.getPosition().x;
                     int oldY = m.getPosition().y;

                     m.moveTowards(player.getPosition(), world);

                     // If moved onto player, revert and attack
                     if (m.getPosition().x == player.getPosition().x && m.getPosition().y == player.getPosition().y) {
                         m.setPosition(new Position(oldX, oldY));
                         player.takeDamage(1);
                     }
                }

                // Check trap collision
                for (int i = 0; i < traps.size(); i++) {
                    Trap t = traps.get(i);
                    if (m.getPosition().x == t.getPosition().x && m.getPosition().y == t.getPosition().y) {
                        m.setActive(false); // Kill monster logic needs refinement?
                        // For now just teleport away or remove?
                        // I need to remove monster but iterating list safely.
                        // I will mark it inactive and handle removal later or just keep inactive.
                        // Ideally remove from list.
                        m.setPosition(new Position(-1, -1)); // Void
                        t.decreaseDurability();
                        if (t.getDurability() <= 0) {
                             traps.remove(i);
                             i--;
                        }
                    }
                }

                if (m.getPosition().x == player.getPosition().x && m.getPosition().y == player.getPosition().y) {
                    player.takeDamage(1);
                }
            }
        }
        monsters.removeIf(m -> m.getPosition().x == -1); // Remove dead monsters
    }

    private void renderGame(TETile[][] staticWorld, List<Monster> monsters, List<Trap> traps) {
        // Create temporary view
        TETile[][] view = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            System.arraycopy(staticWorld[x], 0, view[x], 0, HEIGHT);
        }

        // Draw traps
        for (Trap t : traps) {
             view[t.getPosition().x][t.getPosition().y] = t.getAppearance();
        }

        // Draw monsters
        for (Monster m : monsters) {
            view[m.getPosition().x][m.getPosition().y] = m.getAppearance();
        }

        // Draw player
        view[player.getPosition().x][player.getPosition().y] = player.getAppearance();

        ter.renderFrame(view, false); // Don't show yet
    }

    private void drawHUD(TETile[][] world, List<Monster> monsters, List<Trap> traps) {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1, HEIGHT - 1, "HP: " + player.getHealth() + " | Shld: " + player.getShield() + " | Pot: " + player.getPotionCount() + " | Trap: " + player.getTrapCount());
        StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "Level: " + level);
        StdDraw.line(0, HEIGHT - 2, WIDTH, HEIGHT - 2);

        // Mouse Hover
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
             String description = world[mouseX][mouseY].description();
             // Check entities
             if (player.getPosition().x == mouseX && player.getPosition().y == mouseY) description = "Player";
             for (Monster m : monsters) {
                  if (m.getPosition().x == mouseX && m.getPosition().y == mouseY) description = "Monster HP: " + 3; // Hardcoded HP for now
             }
             for (Trap t : traps) {
                  if (t.getPosition().x == mouseX && t.getPosition().y == mouseY) description = "Trap";
             }

             StdDraw.text(WIDTH / 2.0, HEIGHT - 1, description);
        }
    }


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        long seed = withdrawSeed(input);
        WorldGenerator generator = new WorldGenerator(seed, WIDTH, HEIGHT);
        TETile[][] finalWorldFrame = generator.generate();

        return finalWorldFrame;
    }

    private long withdrawSeed(String input) {
        input = input.toUpperCase();
        int nIndex = input.indexOf('N');
        int sIndex = input.indexOf('S');

        if (nIndex == -1 || sIndex == -1 || nIndex >= sIndex) {
            return 0;
        }

        String seedStr = input.substring(nIndex + 1, sIndex);
        try {
            return Long.parseLong(seedStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
