package io.github.INF1009_P10_Team7.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import io.github.INF1009_P10_Team7.engine.events.EventListener;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;

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
public class AudioOutput implements EventListener {
    private MusicState musicState;
    private Music currentMusic;

    // Volume controls (0.0 to 1.0)
    private float musicVolume = 0.4f; // Default music volume
    private float sfxVolume = 1.0f;   // Default SFX volume

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
    }

    @Override
    public void onNotify(GameEvent event) {
        Gdx.app.log("AudioOutput - EventBus", "Event received: " + event.type);
        switch (event.type) {
            case GAME_START:
            case PLAY_MUSIC:
                if (event.params.containsKey("file_path")) {
                    setMusic((String) event.params.get("file_path"));
                }
                break;

            case PLAY_SOUND:
                if (event.params.containsKey("file_path")) {
                    playSound((String) event.params.get("file_path"));
                }
                break;

            case STOP_MUSIC:
                stopMusic();
                break;

            case SET_MUSIC_VOLUME:
                if (event.params.containsKey("volume")) {
                    Object volumeObj = event.params.get("volume");
                    if (volumeObj instanceof Float) {
                        setMusicVolume((Float) volumeObj);
                    } else if (volumeObj instanceof Double) {
                        setMusicVolume(((Double) volumeObj).floatValue());
                    }
                }
                break;

            case SET_SFX_VOLUME:
                if (event.params.containsKey("volume")) {
                    Object volumeObj = event.params.get("volume");
                    if (volumeObj instanceof Float) {
                        setSfxVolume((Float) volumeObj);
                    } else if (volumeObj instanceof Double) {
                        setSfxVolume(((Double) volumeObj).floatValue());
                    }
                }
                break;

            case GAME_PAUSED:
                setMusicState(MusicState.PAUSED);
                break;

            case GAME_RESUMED:
                setMusicState(MusicState.PLAYING);
                break;

            case GAME_OVER:
            default:
                break; // Do nothing
        }
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
        // Ensure all music stopped and disposed before set Next Music
        stopMusic();

        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(audioPath));
        currentMusic.setVolume(musicVolume); // Use current music volume setting
        currentMusic.setLooping(true);
        currentMusic.play();

        this.musicState = MusicState.PLAYING;
    }

    /**
     * Sets the music volume (0.0 to 1.0)
     * @param volume Volume level from 0.0 (mute) to 1.0 (full volume)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume)); // Clamp between 0 and 1
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
        Gdx.app.log("AudioOutput", "Music volume set to: " + (int)(this.musicVolume * 100) + "%");
    }

    /**
     * Gets the current music volume
     * @return Current music volume (0.0 to 1.0)
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the sound effects volume (0.0 to 1.0)
     * @param volume Volume level from 0.0 (mute) to 1.0 (full volume)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume)); // Clamp between 0 and 1
        Gdx.app.log("AudioOutput", "SFX volume set to: " + (int)(this.sfxVolume * 100) + "%");
    }

    /**
     * Gets the current SFX volume
     * @return Current SFX volume (0.0 to 1.0)
     */
    public float getSfxVolume() {
        return sfxVolume;
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
        // Play sound with current SFX volume
        soundCache.get(audioPath).play(sfxVolume);
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
