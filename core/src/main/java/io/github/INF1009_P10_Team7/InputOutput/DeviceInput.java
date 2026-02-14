package io.github.INF1009_P10_Team7.InputOutput;

public abstract class DeviceInput {
    protected int deviceID;
    protected String deviceName;

    /**
     * Returns the device name
     */
    public String getDeviceName() { return this.deviceName; }

    /**
     * Snapshots the hardware state. Must be called once per frame.
     */
    public abstract void pollInput();

    /**
     * Checks if a button is currently held down.
     */
    public abstract boolean getButton(int id);

    /**
     * Checks if a button was pressed THIS exact frame.
     */
    public abstract boolean isButtonJustPressed(int id);
}