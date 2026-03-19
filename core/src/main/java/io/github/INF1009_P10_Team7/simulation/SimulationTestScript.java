package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.Gdx;

/**
 * <p>Utility class that prints simulation control instructions and
 * scaling notes to the console log at startup.</p>
 *
 * <p>This is a final class with a private constructor to prevent
 * instantiation  -  all methods are static.</p>
 *
 * <p>Test script verifies:</p>
 * <ul>
 *   <li>Engine starts successfully</li>
 *   <li>Scene lifecycle logs show correct load/unload order</li>
 *   <li>Controls trigger scene transitions</li>
 *   <li>Resize log confirms viewport forwarding works</li>
 *   <li>Closing the window triggers a clean shutdown</li>
 * </ul>
 */
public final class SimulationTestScript {

    /** <p>Private constructor to prevent instantiation of this utility class.</p> */
    private SimulationTestScript() {}

    /**
     * <p>Prints all simulation control instructions to the console log.
     * Includes scene navigation keys, movement keys, mouse controls,
     * and gameplay tips.</p>
     */
    public static void printInstructions() {
        Gdx.app.log("SIM", "==============================");
        Gdx.app.log("SIM", "Cyber Maze Escape Controls:");
        Gdx.app.log("SIM", "Main Menu: SPACE/ENTER/click -> continue");
        Gdx.app.log("SIM", "Game: ESC -> Settings, Q -> Main Menu, E -> Interact, H -> Signal Ping");
        Gdx.app.log("SIM", "Settings: change volume and rebind gameplay + menu keys");
        Gdx.app.log("SIM", "Resize window: logs resize() calls");
        Gdx.app.log("SIM", "Close window: logs dispose() (clean end)");
        Gdx.app.log("SIM", "==============================");
        Gdx.app.log("Input Output", "Key Controls Binded to Actions:");
        Gdx.app.log("Input Output", "START_GAME: SPACE");
        Gdx.app.log("Input Output", "SETTINGS: ESC");
        Gdx.app.log("Input Output", "BACK: Q");
        Gdx.app.log("Input Output", "WASD: Move player");
        Gdx.app.log("Input Output", "E: Interact with terminal");
        Gdx.app.log("Input Output", "Arrow keys / ENTER: menu navigation");
    }

    /**
     * <p>Prints a note explaining that adding a new Scene class does not
     * require any changes to the SceneManager (OCP).</p>
     */
    public static void printScalingNote() {
        Gdx.app.log("SIM", "Scaling note: adding a new Scene class does NOT require changes to SceneManager.");
    }
}
