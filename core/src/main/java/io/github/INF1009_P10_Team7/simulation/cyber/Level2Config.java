package io.github.INF1009_P10_Team7.simulation.cyber;

import io.github.INF1009_P10_Team7.simulation.cyber.ctf.SqlInjectionChallenge;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.*;

/**
 * Level 2 – NETWORK HUB configuration (Strategy Pattern).
 */
public class Level2Config implements LevelConfig {

    @Override public int    getLevelNumber()   { return 2; }
    @Override public String getLevelName()     { return "LEVEL 2  -  NETWORK HUB"; }
    @Override public String getIntroSubtitle() {
        return "Break line of sight at corners and do not rush into the center lane.";
    }

    @Override public String getMapFile()        { return "maps/Level2.tmx"; }
    @Override public String getCollisionLayer() { return "collision"; }
    @Override public String getWallLayer()      { return "Walls"; }
    @Override public String getDoorLayer()      { return "door"; }

    @Override
    public IMiniGame[] createChallenges(TerminalEmulator terminal) {
        return new IMiniGame[]{
            new BinaryDecodeGame(),
            new PacketSnifferGame(),
            new PortMatchGame(),
            new LogAnalysisGame(),
            new TerminalMiniGame(new SqlInjectionChallenge(), terminal)
        };
    }

    @Override public int   getKeysRequired()  { return 5; }
    @Override public float getTimeLimit()     { return 390f; }

    @Override
    public DroneAI[] createDrones() {
        return new DroneAI[]{
            new DroneAI(TileMap.tileCentreX(13), TileMap.tileCentreY(11),
                new float[][]{ {11,10}, {17,10}, {17,12}, {11,12} }),
            new DroneAI(TileMap.tileCentreX(25), TileMap.tileCentreY(11),
                new float[][]{ {22,10}, {28,10}, {28,12}, {22,12} })
        };
    }

    @Override public int[] getPlayerStartTile() { return new int[]{ 19, 11 }; }

    @Override public int[][] getCameraPositions() {
        return new int[][]{ {8,8,270}, {27,8,270}, {8,14,90}, {27,14,90} };
    }
    @Override public int[][] getBarrierPositions() {
        return new int[][]{ {13,11}, {15,11}, {24,11}, {26,11} };
    }
    @Override public int[][] getLightPositions() {
        return new int[][]{ {7,6}, {30,6}, {19,11}, {7,17}, {30,17} };
    }
}
