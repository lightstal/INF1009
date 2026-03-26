package io.github.INF1009_P10_Team7.cyber.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Central event bus for game events (Observer Pattern - Subject/Publisher).
 * The game scene notifies this; HUD/inventory observe it.
 * Completely decouples game logic from UI rendering (OCP, DIP).
 */
public class GameEventSystem {

    private final List<IGameEventObserver> observers = new ArrayList<>();

    /**
     * Registers an observer to receive future game events.
     * Duplicate registrations are silently ignored.
     *
     * @param o the observer to add
     */
    public void addObserver(IGameEventObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }

    /**
     * Unregisters a previously added observer.
     *
     * @param o the observer to remove
     */
    public void removeObserver(IGameEventObserver o) {
        observers.remove(o);
    }

    /**
     * Notifies all observers that the player collected a key.
     *
     * @param total    total keys collected so far this run
     * @param required keys needed to unlock the exit
     */
    public void notifyKeyCollected(int total, int required) {
        for (IGameEventObserver o : observers) o.onKeyCollected(total, required);
    }

    /** Notifies all observers that the exit door has been unlocked. */
    public void notifyExitUnlocked() {
        for (IGameEventObserver o : observers) o.onExitUnlocked();
    }

    /**
     * Notifies all observers of a change in the drone alert level.
     *
     * @param level the new alert level in the range [0.0, 1.0]
     */
    public void notifyAlertChanged(float level) {
        for (IGameEventObserver o : observers) o.onAlertLevelChanged(level);
    }
}
