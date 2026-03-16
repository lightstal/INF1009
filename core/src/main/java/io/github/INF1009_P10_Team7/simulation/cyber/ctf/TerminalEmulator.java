package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A full-featured terminal emulator widget for LibGDX.
 *
 * <p>Features:
 * <ul>
 *   <li>Real keyboard input via InputAdapter.keyTyped (captures shift, symbols)</li>
 *   <li>Typewriter reveal effect — one line per 45ms</li>
 *   <li>Command history via UP/DOWN arrows</li>
 *   <li>Mouse-wheel / PAGE UP/DOWN scrolling</li>
 *   <li>Blinking cursor</li>
 *   <li>Panic key (TAB) — instantly close terminal</li>
 * </ul>
 *
 * <p>Usage: call open(challenge), update(dt) each frame,
 * render(sr, batch, font) in the render pass, and close() when done.
 */
public class TerminalEmulator {

    // ---- Layout (HUD coords at 1280×704) ----
    private static final float WIN_X  = 32f;
    private static final float WIN_Y  = 24f;
    private static final float WIN_W  = 1216f;
    private static final float WIN_H  = 656f;
    private static final float TITLE_H= 34f;
    private static final float INPUT_H= 28f;
    private static final float PAD    = 14f;
    private static final float LINE_H = 18f;
    private static final int   MAX_VISIBLE = 32;

    // ---- State ----
    private boolean open = false;
    private boolean waitingForInput = true;   // false while typewriter is active
    private boolean panicked = false;         // set when TAB is pressed

    private ICTFChallenge challenge;
    private final List<TerminalLine>  displayLines  = new ArrayList<>();
    private final Deque<TerminalLine> pendingLines  = new ArrayDeque<>();
    private float revealTimer  = 0f;
    private static final float REVEAL_DELAY = 0.035f; // seconds per line

    private final StringBuilder    inputBuffer   = new StringBuilder();
    private final List<String>     cmdHistory    = new ArrayList<>();
    private int                    historyIdx    = -1;

    private float   cursorTimer = 0f;
    private boolean cursorVisible = true;
    private int     scrollOffset  = 0;     // lines scrolled up from bottom

    // ---- LibGDX input adapter ----
    private final TerminalInputAdapter adapter = new TerminalInputAdapter();

    // ---- Glitch effect ----
    private float glitchTimer = 0f;
    private float stateTime   = 0f;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    public void open(ICTFChallenge c) {
        challenge      = c;
        open           = true;
        panicked       = false;
        waitingForInput= false;   // start with typewriter for welcome
        displayLines.clear();
        pendingLines.clear();
        inputBuffer.setLength(0);
        cmdHistory.clear();
        historyIdx = -1;
        scrollOffset   = 0;
        stateTime      = 0f;
        cursorTimer    = 0f;

        // Queue welcome lines
        addLine(TerminalLine.dim("╔══════════════════════════════════════════════╗"));
        addLine(TerminalLine.info("  " + c.getTitle() + "  //  " + c.getTargetInfo()));
        addLine(TerminalLine.dim("╚══════════════════════════════════════════════╝"));
        addLine(TerminalLine.blank());
        for (TerminalLine l : c.getWelcomeLines()) addLine(l);
        addLine(TerminalLine.blank());

        Gdx.input.setInputProcessor(adapter);
    }

    public void close() {
        open    = false;
        panicked= false;
        Gdx.input.setInputProcessor(null);
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
        if (cursorTimer > 0.5f) { cursorTimer = 0f; cursorVisible = !cursorVisible; }

        // Typewriter reveal
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

        // Scroll down automatically when new lines appear and user hasn't scrolled up
        if (scrollOffset == 0) clampScroll();

        // Glitch when first opened (0.3s)
        if (stateTime < 0.3f) glitchTimer = stateTime;
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        if (!open) return;

        float glitch = (stateTime < 0.3f) ? (float)Math.sin(stateTime * 80f) * 3f : 0f;

        // ---- Background overlay ----
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.92f);
        sr.rect(0, 0, 1280, 704);

        // ---- Outer glow ----
        sr.setColor(0f, 0.9f, 0.4f, 0.15f);
        sr.rect(WIN_X - 4, WIN_Y - 4, WIN_W + 8, WIN_H + 8);

        // ---- Window body ----
        sr.setColor(0.03f, 0.05f, 0.04f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, WIN_H);

        // ---- Title bar ----
        sr.setColor(0f, 0.25f, 0.12f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_W, TITLE_H);

        // ---- Input bar background ----
        sr.setColor(0.02f, 0.08f, 0.04f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, INPUT_H);

        // ---- Input bar top border ----
        sr.setColor(0f, 0.6f, 0.25f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y + INPUT_H - 1, WIN_W, 1f);

        // ---- Scanlines ----
        sr.setColor(0f, 0f, 0f, 0.08f);
        for (float y = WIN_Y; y < WIN_Y + WIN_H; y += 3) {
            sr.rect(WIN_X, y, WIN_W, 1f);
        }

