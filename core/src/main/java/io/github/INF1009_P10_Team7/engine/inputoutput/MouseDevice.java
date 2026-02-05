package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.Gdx;

public class MouseDevice extends DeviceInput {
	private static final int BUTTON_COUNT = 5;
	
    private float mouseX;
    private float mouseY;
    private boolean[] currentButtons = new boolean[BUTTON_COUNT];
    private boolean[] previousButtons = new boolean[BUTTON_COUNT];
    
    public MouseDevice() {
        this.deviceID = 1;
        this.deviceName = "Mouse";
    }

    @Override
    public void pollInput() {
        // Get and update current X,Y coordinate of mouse
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        // Update buttons pressed// First, copy current state to previous state (Critical for JustPressed logic)
        System.arraycopy(currentButtons, 0, previousButtons, 0, BUTTON_COUNT);

        // Then, read new state from LibGDX hardware
        for (int i = 0; i < BUTTON_COUNT; i++) {
            currentButtons[i] = Gdx.input.isButtonPressed(i);
        }
    }

    @Override
    public boolean getButton(int id) {
    	if (id >= 0 && id < BUTTON_COUNT) {
            return currentButtons[id];
        }
        return false;
    }

    @Override
    public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < BUTTON_COUNT) {
            return currentButtons[id] && !previousButtons[id];
        }
        return false;
    }

    @Override
    public float getAxis(int id) {
        if (id == 0) return mouseX;
        if (id == 1) return mouseY;
        return 0;
    }
}