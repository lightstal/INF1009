package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

/**
 * Strategy Pattern interface for interactive CTF terminal challenges.
 * Each terminal swaps in its own concrete implementation.
 * Adding a new challenge never requires modifying the TerminalEmulator (OCP).
 */
public interface ICTFChallenge {
    /** Short name shown in the terminal title bar. */
    String getTitle();
    /** Target IP / service string shown in the header. */
    String getTargetInfo();
    /** Shell prompt string, e.g. "root@kali:~$ " */
    String getPrompt();
    /** Lines shown once when the terminal first opens. */
    TerminalLine[] getWelcomeLines();
    /**
     * Process a command the player typed and return output lines.
     * Return an empty array for "no output".
     */
    TerminalLine[] processInput(String rawInput);
    /** Returns true once the player has captured the flag. */
    boolean isSolved();
    /** Resets the challenge so the terminal can be retried. */
    void reset();
}
