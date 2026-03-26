package io.github.INF1009_P10_Team7.cyber.ctf;

/**
 * ICTFChallenge, interface for terminal-style CTF (Capture-The-Flag) challenges.
 *
 * <p>Each challenge simulates a real-world hacking scenario inside the
 * {@link TerminalEmulator}. Concrete implementations define the command
 * vocabulary, the correct solution path, and the flag text to reveal on success.</p>
 *
 * <p>Used by {@link io.github.INF1009_P10_Team7.cyber.minigame.TerminalMiniGame}
 * to wrap a CTF challenge as an {@link io.github.INF1009_P10_Team7.cyber.minigame.IMiniGame}
 * so {@code CyberGameScene} can treat all challenges uniformly (LSP, OCP).</p>
 */
public interface ICTFChallenge {

    /**
     * @return the display title shown at the top of the terminal window
     * (e.g. {@code "TERMINAL-01 // NETWORK RECON"})
     */
    String getTitle();

    /**
     * @return a one-line target description shown below the title
     * (e.g. {@code "target: 192.168.10.0/24"})
     */
    String getTargetInfo();

    /**
     * @return the shell prompt string rendered before each input line
     * (e.g. {@code "attacker@kali:~$ "})
     */
    String getPrompt();

    /**
     * Returns the lines to display when the terminal is first opened.
     * Typically includes the objective, context, and an initial hint.
     *
     * @return array of {@link TerminalLine} objects for the welcome screen
     */
    TerminalLine[] getWelcomeLines();

    /**
     * Processes a raw command string typed by the player and returns
     * the terminal output to display.
     *
     * @param raw the raw input line including any arguments
     * @return array of {@link TerminalLine} objects representing the response
     */
    TerminalLine[] processInput(String raw);

    /**
     * @return {@code true} once the player has completed the challenge
     * (found the flag / extracted the key)
     */
    boolean isSolved();

    /**
     * Resets all internal state so the challenge can be attempted again
     * (e.g. after a respawn or level restart).
     */
    void reset();
}