        // ---- Border ----
        sr.setColor(0f, 0.75f, 0.3f, 1f);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0f, 0.75f, 0.3f, 1f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, WIN_H);
        // Separator under title
        sr.line(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_X + WIN_W + glitch, WIN_Y + WIN_H - TITLE_H);
        sr.end();

        // ---- Text rendering ----
        batch.begin();
        font.getData().setScale(0.88f);

        // Title bar text
        font.setColor(0f, 1f, 0.45f, 1f);
        font.draw(batch, "  [ " + challenge.getTitle() + " ]   " + challenge.getTargetInfo(),
            WIN_X + PAD + glitch, WIN_Y + WIN_H - 8f);
        font.setColor(0.4f, 0.4f, 0.4f, 1f);
        font.draw(batch, "[TAB=PANIC CLOSE]  [PGUP/DN=SCROLL]",
            WIN_X + WIN_W - 230f + glitch, WIN_Y + WIN_H - 8f);

        // Output lines
        float textAreaH = WIN_H - TITLE_H - INPUT_H - PAD * 2;
        int maxLines = (int)(textAreaH / LINE_H);
        int total = displayLines.size();
        int start = Math.max(0, total - maxLines - scrollOffset);
        int end   = Math.min(total, start + maxLines);

        float lineY = WIN_Y + INPUT_H + PAD + (maxLines * LINE_H);
        for (int i = start; i < end; i++) {
            TerminalLine tl = displayLines.get(i);
            font.setColor(tl.color);
            font.draw(batch, tl.text, WIN_X + PAD + glitch, lineY);
            lineY -= LINE_H;
        }

        // Scroll indicator
        if (scrollOffset > 0) {
            font.setColor(TerminalLine.C_YELLOW);
            font.getData().setScale(0.75f);
            font.draw(batch, "▲ scrolled " + scrollOffset + " lines  (PGDN to go back)",
                WIN_X + WIN_W - 240f + glitch, WIN_Y + INPUT_H + 4f);
            font.getData().setScale(0.88f);
        }

        // ---- Input line ----
        String prompt = challenge.getPrompt();
        String inputStr = inputBuffer.toString();
        boolean showCursor = waitingForInput && cursorVisible;
        String cursor = showCursor ? "█" : " ";

        font.setColor(TerminalLine.C_GREEN);
        font.draw(batch, prompt, WIN_X + PAD + glitch, WIN_Y + INPUT_H - 5f);

        float promptW = getTextWidth(font, prompt);
        font.setColor(TerminalLine.C_WHITE);
        font.draw(batch, inputStr + cursor, WIN_X + PAD + promptW + glitch, WIN_Y + INPUT_H - 5f);

        // Solved flash
        if (challenge.isSolved()) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            font.setColor(0f, fl, fl * 0.5f, 1f);
            font.getData().setScale(1.1f);
            font.draw(batch, "  ✓ TERMINAL SECURED — KEY ACQUIRED", WIN_X + PAD, WIN_Y + 6f);
            font.getData().setScale(0.88f);
        }

        batch.end();
    }

    // =========================================================================
    // INPUT ADAPTER (inner class)
    // =========================================================================

    private class TerminalInputAdapter extends InputAdapter {

        @Override
        public boolean keyTyped(char c) {
            if (!open || !waitingForInput) return true;
            if (c == '\b') {                           // backspace
                if (inputBuffer.length() > 0)
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                return true;
            }
            if (c == '\r' || c == '\n') {              // enter
                submitCommand();
                return true;
            }
            if (c >= 32 && c < 127) {                  // printable ASCII
                inputBuffer.append(c);
            }
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (!open) return false;

            if (keycode == Input.Keys.TAB) {            // PANIC close
                panicked = true;
                close();
                return true;
            }
            if (keycode == Input.Keys.ESCAPE) {
                panicked = true;
                close();
                return true;
            }

            // Command history
            if (keycode == Input.Keys.UP) {
                if (!cmdHistory.isEmpty()) {
                    historyIdx = Math.min(historyIdx + 1, cmdHistory.size() - 1);
                    inputBuffer.setLength(0);
                    inputBuffer.append(cmdHistory.get(cmdHistory.size() - 1 - historyIdx));
                }
                return true;
            }
            if (keycode == Input.Keys.DOWN) {
                if (historyIdx > 0) {
                    historyIdx--;
                    inputBuffer.setLength(0);
                    inputBuffer.append(cmdHistory.get(cmdHistory.size() - 1 - historyIdx));
                } else {
                    historyIdx = -1;
                    inputBuffer.setLength(0);
                }
                return true;
            }

            // Scroll
            if (keycode == Input.Keys.PAGE_UP)   { scrollOffset += 5; clampScroll(); return true; }
            if (keycode == Input.Keys.PAGE_DOWN)  { scrollOffset = Math.max(0, scrollOffset - 5); return true; }
            if (keycode == Input.Keys.HOME)       { scrollOffset = Math.max(0, displayLines.size() - MAX_VISIBLE); return true; }
            if (keycode == Input.Keys.END)        { scrollOffset = 0; return true; }

            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            scrollOffset = Math.max(0, Math.min(
                scrollOffset + (int)(amountY * 2),
                Math.max(0, displayLines.size() - MAX_VISIBLE)));
            return true;
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private void submitCommand() {
        String cmd = inputBuffer.toString().trim();
        inputBuffer.setLength(0);
        historyIdx = -1;

        // Echo the command
        addLine(new TerminalLine(challenge.getPrompt() + cmd, TerminalLine.C_WHITE));

        if (!cmd.isEmpty()) {
            cmdHistory.add(cmd);
        }

        // Blank command = just newline
        if (cmd.isEmpty()) {
            addLine(TerminalLine.blank());
            return;
        }

        // Process and queue response
        waitingForInput = false;
        revealTimer     = 0f;
        TerminalLine[] response = challenge.processInput(cmd);
        for (TerminalLine l : response) pendingLines.add(l);
        pendingLines.add(TerminalLine.blank());

        // Scroll to bottom
        scrollOffset = 0;
    }

    private void addLine(TerminalLine line) {
        pendingLines.add(line);
    }

    private void clampScroll() {
        int max = Math.max(0, displayLines.size() - MAX_VISIBLE);
        scrollOffset = Math.min(scrollOffset, max);
    }

    private float getTextWidth(BitmapFont font, String text) {
        GlyphLayout gl = new GlyphLayout(font, text);
        return gl.width;
    }

    public InputAdapter getAdapter() { return adapter; }
}
