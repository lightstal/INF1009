package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

import com.badlogic.gdx.graphics.Color;

/** Immutable coloured line of text for the terminal display. */
public final class TerminalLine {
    public final String text;
    public final Color  color;

    // Palette constants so challenges read cleanly
    public static final Color C_GREEN   = new Color(0.00f, 1.00f, 0.25f, 1f);
    public static final Color C_DIMGREEN= new Color(0.00f, 0.55f, 0.15f, 1f);
    public static final Color C_CYAN    = new Color(0.00f, 1.00f, 1.00f, 1f);
    public static final Color C_YELLOW  = new Color(1.00f, 0.95f, 0.00f, 1f);
    public static final Color C_RED     = new Color(1.00f, 0.20f, 0.10f, 1f);
    public static final Color C_WHITE   = new Color(0.90f, 0.90f, 0.90f, 1f);
    public static final Color C_GRAY    = new Color(0.45f, 0.45f, 0.50f, 1f);
    public static final Color C_ORANGE  = new Color(1.00f, 0.60f, 0.00f, 1f);

    public TerminalLine(String text, Color color) {
        this.text  = text;
        this.color = color;
    }

    // Convenience constructors
    public static TerminalLine ok  (String t){ return new TerminalLine(t, C_GREEN);  }
    public static TerminalLine info(String t){ return new TerminalLine(t, C_CYAN);   }
    public static TerminalLine warn(String t){ return new TerminalLine(t, C_YELLOW); }
    public static TerminalLine err (String t){ return new TerminalLine(t, C_RED);    }
    public static TerminalLine dim (String t){ return new TerminalLine(t, C_GRAY);   }
    public static TerminalLine out (String t){ return new TerminalLine(t, C_WHITE);  }
    public static TerminalLine blank()       { return new TerminalLine("", C_GRAY);  }
}
