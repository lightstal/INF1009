package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * IInputController, unified input binding and query interface.
 *
 * <p>Provides a device-agnostic layer over keyboard and mouse input.
 * Callers bind abstract action names (e.g. {@code "JUMP"}, {@code "LEFT"})
 * to physical key/button codes via {@link #bindInput}, then poll those
 * actions by name without caring which device they came from.</p>
 *
 * <p>Design note (DIP): scenes depend on this interface, not on the
 * concrete {@link InputOutputManager} or any LibGDX {@code Gdx.input} call.
 * This isolates scenes from input-hardware details and makes the binding
 * rebindable at runtime (used by the settings scene).</p>
 *
 * <p>Device ID convention (matches {@link DeviceInput} base offsets):</p>
 * <ul>
 * <li>{@code 0}, Keyboard (base offset 0)</li>
 * <li>{@code 1}, Mouse (base offset 300)</li>
 * </ul>
 */

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
    
    // --- Text Input Management ---
    
    /**
     * Registers a listener to receive raw text events directly from the OS.
     * <p>
     * This is used by UI elements like terminals or text boxes that require
     * OS-level character mapping (e.g., shift-modifiers) and key-repeating,
     * which standard state-polling cannot provide. Only one text listener
     * can be active at a time.
     *
     * @param listener The object (e.g., TerminalEmulator) that will receive typing events.
     */
    void setTextInputListener(ITextInputListener listener);

    /**
     * Unregisters the current text listener and releases the OS input processor.
     * <p>
     * Must be called when the UI element requesting text input is closed to
     * return input control to the standard game loop.
     */
    void clearTextInputListener();

    /**
     * Interface for UI elements that need raw typing data.
     * Implementing classes will receive direct callbacks from the engine when keys are struck.
     */
    interface ITextInputListener {
        
        /**
         * Called when a printable character or a backspace is typed.
         *
         * @param c The resolved character typed by the user (accounts for Shift/Caps Lock).
         */
        void onCharTyped(char c);
        
        /**
         * Called when a non-printable control/navigation key is pressed.
         * <p>
         * Useful for handling keys that require OS-level key-repeating when held down,
         * such as Arrow keys for history navigation, or Page Up/Page Down for scrolling.
         *
         * @param libgdxKeyCode The raw integer keycode from {@link com.badlogic.gdx.Input.Keys}.
         */
        void onControlKeyPressed(int libgdxKeyCode);
    }
}