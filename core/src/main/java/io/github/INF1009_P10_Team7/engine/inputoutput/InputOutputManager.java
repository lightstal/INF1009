package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
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
	
	private AudioOutput audioOutput;
	
	/**
     * The registry containing all active input devices.
     * Devices are sorted descending by their base offset.
     */
    private List<DeviceInput> registeredDevices;

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
		
        this.keyBindings = new HashMap<>();
        this.registeredDevices = new ArrayList<>();
        
        // Register default devices
        registerDevice(new KeyboardDevice());
        registerDevice(new MouseDevice());
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public void registerDevice(DeviceInput device) {
        registeredDevices.add(device);
        registeredDevices.sort((d1, d2) -> Integer.compare(d2.getBaseOffset(), d1.getBaseOffset()));
    }

    /**
     * Routes a global key code to the correct registered hardware device.
     * * @param globalCode The integer code retrieved from the key bindings map.
     * @return The {@link DeviceInput} responsible for handling this code, or {@code null} if none match.
     */
    private DeviceInput getTargetDevice(int globalCode) {
        for (DeviceInput device : registeredDevices) {
            if (globalCode >= device.getBaseOffset()) {
                return device;
            }
        }
        return null;
    }
    
    /**
     * Retrieves a registered device by its unique ID.
     * <p>
     * Used internally to route dynamic binding requests to the correct hardware.
     * * @param id The integer ID of the device to find.
     * @return The {@link DeviceInput} matching the ID, or {@code null} if no such device is registered.
     */
    private DeviceInput getDeviceByID(int id) {
        for (DeviceInput device : registeredDevices) {
            if (device.getDeviceID() == id) {
                return device;
            }
        }
        return null; 
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
		for (DeviceInput device : registeredDevices) {
            device.pollInput();
        }
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
     * <p>
     * <b>Dynamic Routing Logic:</b> 
     * <ol>
     * <li>Finds the target device in the registry using the {@code deviceID}.</li>
     * <li>Retrieves the device's specific {@code baseOffset}.</li>
     * <li>Mathematically combines the offset with the {@code localCode} to create a 
     * unique global code (e.g., Mouse Offset 300 + Left Click 0 = Global Code 300).</li>
     * <li>Stores the global code in the {@code keyBindings} map.</li>
     * </ol>
     */
    @Override
    public void bindInput(String actionName, int deviceID, int localCode) {
        DeviceInput targetDevice = getDeviceByID(deviceID);
        
        if (targetDevice != null) {
            int globalCode = targetDevice.getBaseOffset() + localCode;
            keyBindings.put(actionName, globalCode);
            Gdx.app.log("InputManager", "Bound " + actionName + " to global code: " + globalCode);
        } else {
            Gdx.app.error("InputManager", "Failed to bind " + actionName + ". Device ID " + deviceID + " not found.");
        }
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
    	Integer code = keyBindings.get(actionName);
        if (code == null) return false;

        DeviceInput device = getTargetDevice(code);
        if (device != null) {
            return device.getButton(code - device.getBaseOffset());
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActionJustPressed(String actionName) {
    	Integer code = keyBindings.get(actionName);
        if (code == null) return false;

        DeviceInput device = getTargetDevice(code);
        if (device != null) {
            return device.isButtonJustPressed(code - device.getBaseOffset());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>Get key code when pressed</p>
     * 
     * @param action Get action name like ("UP", "DOWN")
     * @return Mouse click or Keyboard keycode or NONE or UNKNOWN(Error)
     */
    @Override
    public String getKeyName(String action) {
    	Integer code = keyBindings.get(action);
        if (code == null) return "NONE";

        DeviceInput device = getTargetDevice(code);
        if (device != null) {
            return device.getKeyName(code - device.getBaseOffset());
        }
        return "UNKNOWN";
    }

    /**
     * {@inheritDoc}
     * <p>add listener for next key press</p>
     * 
     * @param callback when key is pressed
     */
    @Override
    public void listenForNextKey(final IInputController.InputCallback callback) {

        // set a temporary processor to catch exactly ONE key or mouse click
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
            	// Device ID 0 = Keyboard
                callback.onInputReceived(0, keycode);
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            	// Device ID 1 = Mouse
                callback.onInputReceived(1, button);
                return true;
            }
        });
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public float getMouseX() {
    	for (DeviceInput device : registeredDevices) {
            if (device.deviceID == 1) return device.getAxis(0); 
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMouseY() {
    	for (DeviceInput device : registeredDevices) {
            if (device.deviceID == 1) return device.getAxis(1); 
        }
        return 0;
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
