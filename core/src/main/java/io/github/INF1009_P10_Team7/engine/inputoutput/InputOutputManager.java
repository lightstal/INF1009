package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

/**
 * The concrete implementation of the {@link InputController} interface.
 * <p>
 * This class serves as the <b>Central Manager</b> for all engine input and output.
 * It is responsible for:
 * <ul>
 * <li>Aggregating specific hardware devices (Keyboard, Mouse).</li>
 * <li>Managing the mapping (binding) between abstract Action Names ("JUMP") and physical keys.</li>
 * <li>Polling hardware state every frame.</li>
 * <li>Delegating audio requests to the {@link AudioOutput} system.</li>
 * </ul>
 */
public class InputOutputManager implements InputController{
	
	/**
     * An arbitrary offset added to mouse button codes to distinguish them from keyboard key codes.
     * <p>
     * Keyboard keys usually range from 0-255. By adding 300 to mouse buttons,
     * we ensure a mouse click (code 0) acts as code 300, preventing overlap with keyboard key 0.
     */
    private static final int MOUSE_OFFSET = 300;
	
	private AudioOutput audioOutput;
	private KeyboardDevice keyboard;
    private MouseDevice mouse;

    /**
     * Stores the current input bindings.
     * <p>
     * Map structure:</br>
     * {@code Action Name (String) -> Key/Button Code (Integer)}.
     */
    private Map<String, Integer> keyBindings;
    
    /**
     * Initializes the InputOutputManager and its sub-components (Audio, Keyboard, Mouse).
     */
	public InputOutputManager() {
		this.audioOutput = new AudioOutput();
		this.keyboard = new KeyboardDevice();
        this.mouse = new MouseDevice();
        
        this.keyBindings = new HashMap<>();
	}
	
	public AudioOutput getAudioOutput() {
        return audioOutput;
    }

	// --- Lifecycle Methods ---
    
    /**
     * Updates the state of all input devices.
     * <p>
     * <b>Must be called once per frame</b> at the start of the game loop.
     * This triggers the {@code pollInput()} method on devices, which updates
     * their internal "Current" and "Previous" state arrays to allow for "Just Pressed" detection.
     */
	public void update() {
		keyboard.pollInput();
        mouse.pollInput();
	}
	
	/**
     * Cleans up resources, specifically disposing of audio assets.
     */
    public void dispose() {
        audioOutput.dispose();
    }
    
    
    // --- Input Binding Implementation ---

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindKey(String actionName, int keyCode) {
        keyBindings.put(actionName, keyCode);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Internally adds {@link #MOUSE_OFFSET} to the button code to store it uniquely in the map.
     */
    @Override
    public void bindMouseButton(String actionName, int buttonCode) {
        keyBindings.put(actionName, MOUSE_OFFSET + buttonCode);
    }
    
    
    
    // --- Input Checking Implementation ---
    
    /**
     * {@inheritDoc}
     * <p>
     * Logic:
     * <ol>
     * <li>Retrieves the integer code for the action.</li>
     * <li>If code >= 300, it subtracts the offset and checks the {@link MouseDevice}.</li>
     * <li>Otherwise, it checks the {@link KeyboardDevice}.</li>
     * </ol>
     */
    @Override
    public boolean isActionPressed(String actionName) {
        if (keyBindings.containsKey(actionName)) {
            int key = keyBindings.get(actionName);
            if (key >= MOUSE_OFFSET) {
                return mouse.getButton(key - MOUSE_OFFSET);                 
            } else {
                return keyboard.getButton(key);
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActionJustPressed(String actionName) {
        if (keyBindings.containsKey(actionName)) {
            int key = keyBindings.get(actionName);
            if (key >= MOUSE_OFFSET) {
                return mouse.isButtonJustPressed(key - MOUSE_OFFSET);                 
            } else {
                return keyboard.isButtonJustPressed(key);
            }
        }
        return false;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public float getMouseX() {
        // In MouseDevice, you defined axis 0 as X
        return mouse.getAxis(0); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMouseY() {
        // In MouseDevice, you defined axis 1 as Y
        return mouse.getAxis(1);
    }
}
