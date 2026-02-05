package io.github.INF1009_P10_Team7.engine.inputoutput;

//import java.util.List;

import com.badlogic.gdx.audio.Sound;

public class DeviceOutput {
	private AudioOutput audioOutput;
	private VisualOutput visualOutput;
	
	public DeviceOutput() {
	    // Constructor for initializing the components
	    this.audioOutput = new AudioOutput();
	    this.visualOutput = new VisualOutput();
	}
	
//	public void renderEntities(List<Entity> entities) {
//	    visualOutput.render(entities);
//	}
	
	public void playMusic(String audioPath) {
	    audioOutput.setMusic(audioPath);
	}
	
	public void setMusicState(String stateStr) {
        try {
            MusicState state = MusicState.valueOf(stateStr.toUpperCase());
            audioOutput.setMusicState(state);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Music State: " + stateStr);
        }
    }
	
	public void playSound(String audioPath) {
	    Sound sound = audioOutput.getSound(audioPath);
	    if (sound != null) {
	         sound.play();
	    }
	}
	
	public void dispose() {
	    visualOutput.dispose();
	    audioOutput.dispose();
	}
}