package io.github.INF1009_P10_Team7.cyber.ctf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.KeyCode;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;
import io.github.INF1009_P10_Team7.engine.render.ITextDraw;
import io.github.INF1009_P10_Team7.engine.render.MiniGameRenderContext;

/**
 * A full-featured terminal emulator widget for LibGDX.
 *
 * <p>Refactored to rely purely on the abstract GameEngine:
 * <ul>
 * <li>Implements ITextInputListener to receive OS-level typing and control keys</li>
 * <li>No longer fights for control of the Gdx InputProcessor</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 * <li>Real keyboard input via inherited listener methods</li>
 * <li>Typewriter reveal effect</li>
 * <li>Command history via UP/DOWN arrows</li>
 * <li>Mouse-wheel / PAGE UP/DOWN scrolling</li>
 * <li>Blinking cursor</li>
 * <li>Panic key (TAB) - instantly close terminal</li>
 * </ul>
 *
 * <p>render() uses its own pre-scaled fonts created in open(),
 * never mutating the caller's shared hudFont reference.
 */
public class TerminalEmulator implements IInputController.ITextInputListener {

    // Layout (HUD coords at 1280x704)
    private static final float WIN_X  = 32f;
    private static final float WIN_Y  = 24f;
    private static final float WIN_W  = 1216f;
    private static final float WIN_H  = 656f;
    private static final float TITLE_H= 40f;
    private static final float INPUT_H= 34f;
    private static final float PAD    = 16f;
    private static final float LINE_H = 18f;
    private static final int   MAX_VISIBLE = 32;

    // State
    private boolean open = false;
    private boolean waitingForInput = true;
    private boolean panicked = false;

    private ICTFChallenge challenge;
    private final List<TerminalLine>  displayLines  = new ArrayList<>();
    private final Deque<TerminalLine> pendingLines  = new ArrayDeque<>();
    private float revealTimer  = 0f;
    private static final float REVEAL_DELAY = 0.035f;

    private final StringBuilder    inputBuffer   = new StringBuilder();
    private final List<String>     cmdHistory    = new ArrayList<>();
    private int                    historyIdx    = -1;

    private float   cursorTimer = 0f;
    private boolean cursorVisible = true;
    private int     scrollOffset  = 0;

    // Glitch effect
    private float glitchTimer = 0f;
    private float stateTime   = 0f;
    private float solveTimer  = 0f;
    private long  lastDeleteNanos = 0L;

    // Keyboard modifiers (engine-only)
    private float ctrlLatchTimer = 0f;
    private static final float CTRL_LATCH_SECONDS = 0.25f;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Opens the terminal emulator and loads the provided CTF challenge.
     * @param c The challenge configuration to load.
     */
    public void open(ICTFChallenge c) {
        challenge      = c;
        open           = true;
        panicked       = false;
        waitingForInput= false;
        displayLines.clear();
        pendingLines.clear();
        inputBuffer.setLength(0);
        cmdHistory.clear();
        historyIdx = -1;
        scrollOffset   = 0;
        stateTime      = 0f;
        cursorTimer    = 0f;
        solveTimer     = 0f;
        lastDeleteNanos = 0L;
        ctrlLatchTimer = 0f;

        // Queue welcome lines
        addLine(TerminalLine.dim("============================================================"));
        addLine(TerminalLine.info("  " + c.getTitle() + "  // " + c.getTargetInfo()));
        addLine(TerminalLine.dim("============================================================"));
        addLine(TerminalLine.blank());
        for (TerminalLine l : c.getWelcomeLines()) addLine(l);
        addLine(TerminalLine.blank());
        
        // Note: The parent TerminalMiniGame routes the engine input to us automatically!
    }

    /** Closes the terminal and cleans up resources. */
    public void close() {
        open = false;
    }

    public boolean isOpen()     { return open; }
    public boolean wasPanicked(){ return panicked; }
    public boolean isSolved()   { return challenge != null && challenge.isSolved(); }

