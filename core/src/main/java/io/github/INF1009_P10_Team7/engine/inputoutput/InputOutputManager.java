package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

public class InputOutputManager implements InputOutput{
	private static final int MOUSE_OFFSET = 300;
	
	private AudioOutput audioOutput;
	private KeyboardDevice keyboard;
    private MouseDevice mouse;
    
    // Map to store bindings: "JUMP" -> KeyCode (e.g., 62 for Space)
    private Map<String, Integer> keyBindings;
    
	public InputOutputManager() {
		this.audioOutput = new AudioOutput();
		this.keyboard = new KeyboardDevice();
        this.mouse = new MouseDevice();
        
        this.keyBindings = new HashMap<>();
	}
	
	// Methods for device inputs
	
	public void update() {
	    // Main loop update logic
		keyboard.pollInput();
        mouse.pollInput();
	}
	
	public void bindKey(String actionName, int keyCode) {
        keyBindings.put(actionName, keyCode);
    }

    public void bindMouseButton(String actionName, int buttonCode) {
        keyBindings.put(actionName, MOUSE_OFFSET + buttonCode);
    }
	
	public boolean isActionPressed(String actionName) {
	    // Check if the bound key for the action is currently pressed
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
	
	// Check if the bound key for action was already pressed in the previous frame
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
	
	// Methods for device outputs
	
	public void playMusic(String audioPath) {
		audioOutput.setMusic(audioPath);
    }

    public void playSound(String audioPath) {
    	audioOutput.playSound(audioPath);
    }
    
    public void setMusicState(String state) {
    	try {
            MusicState musicState = MusicState.valueOf(state.toUpperCase());
            audioOutput.setMusicState(musicState);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Music State: " + state);
        }
    }
    
    public AudioOutput getAudioOutput() {
        return audioOutput;
    }
    
    public void dispose() {
    	audioOutput.dispose();
    }
}
