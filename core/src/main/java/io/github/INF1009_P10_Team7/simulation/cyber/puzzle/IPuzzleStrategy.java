package io.github.INF1009_P10_Team7.simulation.cyber.puzzle;

/**
 * Strategy Pattern interface for terminal puzzles.
 * Each terminal swaps in a different IPuzzleStrategy at runtime.
 * Adding a new puzzle type never requires touching existing code (OCP).
 */
public interface IPuzzleStrategy {
    /** Returns the question data for this puzzle. */
    PuzzleQuestion getQuestion();

    /** Returns true if the player's chosen answer index is correct. */
    boolean checkAnswer(int chosenIndex);

    /** Terminal identifier shown in the HUD. */
    String getTerminalId();
}
