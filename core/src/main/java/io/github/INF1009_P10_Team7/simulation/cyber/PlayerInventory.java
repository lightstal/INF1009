package io.github.INF1009_P10_Team7.simulation.cyber;

import io.github.INF1009_P10_Team7.simulation.cyber.observer.IGameEventObserver;

/**
 * Tracks the player's collected keys and game state flags.
 * Implements IGameEventObserver (Observer Pattern) so it stays in sync
 * when terminals are solved — without tight coupling to the scene.
 */
public class PlayerInventory implements IGameEventObserver {

    private int keysCollected  = 0;
    private int keysRequired   = 3;
    private boolean exitUnlocked = false;
    private float alertLevel   = 0f;   // 0 = calm, 1 = full chase

    @Override
    public void onKeyCollected(int total, int required) {
        keysCollected = total;
        keysRequired  = required;
    }

    @Override
    public void onExitUnlocked() {
        exitUnlocked = true;
    }

    @Override
    public void onAlertLevelChanged(float level) {
        alertLevel = level;
    }

    public int  getKeysCollected()  { return keysCollected; }
    public int  getKeysRequired()   { return keysRequired;  }
    public boolean isExitUnlocked() { return exitUnlocked;  }
    public float getAlertLevel()    { return alertLevel;     }
}
