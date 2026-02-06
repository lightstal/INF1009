package io.github.INF1009_P10_Team7.engine.inputoutput;

/**
 * Represents the possible playback states of background music in the engine.
 * <p>
 * Used by {@link AudioOutput} to track and control the current music track.
 */
public enum MusicState {
    
    /**
     * Music is completely stopped.
     * <p>
     * In this state, the underlying Music object is usually disposed or null,
     * consuming minimal resources.
     */
    STOPPED,

    /**
     * Music is currently playing (active).
     * <p>
     * The audio stream is reading from the file and outputting sound.
     */
    PLAYING,

    /**
     * Music is paused at a specific position.
     * <p>
     * The file is still loaded in memory, and playback can resume from
     * exactly where it left off.
     */
    PAUSED
}