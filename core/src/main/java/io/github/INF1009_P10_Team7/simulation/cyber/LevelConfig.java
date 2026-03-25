package io.github.INF1009_P10_Team7.simulation.cyber;

import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.IMiniGame;

/**
 * Strategy interface for per-level configuration.
 *
 * SRP  – all level-specific data lives here, not in CyberGameScene.
 * OCP  – adding a new level only requires a new implementation;
 *        CyberGameScene is never modified.
 */
public interface LevelConfig {
    /** @return the 1-based level index (e.g. 1, 2, …) */
    int       getLevelNumber();
    /** @return the display name shown in the HUD and intro banner */
    String    getLevelName();
    /** @return the subtitle shown in the level-intro cutscene */
    String    getIntroSubtitle();

    // Map loading
    /** @return the internal asset path to the Tiled .tmx map file */
    String    getMapFile();
    /** @return the Tiled object layer name that contains collision rectangles */
    String    getCollisionLayer();
    /** @return the Tiled tile layer name used for wall rendering and LOS */
    String    getWallLayer();
    /** @return the Tiled object layer name that contains the exit door object */
    String    getDoorLayer();

    // Gameplay config
    IMiniGame[] createChallenges(TerminalEmulator terminal);
    /** @return the number of terminals the player must solve to unlock the exit */
    int         getKeysRequired();
    /** @return the level time limit in seconds (use 0 for untimed) */
    float       getTimeLimit();
    /** @return array of pre-configured drone instances for this level (may be empty) */
    DroneAI[]   createDrones();
    /** @return {@code [col, row]} of the player's spawn tile in map coordinates */
    int[]       getPlayerStartTile();

    // Rendering helpers
    /**
     * @return array of {@code [col, row, facingDeg]} for each CCTV camera in the level
     */
    int[][] getCameraPositions();
    /**
     * @return array of {@code [col, row]} for each ceiling light fixture in the level
     */
    int[][] getLightPositions();
}
