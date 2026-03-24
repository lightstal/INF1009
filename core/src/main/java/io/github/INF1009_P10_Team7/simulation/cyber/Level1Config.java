package io.github.INF1009_P10_Team7.simulation.cyber;

import io.github.INF1009_P10_Team7.simulation.cyber.ctf.NmapReconChallenge;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.*;

/**
 * Level 1 – RECON LAB configuration (Strategy Pattern).
 */
public class Level1Config implements LevelConfig {

    @Override public int    getLevelNumber()   { return 1; }
    @Override public String getLevelName()     { return "LEVEL 1  -  RECON LAB"; }
    @Override public String getIntroSubtitle() {
        return "No drones yet. Learn the terminals, checkpoints, and signal ping.";
    }

    @Override public String getMapFile()         { return "maps/Level1.tmx"; }
    @Override public String getCollisionLayer()  { return "collision"; }
    @Override public String getWallLayer()       { return "Walls"; }
    @Override public String getDoorLayer()       { return "doors"; }

    @Override
    public IMiniGame[] createChallenges(TerminalEmulator terminal) {
        return new IMiniGame[]{
            new BinaryDecodeGame(),
            new CaesarCipherGame(),
            new PortMatchGame(),
            new LogAnalysisGame(),
            new TerminalMiniGame(new NmapReconChallenge(), terminal)
        };
    }

    @Override public int      getKeysRequired()  { return 5; }
    @Override public float    getTimeLimit()     { return 360f; }
    @Override public DroneAI[] createDrones()    { return new DroneAI[]{}; }
    @Override public int[]    getPlayerStartTile() { return new int[]{ 19, 12 }; }

    @Override public int[][] getCameraPositions() {
        return new int[][]{ {19, 8, 270} };
    }
    @Override public int[][] getLightPositions() {
        return new int[][]{ {19, 5}, {19, 12}, {19, 18} };
    }
}
