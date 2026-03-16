package io.github.INF1009_P10_Team7.simulation.cyber.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Central event bus for game events (Observer Pattern - Subject/Publisher).
 * The game scene notifies this; HUD/inventory observe it.
 * Completely decouples game logic from UI rendering (OCP, DIP).
 */
public class GameEventSystem {

    private final List<IGameEventObserver> observers = new ArrayList<>();

    public void addObserver(IGameEventObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }

    public void removeObserver(IGameEventObserver o) {
        observers.remove(o);
    }

    public void notifyKeyCollected(int total, int required) {
        for (IGameEventObserver o : observers) o.onKeyCollected(total, required);
    }

    public void notifyExitUnlocked() {
        for (IGameEventObserver o : observers) o.onExitUnlocked();
    }

    public void notifyAlertChanged(float level) {
        for (IGameEventObserver o : observers) o.onAlertLevelChanged(level);
    }
}
