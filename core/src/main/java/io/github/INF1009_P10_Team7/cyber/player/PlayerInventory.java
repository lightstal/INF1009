package io.github.INF1009_P10_Team7.cyber.player;

import io.github.INF1009_P10_Team7.cyber.observer.IGameEventObserver;

public class PlayerInventory implements IGameEventObserver {
    private int keysCollected = 0;
    private int keysRequired = 3;
    private boolean exitUnlocked = false;
    private float alertLevel = 0f;

    @Override public void onKeyCollected(int total, int required) { keysCollected = total; keysRequired = required; }
    @Override public void onExitUnlocked() { exitUnlocked = true; }
    @Override public void onAlertLevelChanged(float level) { alertLevel = level; }

    public int getKeysCollected() { return keysCollected; }
    public int getKeysRequired() { return keysRequired; }
    public boolean isExitUnlocked() { return exitUnlocked; }
    public float getAlertLevel() { return alertLevel; }
}
