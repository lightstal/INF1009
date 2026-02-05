package io.github.INF1009_P10_Team7.engine.inputoutput;

abstract class DeviceInput {
	protected int deviceID;
	protected String deviceName;

	public abstract void pollInput();
	public abstract boolean getButton(int id);
	public abstract boolean isButtonJustPressed(int id);
	public abstract float getAxis(int id);
}
