package io.github.INF1009_P10_Team7.InputOutput;

import com.badlogic.gdx.Gdx;

public class MouseDevice extends DeviceInput {
    private static final int BUTTON_COUNT = 5;
    private final boolean[] currentButtons = new boolean[BUTTON_COUNT];
    private final boolean[] previousButtons = new boolean[BUTTON_COUNT];

    public MouseDevice() {
        this.deviceID = 1;
        this.deviceName = "Mouse";
    }

    @Override
    public void pollInput() {
        System.arraycopy(currentButtons, 0, previousButtons, 0, BUTTON_COUNT);

        for (int i = 0; i < BUTTON_COUNT; i++) {
            currentButtons[i] = Gdx.input.isButtonPressed(i);
        }
    }

    @Override
    public boolean getButton(int id) {
        if (id >= 0 && id < BUTTON_COUNT) return currentButtons[id];
        return false;
    }

    @Override
    public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < BUTTON_COUNT) {
            return currentButtons[id] && !previousButtons[id];
        }
        return false;
    }
}