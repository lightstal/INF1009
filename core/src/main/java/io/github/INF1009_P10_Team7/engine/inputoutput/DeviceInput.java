package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * Abstract base class representing a physical input device.
 * <p>
 * This class serves as a contract for specific hardware implementations such as:
 * <ul>
 * <li>{@link KeyboardDevice} - For keyboard keys.</li>
 * <li>{@link MouseDevice} - For mouse buttons and cursor coordinates.</li>
 * </ul>
 * <p>
 * It enforces a standard interface so that the {@link InputOutputManager} can treat
 * all devices uniformly (Polymorphism).
 */
abstract class DeviceInput {
	/** * Unique integer identifier for the device (e.g., 0 for Keyboard, 1 for Mouse). 
     */
    protected int deviceID;
    
    /** * Human-readable name of the device (e.g., "Keyboard", "Mouse"). 
     */
    protected String deviceName;

    /**
     * Updates the internal state of the device.
     * <p>
     * This method must be called <b>once per frame</b> (usually in the game loop).
     * It is responsible for:
     * <ol>
     * <li>Copying the "current" state to the "previous" state (for edge detection).</li>
     * <li>Polling the actual hardware (via LibGDX) to get the new state.</li>
     * </ol>
     */
    public abstract void pollInput();

    /**
     * Checks if a specific button or key is currently held down.
     * @param id The integer code representing the key (e.g., Input.Keys.A) or button.
     * @return {@code true} if the button is currently pressed down, {@code false} otherwise.
     */
    public abstract boolean getButton(int id);

    /**
     * Checks if a specific button or key was pressed <b>this frame</b>.
     * <p>
     * This is useful for "trigger" actions like jumping, shooting, or menu selection,
     * where you only want the action to happen once per click, not every frame while held.
     * @param id The integer code representing the key or button.
     * @return {@code true} only on the exact frame the button transitioned from UP to DOWN.
     */
    public abstract boolean isButtonJustPressed(int id);

    /**
     * Retrieves the value of an analog axis (e.g., Mouse coordinates, Joystick tilt).
     * @param id The identifier for the axis (e.g., 0 for X-axis, 1 for Y-axis).
     * @return A float value representing the position or intensity of the axis.
     */
    public abstract float getAxis(int id);
}
