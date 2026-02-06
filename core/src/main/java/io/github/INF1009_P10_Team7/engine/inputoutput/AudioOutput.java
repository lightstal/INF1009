package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Handles all audio operations for the game engine.
 * <p>
 * This class manages:
 * <ul>
 * <li>Background Music: Streaming long audio files (only one plays at a time).</li>
 * <li>Sound Effects: Caching and playing short audio clips (multiple can play at once).</li>
 * </ul>
 * It ensures resources are disposed of correctly to prevent memory leaks.
 */
public class AudioOutput {
	private MusicState musicState;
	private Music currentMusic;
	private String currentMusicPath;
	
	/**
     * Cache for short sound effects to avoid reloading them from disk repeatedly.
     * <p>
     * Map structure:</br>
     * {@code File Path (String) -> Sound Object (Sound)}.
     */
	private Map<String, Sound> soundCache;
	
	/**
     * Initializes the audio system.
     * Sets the default music state to STOPPED and prepares the sound cache.
     */
	public AudioOutput() {
        this.currentMusic = null;
        this.musicState = MusicState.STOPPED; // Default state
        this.soundCache = new HashMap<>();
        this.currentMusicPath = "";
    }

	/**
     * Sets and plays the background music.
     * <p>
     * Logic:
     * <ol>
     * <li>If the requested song is already playing, it ensures the state is PLAYING and returns (does not restart).</li>
     * <li>If a different song is playing, it stops and disposes of the old one before loading the new one.</li>
     * </ol>
     * @param audioPath The internal file path to the music file (e.g., "music/theme.mp3").
     */
	public void setMusic(String audioPath) {
		// Check if the requested music is already the active one
		if (currentMusic != null && audioPath.equals(currentMusicPath)) {
            setMusicState(MusicState.PLAYING);
            return;
        }
		
		// Ensure all music stopped and disposed before set Next Music
		stopMusic();
		
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusicPath = audioPath;
        currentMusic.setVolume(0.4f); // Default volume of 40%
        currentMusic.setLooping(true);
        currentMusic.play();
        
        this.musicState = MusicState.PLAYING;
    }
	
	/**
     * Plays a short sound effect (SFX).
     * <p>
     * This method uses a caching mechanism. If the sound has been played before,
     * it retrieves it from memory instead of loading it from the disk again.
     * @param audioPath The internal file path to the sound file.
     */
	public void playSound(String audioPath) {
		// Check if sound is not in cache; if so, load it and add to cache
		if (!soundCache.containsKey(audioPath)) {
			soundCache.put(audioPath, Gdx.audio.newSound(Gdx.files.internal(audioPath)));
        }
	    soundCache.get(audioPath).play();
	}
	
	/**
     * Changes the state of the currently playing music.
     * @param newState The desired state (PAUSED, PLAYING, or STOPPED).
     */
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
	
	/**
     * Stops the currently playing music and releases its resources.
     * This is crucial because LibGDX Music objects stream from disk and must be disposed.
     */
	public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = "";
        }
        this.musicState = MusicState.STOPPED;
    }
	
	/**
     * Cleans up all audio resources.
     * Should be called when the game or application is closing to prevent memory leaks.
     * Disposes of the current music and all cached sound effects.
     */
	public void dispose() {
		stopMusic();
        for (Sound s : soundCache.values()) s.dispose();
	}
}