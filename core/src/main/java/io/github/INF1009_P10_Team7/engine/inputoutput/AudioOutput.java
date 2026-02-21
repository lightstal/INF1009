package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * <p>Handles all audio operations for the engine.</p>
 *
 * <p>This class encapsulates LibGDX's audio system, managing both streamed 
 * background music and cached sound effects. By handling the loading, playback, 
 * and disposal of audio resources internally, it hides the complexity of 
 * hardware audio management from the rest of the game engine.</p>
 * * <ul>
 * <li><b>Background music:</b> Streamed directly from the file (one track at a time).</li>
 * <li><b>Sound effects (SFX):</b> Short clips cached in memory for immediate, repeated playback.</li>
 * </ul>
 */
public class AudioOutput {
	
	/** The currently playing background music track. */
    private Music currentMusic;

    // Volume controls (0.0 to 1.0)
    private float musicVolume = 0.4f;
    private float sfxVolume = 1.0f;

    // Sound effect cache
    private final Map<String, Sound> soundCache = new HashMap<>();

    /**
     * <p>Constructs a new AudioOutput manager with no music loaded 
     * and default volume settings.</p>
     */
    public AudioOutput() {
        this.currentMusic = null;
    }

    /**
     * <p>Loads and starts playing a new background music track.</p>
     * <p>If a track is already playing, it will be stopped and disposed of 
     * before the new track begins. The music will automatically loop.</p>
     *
     * @param audioPath The internal file path to the audio file (e.g., "audio/bgm.mp3").
     */
    public void setMusic(String audioPath) {
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusic.setVolume(musicVolume);
        currentMusic.setLooping(true);
        currentMusic.play();
    }
    
    /**
     * <p>Stops the currently playing background music and releases its resources.</p>
     * <p>Call this when music is no longer needed to free up memory.</p>
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    /**
     * <p>Pauses the currently playing background music.</p>
     * <p>Does nothing if no music is loaded or if it is already paused.</p>
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    /**
     * <p>Resumes the background music from where it was paused.</p>
     * <p>Does nothing if no music is loaded or if it is already playing.</p>
     */
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    /**
     * <p>Plays a short sound effect (SFX).</p>
     * <p>If the sound has not been played before, it will be loaded from the disk 
     * and stored in the {@code soundCache}. Subsequent calls will play the cached 
     * sound instantly. If the SFX volume is set to 0, playback is skipped.</p>
     *
     * @param audioPath The internal file path to the sound file (e.g., "audio/jump.wav").
     */
    public void playSound(String audioPath) {
        if (sfxVolume <= 0f) {
            Gdx.app.log("AudioOutput", audioPath + " skipped (volume is 0).");
            return;
        }
        Sound sound = soundCache.get(audioPath);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(audioPath));
            soundCache.put(audioPath, sound);
        }
        sound.play(sfxVolume);
        Gdx.app.log("AudioOutput", audioPath + " Sound played.");
    }

    /**
     * <p>Retrieves the current volume level of the background music.</p>
     *
     * @return The music volume, ranging from 0.0f (muted) to 1.0f (maximum).
     */
    public float getMusicVolume() { 
        return musicVolume; 
    }

    /**
     * <p>Sets the global volume level for background music.</p>
     * <p>If music is currently playing, its volume will be adjusted immediately.</p>
     *
     * @param volume The desired volume level. Automatically clamped between 0.0f and 1.0f.
     */
    public void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        Gdx.app.log("AudioOutput", "Music volume set to: " + (int)(musicVolume * 100) + "%");
    }

    /**
     * <p>Retrieves the current volume level for sound effects.</p>
     *
     * @return The SFX volume, ranging from 0.0f (muted) to 1.0f (maximum).
     */
    public float getSoundVolume() { 
        return sfxVolume; 
    }

    /**
     * <p>Sets the global volume level for all future sound effects.</p>
     *
     * @param volume The desired volume level. Automatically clamped between 0.0f and 1.0f.
     */
    public void setSoundVolume(float volume) {
        sfxVolume = Math.max(0f, Math.min(1f, volume));
        Gdx.app.log("AudioOutput", "SFX volume set to: " + (int)(sfxVolume * 100) + "%");
    }

    /**
     * <p>Safely disposes of all audio resources currently managed by this class.</p>
     * <p>Stops and unloads the current music track, and flushes all cached sound 
     * effects from memory. This must be called when the application is shutting down 
     * to prevent memory leaks.</p>
     */
    public void dispose() {
        stopMusic();
        for (Sound s : soundCache.values()) {
            s.dispose();
        }
        soundCache.clear();
        Gdx.app.log("AudioOutput", "AudioOutput disposed");
    }
}
