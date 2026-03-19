package io.github.INF1009_P10_Team7.simulation.cyber.puzzle;

/**
 * Immutable data object representing a single terminal puzzle question.
 * Stores the title, flavour context, code snippet, options, and correct answer index.
 */
public final class PuzzleQuestion {

    public final String terminalTitle;
    public final String context;        // Short scenario description
    public final String codeSnippet;    // Vulnerable code shown to player
    public final String[] options;      // Four multiple-choice answers
    public final int     correctIndex;  // 0-based index of correct answer
    public final String  explanation;   // Shown after answering

    public PuzzleQuestion(String title, String context, String code,
                          String[] options, int correctIndex, String explanation) {
        this.terminalTitle  = title;
        this.context        = context;
        this.codeSnippet    = code;
        this.options        = options;
        this.correctIndex   = correctIndex;
        this.explanation    = explanation;
    }
}
