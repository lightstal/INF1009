package io.github.INF1009_P10_Team7.simulation.cyber;

/**
 * PlayerState — tracks the player's current action for state-based
 * sprite rendering and gameplay logic.
 *
 * Used by CyberGameScene to pick the correct animation frame
 * and by the clue system to gate interactions.
 */
public enum PlayerState {
    IDLE,
    MOVING,
    HACKING,
    SCANNING;

    /** Display name for HUD / debug. */
    public String getDisplayName() {
        switch (this) {
            case HACKING:       return "JACKING IN";
            case SCANNING:      return "SCANNING";
            case MOVING:        return "MOVING";
            case IDLE: default: return "STANDBY";
        }
    }
}
