package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.Gdx;

/**
 * SimulationTestScript (Part 1)
 *
 * - Engine starts
 * - Scene lifecycle logs show load/unload order
 * - Controls show transitions
 * - Resize log shows forwarding works
 * - Close window shows clean shutdown
 */
public final class SimulationTestScript {

    private SimulationTestScript() {}

    public static void printInstructions() {
        Gdx.app.log("SIM", "==============================");
        Gdx.app.log("SIM", "Part 1 Simulation Controls:");
        Gdx.app.log("SIM", "MainMenu: SPACE -> Game");
        Gdx.app.log("SIM", "Game: ESC -> Settings, BACKSPACE -> MainMenu");
        Gdx.app.log("SIM", "Settings: BACKSPACE -> return to previous");
        Gdx.app.log("SIM", "Resize window: logs resize() calls");
        Gdx.app.log("SIM", "Close window: logs dispose() (clean end)");
        Gdx.app.log("SIM", "==============================");
        Gdx.app.log("Input Output", "Key Controls Binded to Actions:");
        Gdx.app.log("Input Output", "START_GAME: SPACE");
        Gdx.app.log("Input Output", "SETTINGS: ESC");
        Gdx.app.log("Input Output", "BACK: BACKSPACE");
        Gdx.app.log("Input Output", "SHOOT: Mouse LEFT BUTTON (also dashes in move direction)");
        Gdx.app.log("Input Output", "WASD: Move player");
        Gdx.app.log("Input Output", "R: Toggle follower AI (chase vs random wander)");
        Gdx.app.log("Input Output", "Collect yellow balls for speed boosts!");
    }

    public static void printScalingNote() {
        Gdx.app.log("SIM", "Scaling note: adding a new Scene class does NOT require changes to SceneManager.");
    }
}