    // =========================================================================
    // UPDATE
    // =========================================================================

    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        cursorTimer += dt;
        if (ctrlLatchTimer > 0f) ctrlLatchTimer = Math.max(0f, ctrlLatchTimer - dt);
        if (cursorTimer > 0.5f) { cursorTimer = 0f; cursorVisible = !cursorVisible; }

        if (!pendingLines.isEmpty()) {
            revealTimer += dt;
            while (revealTimer >= REVEAL_DELAY && !pendingLines.isEmpty()) {
                revealTimer -= REVEAL_DELAY;
                displayLines.add(pendingLines.poll());
            }
            if (pendingLines.isEmpty()) {
                waitingForInput = true;
            }
        }

        if (scrollOffset == 0) clampScroll();
        if (stateTime < 0.3f) glitchTimer = stateTime;

        if (challenge != null && challenge.isSolved()) {
            solveTimer += dt;
            if (solveTimer > 2f) close();
        }
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    public void render(MiniGameRenderContext context) {
        if (!open) return;
        IShapeDraw sr = context.shape();
        ITextDraw mono = context.monoText();
        ITextDraw small = context.smallText();
        ITextDraw solvedText = context.titleText();

        float glitch = (stateTime < 0.3f) ? (float)Math.sin(stateTime * 80f) * 3f : 0f;

        // Background overlay
        sr.beginFilled();
        sr.setColor(0f, 0f, 0f, 0.94f);
        sr.rect(0, 0, 1280, 704);

        sr.setColor(0f, 0.95f, 0.45f, 0.10f);
        sr.rect(WIN_X - 8 + glitch, WIN_Y - 8, WIN_W + 16, WIN_H + 16);

        sr.setColor(0f, 0.82f, 0.38f, 0.28f);
        sr.rect(WIN_X - 3 + glitch, WIN_Y - 3, WIN_W + 6, WIN_H + 6);

        sr.setColor(0.03f, 0.05f, 0.04f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, WIN_H);

        sr.setColor(0f, 0.25f, 0.12f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_W, TITLE_H);

        sr.setColor(0.02f, 0.08f, 0.04f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, INPUT_H);

        sr.setColor(0f, 0.82f, 0.35f, 0.95f);
        sr.rect(WIN_X - 1 + glitch, WIN_Y - 1, WIN_W + 2, 2f);
        sr.rect(WIN_X - 1 + glitch, WIN_Y + WIN_H - 1, WIN_W + 2, 2f);
        sr.rect(WIN_X - 1 + glitch, WIN_Y - 1, 2f, WIN_H + 2);
        sr.rect(WIN_X + WIN_W - 1 + glitch, WIN_Y - 1, 2f, WIN_H + 2);

        sr.setColor(0f, 0.6f, 0.25f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y + INPUT_H - 1, WIN_W, 1f);
        sr.rect(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_W, 1f);

        sr.setColor(0f, 0f, 0f, 0.08f);
        for (float y = WIN_Y; y < WIN_Y + WIN_H; y += 3) {
            sr.rect(WIN_X + glitch, y, WIN_W, 1f);
        }
        sr.end();

        sr.beginLine();
        sr.setColor(0.15f, 1f, 0.55f, 0.9f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, WIN_H);
        sr.rect(WIN_X + 6 + glitch, WIN_Y + 6, WIN_W - 12, WIN_H - 12);
        sr.line(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_X + WIN_W + glitch, WIN_Y + WIN_H - TITLE_H);
        sr.end();

        // Text rendering (own fonts)
        mono.begin();

        // Title bar text
        mono.setColor(0f, 1f, 0.45f, 1f);
        float headerY = WIN_Y + WIN_H - 12f;
        float headerLeftW = WIN_W - PAD * 2f - 330f;
        String shellId = "analyst@kali:~/cases$";
        mono.draw(fitToWidth(mono, shellId + "  " + challenge.getTitle(), headerLeftW),
            WIN_X + PAD + glitch, headerY);
        mono.setColor(0.52f, 0.58f, 0.56f, 1f);
        mono.draw(fitToWidth(mono, "ESC close | TAB panic | ENTER run | PgUp/PgDn scroll", 310f),
            WIN_X + WIN_W - 322f + glitch, headerY);

        // Output lines
        float textAreaH = WIN_H - TITLE_H - INPUT_H - PAD * 2;
        int maxLines = (int)(textAreaH / LINE_H);
        int total = displayLines.size();
        int start = Math.max(0, total - maxLines - scrollOffset);
        int end   = Math.min(total, start + maxLines);

        float lineY = WIN_Y + INPUT_H + PAD + (maxLines * LINE_H);
        for (int i = start; i < end; i++) {
            TerminalLine tl = displayLines.get(i);
            mono.setColor(tl.color.r, tl.color.g, tl.color.b, tl.color.a);
            mono.draw(fitToWidth(mono, tl.text, WIN_W - PAD * 2f - 10f), WIN_X + PAD + glitch, lineY);
            lineY -= LINE_H;
        }

        // Scroll indicator
        if (scrollOffset > 0) {
            small.setColor(
                TerminalLine.C_YELLOW.r,
                TerminalLine.C_YELLOW.g,
                TerminalLine.C_YELLOW.b,
                TerminalLine.C_YELLOW.a
            );
            small.draw("^ scrolled " + scrollOffset + " lines (PGDN to go back)",
                WIN_X + WIN_W - 240f + glitch, WIN_Y + INPUT_H + 4f);
        }

        // Input line
        String prompt = challenge.getPrompt();
        String inputStr = inputBuffer.toString();
        boolean showCursor = waitingForInput && cursorVisible;
        String cursor = showCursor ? "|" : " ";

        float inputY = WIN_Y + INPUT_H - 7f;
        mono.setColor(
            TerminalLine.C_GREEN.r,
            TerminalLine.C_GREEN.g,
            TerminalLine.C_GREEN.b,
            TerminalLine.C_GREEN.a
        );
        String clippedPrompt = fitToWidth(mono, prompt, 220f);
        mono.draw(clippedPrompt, WIN_X + PAD + glitch, inputY);

        float promptW = mono.measureWidth(clippedPrompt);
        float inputW = WIN_W - PAD * 2f - promptW - 18f;
        mono.setColor(
            TerminalLine.C_WHITE.r,
            TerminalLine.C_WHITE.g,
            TerminalLine.C_WHITE.b,
            TerminalLine.C_WHITE.a
        );
        mono.draw(fitTailToWidth(mono, inputStr + cursor, inputW), WIN_X + PAD + promptW + glitch, inputY);

        // Solved flash
        if (challenge.isSolved()) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            solvedText.setColor(0f, fl, fl * 0.5f, 1f);
            solvedText.draw("  [OK] TERMINAL SECURED - KEY ACQUIRED", WIN_X + PAD, WIN_Y + 6f);
        }

