package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioOutput {
	private MusicState musicState;
	private Music currentMusic;
	private String currentMusicPath;
	private Map<String, Sound> soundCache;
	
	public AudioOutput() {
	    // Constructor for initializing the components
        this.currentMusic = null;
        this.musicState = MusicState.STOPPED; // Default state
        this.soundCache = new HashMap<>();
        this.currentMusicPath = "";
    }
	
	public void setMusic(String audioPath) {
		// Check 
		if (currentMusic != null && audioPath.equals(currentMusicPath)) {
            setMusicState(MusicState.PLAYING);
            return;
        }
		
		// Ensure all music stop before set Next Music
		stopMusic();
		
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusicPath = audioPath;
        currentMusic.setVolume(0.4f);
        currentMusic.setLooping(true);
        currentMusic.play();
        
        this.musicState = MusicState.PLAYING;
    }
	
	
	public void playSound(String audioPath) {
		// Check if sound has not been played before, add to Sound Cache
		if (!soundCache.containsKey(audioPath)) {
			soundCache.put(audioPath, Gdx.audio.newSound(Gdx.files.internal(audioPath)));
        }
	    soundCache.get(audioPath).play();
	}
	
	public void setMusicState(MusicState newState) {
        if (this.musicState == newState) return;

        switch (newState) {
            case PAUSED:
                if (currentMusic != null) currentMusic.pause();
                break;
            case PLAYING:
                if (currentMusic != null) currentMusic.play();
                break;
            case STOPPED:
                stopMusic();
                break;
        }
        this.musicState = newState;
    }
	
	public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = "";
        }
        this.musicState = MusicState.STOPPED;
    }
	
	public void dispose() {
		stopMusic();
        for (Sound s : soundCache.values()) s.dispose();
	}
}