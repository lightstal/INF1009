package io.github.INF1009_P10_Team7.InputOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;

public class InputOutputManager implements iInputController, iAudioController {
    
    // --- Inner Class for Mapping ---
    private static class Binding {
        DeviceInput device;
        int code;

        Binding(DeviceInput device, int code) {
            this.device = device;
            this.code = code;
        }
    }

    // --- Input Fields ---
    private final Map<String, Binding> bindings = new HashMap<>();
    private final List<DeviceInput> devices = new ArrayList<>();

    // --- Audio Fields ---
    private final AudioOutput audioOutput = new AudioOutput();

    public void addDevice(DeviceInput device) {
        devices.add(device);
        Gdx.app.log("InputOutputManager", device.getDeviceName() + " Added.");
    }
    
    public void update() {
        for (DeviceInput device : devices) {
            device.pollInput();
        }
    }

    // --- iInputController Implementation ---
    
    @Override
    public void bindKey(String action, int code) {
        // Assuming Keyboard is the first device added
        bindings.put(action, new Binding(devices.get(0), code));
    }

    @Override
    public void bindMouse(String action, int button) {
        // Assuming Mouse is the second device added
        if (devices.size() > 1) {
            bindings.put(action, new Binding(devices.get(1), button));
        }
    }

    @Override
    public boolean isActionPressed(String action) {
        Binding b = bindings.get(action);
        return b != null && b.device.getButton(b.code);
    }

    @Override
    public boolean isActionJustPressed(String action) {
        Binding b = bindings.get(action);
        return b != null && b.device.isButtonJustPressed(b.code);
    }

    @Override
    public int getKeyCode(String actionName) {
        Binding b = bindings.get(actionName);
        return b != null ? b.code : -1;
    }

    // --- iAudioController Implementation ---

    @Override
    public void loadSound(String name, String path) { audioOutput.loadSound(name, path); }
    
    @Override
    public void playSound(String name) { audioOutput.playSound(name); }
    
    @Override
    public void playMusic(String path) { audioOutput.playMusic(path); }
    
    @Override
    public void pauseMusic() { audioOutput.pauseMusic(); }
    
    @Override
    public void resumeMusic() { audioOutput.resumeMusic(); }
    
    @Override
    public void stopMusic() { audioOutput.stopMusic(); }
    
    @Override
    public void setMusicVolume(float volume) { audioOutput.setMusicVolume(volume); }
    
    @Override
    public float getMusicVolume() { return audioOutput.getMusicVolume(); }

    public void dispose() {
        audioOutput.dispose();
    }
}