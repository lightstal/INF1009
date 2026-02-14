package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


/**
 * The concrete implementation of the {@link IInputController} interface.
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
public class InputOutputManager implements IInputController, IAudioController{
	
	/**
     * An arbitrary offset added to mouse button codes to distinguish them from keyboard key codes.
     * <p>
     * Keyboard keys usually range from 0-255. By adding 300 to mouse buttons,
     * we ensure a mouse click (code 0) acts as code 300, preventing overlap with keyboard key 0.
     */
    private static final int MOUSE_OFFSET = 300;
	
	private AudioOutput audioOutput;
	private DeviceInput keyboard;
    private DeviceInput mouse;

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
        Gdx.app.log("InputOutputManager", "InputOutputManager disposed");
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

     // Get key code when pressed
    @Override
    public String getKeyName(String action) {
        if (keyBindings.containsKey(action)) {
            int keycode = keyBindings.get(action);

            // get keycode for mouse
            if (keycode == Input.Keys.LEFT)
                return "L-CLICK"; // return 0
            if (keycode == Input.Keys.RIGHT)
                return "R-CLICK"; // return 1

            // when keycode more than 300, it will crash.
            if (keycode >= 255) {
                return "MOUSE";
            }

            // conversion for keyboard keys
            if (keycode >= 0 && keycode <= 255) {
                return Input.Keys.toString(keycode);
            }

            return "UNKNOWN";
        }
        return "NONE";
    }

    // add listener for next key press
    @Override
    public void listenForNextKey(final IInputController.InputCallback callback) {

        // set a temporary processor to catch exactly ONE key
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // send the key back
                callback.onInputReceived(keycode);

                // stop listening
                Gdx.input.setInputProcessor(null);

                return true;
            }
        });
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

	@Override
	public float getMusicVolume() {
		return audioOutput.getMusicVolume();
	}

	@Override
	public float getSoundVolume() {
		return audioOutput.getSoundVolume();
	}

	@Override
	public void setMusicVolume(float volume) {
		audioOutput.setMusicVolume(volume);		
	}

	@Override
	public void setSoundVolume(float volume) {
		audioOutput.setSoundVolume(volume);		
	}

	@Override
	public void setMusic(String filePath) {
		audioOutput.setMusic(filePath);		
	}

	@Override
	public void stopMusic() {
		audioOutput.stopMusic();		
	}

	@Override
	public void pauseMusic() {
		audioOutput.pauseMusic();
	}

	@Override
	public void resumeMusic() {
		audioOutput.resumeMusic();
	}

	@Override
	public void playSound(String filePath) {
		audioOutput.playSound(filePath);
	}
}
