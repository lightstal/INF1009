package io.github.INF1009_P10_Team7.cyber.player;

public enum PlayerState {
    IDLE, MOVING, HACKING, SCANNING;
    public String getDisplayName() {
        switch (this) {
            case HACKING: return "JACKING IN";
            case SCANNING: return "SCANNING";
            case MOVING: return "MOVING";
            case IDLE:
            default: return "STANDBY";
        }
    }
}
