package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * Interface that defines how Scenes interact with the IO system.
 * This hides the complex implementation details from the game logic.
 */
public interface InputOutput {
	
	// --- Input Binding ---
    void bindKey(String actionName, int keyCode);
    void bindMouseButton(String actionName, int buttonCode);
    
    // --- Input Checking ---
    boolean isActionPressed(String actionName);      // Held down
    boolean isActionJustPressed(String actionName);  // Clicked once
    
    // --- Audio Control ---
    void playMusic(String audioPath);
    void playSound(String audioPath);
    void setMusicState(String state); // "playing", "paused", "stopped"
}