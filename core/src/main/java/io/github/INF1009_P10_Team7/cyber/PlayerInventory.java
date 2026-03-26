package io.github.INF1009_P10_Team7.cyber;

import io.github.INF1009_P10_Team7.cyber.observer.IGameEventObserver;

/**
 * Tracks the player's collected keys and game state flags.
 * Implements IGameEventObserver (Observer Pattern) so it stays in sync
 * when terminals are solved  -  without tight coupling to the scene.
 */
public class PlayerInventory implements IGameEventObserver {

    /** Total keys (terminal access codes) the player has collected this run. */
    private int keysCollected  = 0;
    /** Keys needed to unlock the exit for the current level. */
    private int keysRequired   = 3;
    /** Set to {@code true} when all required keys have been collected. */
    private boolean exitUnlocked = false;
    /** Current drone alert level: 0.0 = no threat, 1.0 = active chase. */
    private float alertLevel   = 0f;

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
