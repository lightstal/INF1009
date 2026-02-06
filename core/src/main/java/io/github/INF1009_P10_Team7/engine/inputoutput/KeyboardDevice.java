package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.Gdx;

/**
 * A concrete implementation of {@link DeviceInput} for Keyboard handling.
 * <p>
 * This class captures the state of the physical keyboard every frame.
 * It maintains two arrays of booleans (current and previous) to detect state changes,
 * allowing for "Just Pressed" (Edge Trigger) detection.
 */
public class KeyboardDevice extends DeviceInput {
	
	/** 
	 * Stores the state of keys for the current frame.
	 */
    private boolean[] currentKeys = new boolean[256];
    
    /** 
     * Stores the state of keys from the previous frame.
     */
    private boolean[] previousKeys = new boolean[256];
    
    /**
     * Initializes the keyboard device with ID 0.
     */
	public KeyboardDevice() {
        this.deviceID = 0;
        this.deviceName = "Keyboard";
    }
	
	/**
     * Updates the internal key states.
     * <p>
     * Logic:
     * <ol>
     * <li><b>Snapshot:</b> Copies the entire {@code currentKeys} array into {@code previousKeys}.
     * This preserves the state of the keyboard <i>before</i> the new update.</li>
     * <li><b>Update:</b> Queries LibGDX ({@code Gdx.input.isKeyPressed}) for every key code (0-255)
     * to populate the new {@code currentKeys} array.</li>
     * </ol>
     */
	@Override
    public void pollInput() {
        // Copy current state to previous state (for "Just Pressed" detection)
        System.arraycopy(currentKeys, 0, previousKeys, 0, currentKeys.length);

        // Update current state directly from Gdx.input
        // Iterate through all possible key codes to snapshot the state
        for (int i = 0; i < 256; i++) {
            currentKeys[i] = Gdx.input.isKeyPressed(i);
        }
    }
	
	/**
     * Checks if a key is currently held down.
     * @param id The Key Code (0-255).
     * @return {@code true} if the key is down.
     */
	@Override
	public boolean getButton(int id) {
	    if (id >= 0 && id < currentKeys.length) {
	        return currentKeys[id];
	    }
	    return false;
	}

	/**
     * Checks if a key was pressed <b>this exact frame</b>.
     * <p>
     * Logic:
     * Returns {@code true} ONLY if the key is currently down ({@code currentKeys[id]})
     * AND was NOT down in the last frame ({@code !previousKeys[id]}).
     * @param id The Key Code (0-255).
     * @return {@code true} if this is the rising edge of the key press.
     */
    @Override
	public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < currentKeys.length) {
            // It is pressed NOW, but was NOT pressed LAST frame
            return currentKeys[id] && !previousKeys[id];
        }
        return false;
    }
	
    /**
     * Returns 0 as keyboards do not have analog axes.
     */
	@Override
	public float getAxis(int id) {
	    return 0; 
	}

}
