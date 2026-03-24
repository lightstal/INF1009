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
    int       getLevelNumber();
    String    getLevelName();
    String    getIntroSubtitle();

    // Map loading
    String    getMapFile();
    String    getCollisionLayer();
    String    getWallLayer();
    String    getDoorLayer();

    // Gameplay config
    IMiniGame[] createChallenges(TerminalEmulator terminal);
    int         getKeysRequired();
    float       getTimeLimit();
    DroneAI[]   createDrones();
    int[]       getPlayerStartTile();

    // Rendering helpers
    int[][] getCameraPositions();
    int[][] getLightPositions();
}
