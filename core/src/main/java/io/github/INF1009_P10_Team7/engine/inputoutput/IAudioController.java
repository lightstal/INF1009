package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * <p>Interface that defines the contract for all audio operations within the engine.</p>
 *
 * <p>By providing this interface, the engine applies the Dependency Inversion Principle (DIP),
 * ensuring that the simulation layer (Scenes) can control audio playback and volume 
 * without relying on the concrete implementation details of the audio system.</p>
 */
public interface IAudioController {
    
    // --- Getters (Retrieve Values) ---

    /**
     * Retrieves the current volume level of the background music.
     * * @return The music volume, typically ranging from 0.0 (muted) to 1.0 (maximum).
     */
    float getMusicVolume();

    /**
     * Retrieves the current volume level of sound effects (SFX).
     * * @return The sound effects volume, typically ranging from 0.0 (muted) to 1.0 (maximum).
     */
    float getSoundVolume();


    // --- Setters (Direct Control) ---

    /**
     * Sets the volume level for the background music.
     * * @param volume The desired volume level, clamped between 0.0f and 1.0f.
     */
    void setMusicVolume(float volume);

    /**
     * Sets the volume level for sound effects (SFX).
     * * @param volume The desired volume level, clamped between 0.0f and 1.0f.
     */
    void setSoundVolume(float volume);
    

    // --- Actions ---

    /**
     * Loads and starts playing a new background music track.
     * If a track is already playing, it will be stopped and replaced by the new track.
     * The music will automatically loop.
     * * @param filePath The internal file path to the audio file (e.g., "audio/bgm.mp3").
     */
    void setMusic(String filePath);

    /**
     * Pauses the currently playing background music. 
     * Does nothing if no music is playing or if it is already paused.
     */
    void pauseMusic();

    /**
     * Resumes the background music from where it was paused.
     * Does nothing if no music is loaded or if it is already playing.
     */
    void resumeMusic();

    /**
     * Completely stops the currently playing background music and releases its resources.
     */
    void stopMusic();

    /**
     * Plays a short sound effect (SFX) once. 
     * Suitable for transient audio like jumps, UI clicks, or explosions.
     * Multiple sound effects can play simultaneously.
     * * @param filePath The internal file path to the sound file (e.g., "audio/jump.wav").
     */
    void playSound(String filePath);
}