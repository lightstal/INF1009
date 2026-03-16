package io.github.INF1009_P10_Team7.simulation.cyber.observer;

/**
 * Observer interface for game events (Observer Pattern).
 * Any class interested in game events implements this.
 * Keeps the game scene decoupled from UI/inventory systems (DIP).
 */
public interface IGameEventObserver {
    void onKeyCollected(int totalKeys, int requiredKeys);
    void onExitUnlocked();
    void onAlertLevelChanged(float alertLevel);
}
