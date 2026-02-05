package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.Gdx;

public class KeyboardDevice extends DeviceInput {
	private boolean[] currentKeys = new boolean[256];
	private boolean[] previousKeys = new boolean[256];
	
	public KeyboardDevice() {
        this.deviceID = 0;
        this.deviceName = "Keyboard";
    }
	
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
	
	@Override
	public boolean getButton(int id) {
	    if (id >= 0 && id < currentKeys.length) {
	        return currentKeys[id];
	    }
	    return false;
	}

    @Override
	public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < currentKeys.length) {
            // It is pressed NOW, but was NOT pressed LAST frame
            return currentKeys[id] && !previousKeys[id];
        }
        return false;
    }
	
	@Override
	public float getAxis(int id) {
	    return 0; 
	}

}
