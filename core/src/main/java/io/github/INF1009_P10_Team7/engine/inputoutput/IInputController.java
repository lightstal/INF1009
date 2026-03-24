package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * Interface that defines how Scenes interact with the IO system.
 * This hides the complex implementation details from the game logic.
 */
public interface IInputController {
	
	// --- Device Management ---
    
    /**
     * Registers a new hardware device into the input engine dynamically.
     * <p>
     * Allows external modules to inject custom input handlers (e.g., Gamepads, 
     * Steering Wheels, VR Controllers) without modifying engine internals.
     * * @param device The implemented {@link DeviceInput} to add to the system.
     */
    void registerDevice(DeviceInput device);
	
	// --- Input Binding ---

	/**
     * Binds a physical input from any registered device to a logical action name.
     * <p>
     * This unified method replaces device-specific binding methods. It uses the 
     * {@code deviceID} to dynamically route the input to the correct hardware 
     * and calculates the global routing code automatically.
     * <p>
     * Example Uses:
     * <ul>
     * <li>{@code bindInput("JUMP", 0, Input.Keys.SPACE);} - Binds Keyboard Spacebar to "JUMP" action</li>
     * <li>{@code bindInput("SHOOT", 1, Input.Buttons.LEFT);} - Binds Mouse Left-Click to "SHOOT" action</li>
     * </ul>
     * @param actionName The string identifier for the action (case-sensitive).
     * @param deviceID The unique ID of the target device (e.g., 0 for Keyboard, 1 for Mouse).
     * @param localCode The raw integer code of the button/key from LibGDX.
     */
    void bindInput(String actionName, int deviceID, int localCode);
    
    
    // --- Input Checking ---
    
    /**
     * Checks if the key/button bound to the given action is currently held down.
     * <p>
     * Use this for continuous actions like movement (walking, driving).
     * @param actionName The logical name of the action to check.
     * @return {@code true} if the bound input is currently pressed.
     */
    boolean isActionPressed(String actionName);
    
    /**
     * Checks if the key/button bound to the given action was pressed <b>this frame</b>.
     * <p>
     * Use this for single-trigger actions like jumping, shooting, or menu selection.
     * It ensures the action only fires once per click.
     * @param actionName The logical name of the action to check.
     * @return {@code true} only on the frame the input went from UP to DOWN.
     */
    boolean isActionJustPressed(String actionName);

     // --- Mouse Coordinates ---
     /**
     * Retrieves the current X coordinate of the mouse.
     * @return The x-coordinate in screen pixels (0 is left).
     */
    float getMouseX();

    /**
     * Retrieves the current Y coordinate of the mouse.
     * @return The y-coordinate in screen pixels (0 is top).
     */
    float getMouseY();

     // to getkeyname from any keys press
    String getKeyName(String action);

    // listener for/to get the next key
    void listenForNextKey(InputCallback callback);
    
    // interface for callback for better oop
    interface InputCallback {
        void onInputReceived(int keycode, int localCode);
    }
}