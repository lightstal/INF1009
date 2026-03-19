package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * A full-featured terminal emulator widget for LibGDX.
 *
 * <p>Features:
 * <ul>
 *   <li>Real keyboard input via InputAdapter.keyTyped</li>
 *   <li>Typewriter reveal effect</li>
 *   <li>Command history via UP/DOWN arrows</li>
 *   <li>Mouse-wheel / PAGE UP/DOWN scrolling</li>
 *   <li>Blinking cursor</li>
 *   <li>Panic key (TAB)  -  instantly close terminal</li>
 * </ul>
 *
 * <p>BUG-1 FIX: render() now uses its own pre-scaled fonts created in open(),
 * instead of calling font.getData().setScale() on the shared hudFont.
 * This prevents blurry/wrong-sized HUD text after closing a terminal challenge.
 */
public class TerminalEmulator {

    // ---- Layout (HUD coords at 1280x704) ----
    private static final float WIN_X  = 32f;
    private static final float WIN_Y  = 24f;
    private static final float WIN_W  = 1216f;
    private static final float WIN_H  = 656f;
    private static final float TITLE_H= 40f;
    private static final float INPUT_H= 34f;
    private static final float PAD    = 16f;
    private static final float LINE_H = 18f;
    private static final int   MAX_VISIBLE = 32;

    // ---- State ----
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

    // ---- Own fonts (BUG-1 FIX) ----
    private BitmapFont bodyFont;     // 0.88 scale  -  main terminal text
    private BitmapFont smallFont;    // 0.75 scale  -  scroll indicator
    private BitmapFont solvedFont;   // 1.1  scale  -  solved flash
    private GlyphLayout glLayout;

    // ---- LibGDX input adapter ----
    private final TerminalInputAdapter adapter = new TerminalInputAdapter();

    // ---- Glitch effect ----
    private float glitchTimer = 0f;
    private float stateTime   = 0f;
    private long  lastDeleteNanos = 0L;
    private InputProcessor previousProcessor;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

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
        lastDeleteNanos = 0L;

        // Create own fonts at fixed scales  -  NEVER re-scaled later (BUG-1 FIX)
        disposeFonts();
        bodyFont   = makeFont(0.88f);
        smallFont  = makeFont(0.75f);
        solvedFont = makeFont(1.1f);
        glLayout   = new GlyphLayout();

        // Queue welcome lines
        addLine(TerminalLine.dim("============================================================"));
        addLine(TerminalLine.info("  " + c.getTitle() + "  //  " + c.getTargetInfo()));
        addLine(TerminalLine.dim("============================================================"));
        addLine(TerminalLine.blank());
        for (TerminalLine l : c.getWelcomeLines()) addLine(l);
        addLine(TerminalLine.blank());

        previousProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(adapter);
    }

    public void close() {
        open = false;
        disposeFonts();
        Gdx.input.setInputProcessor(previousProcessor);
        previousProcessor = null;
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
    }

    // =========================================================================
    // RENDER  (BUG-1 FIX: uses own fonts, never mutates the passed-in font)
    // =========================================================================

    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open || bodyFont == null) return;

        float glitch = (stateTime < 0.3f) ? (float)Math.sin(stateTime * 80f) * 3f : 0f;

        // ---- Background overlay ----
        sr.begin(ShapeRenderer.ShapeType.Filled);
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

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.15f, 1f, 0.55f, 0.9f);
        sr.rect(WIN_X + glitch, WIN_Y, WIN_W, WIN_H);
        sr.rect(WIN_X + 6 + glitch, WIN_Y + 6, WIN_W - 12, WIN_H - 12);
        sr.line(WIN_X + glitch, WIN_Y + WIN_H - TITLE_H, WIN_X + WIN_W + glitch, WIN_Y + WIN_H - TITLE_H);
        sr.end();

        // ---- Text rendering (own fonts) ----
        batch.begin();

        // Title bar text
        bodyFont.setColor(0f, 1f, 0.45f, 1f);
        float headerY = WIN_Y + WIN_H - 12f;
        float headerLeftW = WIN_W - PAD * 2f - 330f;
        String shellId = "analyst@kali:~/cases$";
        bodyFont.draw(batch, fitToWidth(bodyFont, shellId + "  " + challenge.getTitle(), headerLeftW),
            WIN_X + PAD + glitch, headerY);
        bodyFont.setColor(0.52f, 0.58f, 0.56f, 1f);
        bodyFont.draw(batch, fitToWidth(bodyFont, "ESC close | TAB panic | ENTER run | PgUp/PgDn scroll", 310f),
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
            bodyFont.setColor(tl.color);
            bodyFont.draw(batch, fitToWidth(bodyFont, tl.text, WIN_W - PAD * 2f - 10f), WIN_X + PAD + glitch, lineY);
            lineY -= LINE_H;
        }

        // Scroll indicator
        if (scrollOffset > 0) {
            smallFont.setColor(TerminalLine.C_YELLOW);
            smallFont.draw(batch, "^ scrolled " + scrollOffset + " lines (PGDN to go back)",
                WIN_X + WIN_W - 240f + glitch, WIN_Y + INPUT_H + 4f);
        }

        // ---- Input line ----
        String prompt = challenge.getPrompt();
        String inputStr = inputBuffer.toString();
        boolean showCursor = waitingForInput && cursorVisible;
        String cursor = showCursor ? "|" : " ";

        float inputY = WIN_Y + INPUT_H - 7f;
        bodyFont.setColor(TerminalLine.C_GREEN);
        String clippedPrompt = fitToWidth(bodyFont, prompt, 220f);
        bodyFont.draw(batch, clippedPrompt, WIN_X + PAD + glitch, inputY);

        glLayout.setText(bodyFont, clippedPrompt);
        float promptW = glLayout.width;
        float inputW = WIN_W - PAD * 2f - promptW - 18f;
        bodyFont.setColor(TerminalLine.C_WHITE);
        bodyFont.draw(batch, fitTailToWidth(bodyFont, inputStr + cursor, inputW), WIN_X + PAD + promptW + glitch, inputY);

        // Solved flash
        if (challenge.isSolved()) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            solvedFont.setColor(0f, fl, fl * 0.5f, 1f);
            solvedFont.draw(batch, "  [OK] TERMINAL SECURED - KEY ACQUIRED", WIN_X + PAD, WIN_Y + 6f);
        }

        batch.end();
    }

    // =========================================================================
    // INPUT ADAPTER
    // =========================================================================

    private class TerminalInputAdapter extends InputAdapter {

        @Override
        public boolean keyTyped(char c) {
            if (!open || !waitingForInput) return true;

            if (c == '\b' || c == 127) {
                if (!recentDeleteEvent()) deleteLastChar();
                return true;
            }
            if (c == '\r' || c == '\n') {
                submitCommand();
                return true;
            }
            if (c >= 32 && c < 127) {
                inputBuffer.append(c);
            }
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (!open) return false;

            if (keycode == Input.Keys.TAB || keycode == Input.Keys.ESCAPE) {
                panicked = true;
                close();
                return true;
            }

            boolean ctrlDown = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

            if (waitingForInput && ctrlDown && keycode == Input.Keys.V) {
                tryPasteClipboard();
                return true;
            }

            if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                if (ctrlDown) {
                    while (inputBuffer.length() > 0 && inputBuffer.charAt(inputBuffer.length() - 1) == ' ') {
                        inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    }
                    while (inputBuffer.length() > 0 && inputBuffer.charAt(inputBuffer.length() - 1) != ' ') {
                        inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    }
                    lastDeleteNanos = TimeUtils.nanoTime();
                } else {
                    deleteLastChar();
                }
                return true;
            }

            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                if (waitingForInput) submitCommand();
                return true;
            }

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

            if (keycode == Input.Keys.PAGE_UP)   { scrollOffset += 5; clampScroll(); return true; }
            if (keycode == Input.Keys.PAGE_DOWN) { scrollOffset = Math.max(0, scrollOffset - 5); return true; }
            if (keycode == Input.Keys.HOME)      { scrollOffset = Math.max(0, displayLines.size() - MAX_VISIBLE); return true; }
            if (keycode == Input.Keys.END)       { scrollOffset = 0; return true; }

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
    // HELPERS
    // =========================================================================

    private void deleteLastChar() {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
        }
        lastDeleteNanos = TimeUtils.nanoTime();
    }

    private boolean recentDeleteEvent() {
        return TimeUtils.timeSinceNanos(lastDeleteNanos) < 35_000_000L;
    }

    private void tryPasteClipboard() {
        try {
            if (Gdx.app.getClipboard() == null) return;
            String clip = Gdx.app.getClipboard().getContents();
            if (clip == null || clip.isEmpty()) return;
            for (int i = 0; i < clip.length(); i++) {
                char c = clip.charAt(i);
                if (c >= 32 && c < 127) inputBuffer.append(c);
            }
        } catch (Exception ignored) {
        }
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
        for (TerminalLine l : response) pendingLines.add(l);
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

    private String fitToWidth(BitmapFont font, String text, float maxWidth) {
        if (text == null) return "";
        glLayout.setText(font, text);
        if (glLayout.width <= maxWidth) return text;
        String ellipsis = "...";
        int end = text.length();
        while (end > 0) {
            String candidate = text.substring(0, end) + ellipsis;
            glLayout.setText(font, candidate);
            if (glLayout.width <= maxWidth) return candidate;
            end--;
        }
        return ellipsis;
    }

    private String fitTailToWidth(BitmapFont font, String text, float maxWidth) {
        if (text == null) return "";
        glLayout.setText(font, text);
        if (glLayout.width <= maxWidth) return text;
        String prefix = "...";
        int start = 0;
        while (start < text.length()) {
            String candidate = prefix + text.substring(start);
            glLayout.setText(font, candidate);
            if (glLayout.width <= maxWidth) return candidate;
            start++;
        }
        return prefix;
    }

    private BitmapFont makeFont(float scale) {
        return FontManager.create(scale);
    }

    private void disposeFonts() {
        if (bodyFont   != null) { bodyFont.dispose();   bodyFont   = null; }
        if (smallFont  != null) { smallFont.dispose();  smallFont  = null; }
        if (solvedFont != null) { solvedFont.dispose(); solvedFont = null; }
    }

    public InputAdapter getAdapter() { return adapter; }
}
