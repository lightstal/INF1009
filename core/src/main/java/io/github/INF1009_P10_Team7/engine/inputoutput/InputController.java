package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * Interface that defines how Scenes interact with the IO system.
 * This hides the complex implementation details from the game logic.
 */
public interface InputController {
	
	// --- Input Binding ---
	
	/**
     * Binds a physical keyboard key to a logical action name.
     * <p>
     * Example: {@code bindKey("JUMP", Input.Keys.SPACE);}
     * @param actionName The string identifier for the action (case-sensitive).
     * @param keyCode The integer code of the key (from {@link com.badlogic.gdx.Input.Keys}).
     */
    void bindKey(String actionName, int keyCode);
    
    /**
     * Binds a mouse button to a logical action name.
     * <p>
     * Example: {@code bindMouseButton("SHOOT", Input.Buttons.LEFT);}
     * @param actionName The string identifier for the action.
     * @param buttonCode The integer code of the button (from {@link com.badlogic.gdx.Input.Buttons}).
     */
    void bindMouseButton(String actionName, int buttonCode);
    
    
    
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
}