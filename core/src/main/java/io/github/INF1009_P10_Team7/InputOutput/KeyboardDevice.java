package io.github.INF1009_P10_Team7.InputOutput;

import com.badlogic.gdx.Gdx;

public class KeyboardDevice extends DeviceInput {
    private final boolean[] currentKeys = new boolean[256];
    private final boolean[] previousKeys = new boolean[256];

    public KeyboardDevice() {
        this.deviceID = 0;
        this.deviceName = "Keyboard";
    }

    @Override
    public void pollInput() {
        // Copy current state to previous state
        System.arraycopy(currentKeys, 0, previousKeys, 0, currentKeys.length);

        // Snapshot new hardware state
        for (int i = 0; i < 256; i++) {
            currentKeys[i] = Gdx.input.isKeyPressed(i);
        }
    }

    @Override
    public boolean getButton(int id) {
        if (id >= 0 && id < currentKeys.length) return currentKeys[id];
        return false;
    }

    @Override
    public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < currentKeys.length) {
            return currentKeys[id] && !previousKeys[id];
        }
        return false;
    }
}