        mono.end();
    }

    // =========================================================================
    // ITextInputListener IMPLEMENTATION
    // =========================================================================

    @Override
    public void onCharTyped(char c) {
        if (!open || !waitingForInput) return;

        if (c == '\b' || c == 127) {
            if (!recentDeleteEvent()) deleteLastChar();
            return;
        }
        
        // Enter is handled in onControlKeyPressed
        if (c == '\r' || c == '\n') return;

        if (c >= 32 && c < 127) {
            inputBuffer.append(c);
        }
    }

    @Override
    public void onControlKeyPressed(int keycode) {
        if (!open) return;

        if (keycode == KeyCode.TAB || keycode == KeyCode.ESCAPE) {
            panicked = true;
            close();
            return;
        }

        if (keycode == KeyCode.CONTROL_LEFT || keycode == KeyCode.CONTROL_RIGHT) {
            ctrlLatchTimer = CTRL_LATCH_SECONDS;
            return;
        }

        if (keycode == KeyCode.BACKSPACE || keycode == KeyCode.DEL || keycode == KeyCode.FORWARD_DEL) {
            boolean ctrlDown = ctrlLatchTimer > 0f;
            if (ctrlDown) {
                while (inputBuffer.length() > 0 && inputBuffer.charAt(inputBuffer.length() - 1) == ' ') {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                }
                while (inputBuffer.length() > 0 && inputBuffer.charAt(inputBuffer.length() - 1) != ' ') {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                }
                lastDeleteNanos = System.nanoTime();
            } else {
                deleteLastChar();
            }
            return;
        }

        if (keycode == KeyCode.ENTER || keycode == KeyCode.NUMPAD_ENTER) {
            if (waitingForInput) submitCommand();
            return;
        }

        if (keycode == KeyCode.UP) {
            if (!cmdHistory.isEmpty()) {
                historyIdx = Math.min(historyIdx + 1, cmdHistory.size() - 1);
                inputBuffer.setLength(0);
                inputBuffer.append(cmdHistory.get(cmdHistory.size() - 1 - historyIdx));
            }
            return;
        }
        if (keycode == KeyCode.DOWN) {
            if (historyIdx > 0) {
                historyIdx--;
                inputBuffer.setLength(0);
                inputBuffer.append(cmdHistory.get(cmdHistory.size() - 1 - historyIdx));
            } else {
                historyIdx = -1;
                inputBuffer.setLength(0);
            }
            return;
        }

        if (keycode == KeyCode.PAGE_UP)   { scrollOffset += 5; clampScroll(); return; }
        if (keycode == KeyCode.PAGE_DOWN) { scrollOffset = Math.max(0, scrollOffset - 5); return; }
        if (keycode == KeyCode.HOME)      { scrollOffset = Math.max(0, displayLines.size() - MAX_VISIBLE); return; }
        if (keycode == KeyCode.END)       { scrollOffset = 0; }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void deleteLastChar() {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
        }
        lastDeleteNanos = System.nanoTime();
    }

    private boolean recentDeleteEvent() {
        return System.nanoTime() - lastDeleteNanos < 35_000_000L;
    }

    private void submitCommand() {
        String cmd = inputBuffer.toString().trim();
        inputBuffer.setLength(0);
        historyIdx = -1;

        addLine(new TerminalLine(challenge.getPrompt() + cmd, TerminalLine.C_WHITE));

        if (!cmd.isEmpty()) {
            cmdHistory.add(cmd);
        }

        if (cmd.isEmpty()) {
            addLine(TerminalLine.blank());
            return;
        }

        waitingForInput = false;
        revealTimer     = 0f;
        TerminalLine[] response = challenge.processInput(cmd);
        Collections.addAll(pendingLines, response);
        pendingLines.add(TerminalLine.blank());

        scrollOffset = 0;
    }

    private void addLine(TerminalLine line) {
        pendingLines.add(line);
    }

    private void clampScroll() {
        int max = Math.max(0, displayLines.size() - MAX_VISIBLE);
        scrollOffset = Math.min(scrollOffset, max);
    }

    private String fitToWidth(ITextDraw textDraw, String text, float maxWidth) {
        if (text == null) return "";
        if (textDraw.measureWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int end = text.length();
        while (end > 0) {
            String candidate = text.substring(0, end) + ellipsis;
            if (textDraw.measureWidth(candidate) <= maxWidth) return candidate;
            end--;
        }
        return ellipsis;
    }

    private String fitTailToWidth(ITextDraw textDraw, String text, float maxWidth) {
        if (text == null) return "";
        if (textDraw.measureWidth(text) <= maxWidth) return text;
        String prefix = "...";
        int start = 0;
        while (start < text.length()) {
            String candidate = prefix + text.substring(start);
            if (textDraw.measureWidth(candidate) <= maxWidth) return candidate;
            start++;
        }
        return prefix;
    }
}