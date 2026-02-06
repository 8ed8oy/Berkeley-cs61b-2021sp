package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

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
                    startGame(seed);
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
        StdDraw.text(WIDTH / 2, HEIGHT * 0.75, "CS61B: THE GAME");

        Font smallFont = new Font("Monaco", Font.PLAIN, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.5, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.45, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "Quit (Q)");
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
        StdDraw.text(WIDTH / 2, HEIGHT * 0.6, "Enter Seed (Press S to start):");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.5, s);
        StdDraw.show();
    }

    private void startGame(long seed) {
        WorldGenerator generator = new WorldGenerator(seed, WIDTH, HEIGHT);
        TETile[][] world = generator.generate();

        // Find a valid starting position
        Position playerPos = new Position(WIDTH / 2, HEIGHT / 2);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    playerPos = new Position(x, y);
                    break;
                }
            }
        }

        // Game Loop
        while (true) {
            // Render
            ter.renderFrame(world);
            // Draw player (simple overlay for now, or update world array)
            // Ideally we separate view from model, but for Phase 1 MVP we can hack it
            // Draw player on top manually using StdDraw content?
            // Better: update the world array temporarily for rendering.

            // To properly render the player, we usually create a copy of the view
            // or use specific draw calls. For simplicity:
            world[playerPos.x][playerPos.y] = Tileset.AVATAR;
            ter.renderFrame(world);
            world[playerPos.x][playerPos.y] = Tileset.FLOOR; // Restore after render (hacky)

            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                int dx = 0;
                int dy = 0;
                if (c == 'W') dy = 1;
                else if (c == 'S') dy = -1;
                else if (c == 'A') dx = -1;
                else if (c == 'D') dx = 1;
                else if (c == ':') {
                    // check for Q loop
                     while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                             char c2 = Character.toUpperCase(StdDraw.nextKeyTyped());
                             if (c2 == 'Q') {
                                 // Save and Quit logic
                                 System.exit(0);
                             } else {
                                 break;
                             }
                        }
                    }
                }

                // Move check
                if (!world[playerPos.x + dx][playerPos.y + dy].equals(Tileset.WALL)) {
                    playerPos = new Position(playerPos.x + dx, playerPos.y + dy);
                }
            }
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
