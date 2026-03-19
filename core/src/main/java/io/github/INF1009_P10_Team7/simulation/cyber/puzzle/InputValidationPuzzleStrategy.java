package io.github.INF1009_P10_Team7.simulation.cyber.puzzle;

/**
 * Terminal 3  -  Java coding puzzle: fill in the blank to return the correct value.
 * Strategy Pattern: swapped in at runtime for this terminal.
 */
public class InputValidationPuzzleStrategy implements IPuzzleStrategy {

    private static final PuzzleQuestion QUESTION = new PuzzleQuestion(
        "[ TERMINAL 03 ] FIX THE METHOD",
        "This method should return the largest of two numbers.\n" +
        "Fill in the blank to make it work correctly.",
        "static int max(int a, int b) {\n    if (a > b) {\n        return ___;\n    }\n    return b;\n}",
        new String[]{
            "A)  b",
            "B)  a + b",
            "C)  a",
            "D)  0"
        },
        2,   // C is correct: when a > b, return a
        "When a > b is true, we already know a is larger.\n" +
        "So we return a. The else-case returns b."
    );

    @Override public PuzzleQuestion getQuestion()   { return QUESTION; }
    @Override public boolean checkAnswer(int idx)   { return idx == QUESTION.correctIndex; }
    @Override public String  getTerminalId()        { return "CODE-03"; }
}

