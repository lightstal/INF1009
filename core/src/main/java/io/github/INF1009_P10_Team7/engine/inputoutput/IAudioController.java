package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * IAudioController, narrow interface for audio playback operations.
 *
 * <p>Scenes depend on this interface (via the {@link Scene} base class)
 * rather than the concrete {@link AudioOutput} or {@link InputOutputManager}
 * (DIP). This keeps scenes independent of the audio backend and makes
 * testing easier.</p>
 *
 * <p>Two categories of audio are managed:</p>
 * <ul>
 * <li><b>Music</b>, one streamed background track at a time;
 * supports play, stop, pause, resume, and volume control.</li>
 * <li><b>Sound effects (SFX)</b>, short cached clips played on demand;
 * volume controlled independently from music.</li>
 * </ul>
 */
public interface IAudioController {

    /**
     * Loads and immediately starts playing a background music track.
     * If a track is already playing it is stopped and disposed first.
     * The track loops automatically.
     *
     * @param filePath internal asset path (e.g. {@code "audio/Music_Game.mp3"})
     */
    void setMusic(String filePath);

    /** Stops the current background music track and releases its resources. */
    void stopMusic();

    /**
     * Pauses the current background music track.
     * Does nothing if no music is playing.
     */
    void pauseMusic();

    /**
     * Resumes the background music track from where it was paused.
     * Does nothing if the track is already playing.
     */
    void resumeMusic();

    /**
     * Plays a short sound effect. The clip is loaded on first use and
     * cached for subsequent calls. Skipped silently if SFX volume is 0.
     *
     * @param filePath internal asset path (e.g. {@code "audio/bell.mp3"})
     */
    void playSound(String filePath);

    /**
     * @return the current background music volume in the range [0.0, 1.0]
     */
    float getMusicVolume();

    /**
     * Sets the global background music volume. Takes effect immediately
     * if music is currently playing.
     *
     * @param volume desired volume; automatically clamped to [0.0, 1.0]
     */
    void setMusicVolume(float volume);

    /** @return the current sound-effect volume in the range [0.0, 1.0] */
    float getSoundVolume();

    /**
     * Sets the global sound-effect volume. Applies to all future
     * {@link #playSound} calls.
     *
     * @param volume desired volume; automatically clamped to [0.0, 1.0]
     */
    void setSoundVolume(float volume);
}
