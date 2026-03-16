package io.github.INF1009_P10_Team7.simulation.cyber.puzzle;

/**
 * Terminal 2 — Java coding puzzle: predict what this code prints.
 * Strategy Pattern: swapped in at runtime for this terminal.
 */
public class CsrfPuzzleStrategy implements IPuzzleStrategy {

    private static final PuzzleQuestion QUESTION = new PuzzleQuestion(
        "[ TERMINAL 02 ] WHAT IS THE OUTPUT?",
        "What does this Java code print to the console?",
        "int x = 10;\nif (x > 5) {\n    x = x * 2;\n} else {\n    x = x + 1;\n}\nSystem.out.println(x);",
        new String[]{
            "A)  10",
            "B)  11",
            "C)  20",
            "D)  5"
        },
        2,   // C is correct: 10 > 5 is true, so x = 10 * 2 = 20
        "x = 10, and 10 > 5 is true, so the if-branch runs.\n" +
        "x = 10 * 2 = 20 is printed."
    );

    @Override public PuzzleQuestion getQuestion()   { return QUESTION; }
    @Override public boolean checkAnswer(int idx)   { return idx == QUESTION.correctIndex; }
    @Override public String  getTerminalId()        { return "CODE-02"; }
}

