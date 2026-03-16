package io.github.INF1009_P10_Team7.simulation.cyber.puzzle;

/**
 * Terminal 1 — Java coding puzzle: fill in the blank to complete a for loop.
 * Strategy Pattern: swapped in at runtime for this terminal.
 */
public class XssPuzzleStrategy implements IPuzzleStrategy {

    private static final PuzzleQuestion QUESTION = new PuzzleQuestion(
        "[ TERMINAL 01 ] COMPLETE THE LOOP",
        "Fill in the blank so the loop prints 1 through 5.",
        "for (int i = 1; ___; i++) {\n    System.out.println(i);\n}",
        new String[]{
            "A)  i < 5",
            "B)  i <= 5",
            "C)  i != 6",
            "D)  i < 6 && i > 0"
        },
        1,   // B is correct: i <= 5 prints exactly 1,2,3,4,5
        "i <= 5 stops after printing 5.\n" +
        "i < 5 would only print 1 through 4 (misses 5)."
    );

    @Override public PuzzleQuestion getQuestion()   { return QUESTION; }
    @Override public boolean checkAnswer(int idx)   { return idx == QUESTION.correctIndex; }
    @Override public String  getTerminalId()        { return "CODE-01"; }
}

