package io.github.INF1009_P10_Team7.engine.events;

public enum EventType {
	// --- Audio Events (Generic) ---
    PLAY_MUSIC,
    PLAY_SOUND,
    STOP_MUSIC,
    
    // --- Game Logic Events ---
    GAME_PAUSED,
    GAME_RESUMED,
    GAME_START,
    GAME_OVER
}
