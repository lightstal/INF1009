package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * CHALLENGE: INTERCEPTED DOCUMENT ANALYSIS
 *
 * The player reads an intercepted email chain, finds the override code,
 * and types it into the prompt.
 *
 * All content is injected via the constructor so each level can have a
 * completely different email and answer without touching this file:
 *
 *   Level 1 (easy)  — plaintext override code sitting in the email.
 *                     Player just reads and copies it.
 *
 *   Level 2 (hard)  — override code is Atbash-encoded (A<->Z mirror cipher).
 *                     Player must decode it using hints in the email.
 *
 * To change either level's content, edit only the strings passed to the
 * constructor inside LevelConfig.
 */
/**
 * LogAnalysisGame — mini-game where the player analyses an intercepted email/log
 * document and extracts a hidden override code.
 *
 * <p>The document content, expected answer, highlight keyword, and hint text are
 * all configurable at construction time (supplied per-level from
 * {@link io.github.INF1009_P10_Team7.simulation.cyber.Level1Config} and
 * {@link io.github.INF1009_P10_Team7.simulation.cyber.Level2Config}).
 * Level 1 uses a plaintext code; Level 2 encodes it with Atbash cipher.</p>
 *
 * <p>Implements {@link IMiniGame} (OCP, LSP).</p>
 */
public class LogAnalysisGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;
    private static final int   VISIBLE_LINES = 32;

    // ── Content injected via constructor ──────────────────────────────────────
    // document       — array of lines shown in the scrollable email viewer
    // acceptedAnswer — lowercase string the player must type to win
    // highlightWord  — any line containing this string gets a green highlight
    // hintText       — instruction shown in the strip above the input box
    // wrongText      — message shown when the player types a wrong answer
    private final String[] document;
    private final String   acceptedAnswer;
    private final String   highlightWord;
    private final String   hintText;
    private final String   wrongText;
    // ─────────────────────────────────────────────────────────────────────────

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime = 0f;

    private BitmapFont bigFont, medFont, smallFont;
    private GlyphLayout glLayout;

    private float wrongFlash = 0f;
    private float solveTimer = 0f;

    private int scrollOffset = 0;

    private final StringBuilder inputBuf = new StringBuilder();
    private final InputAdapter  adapter  = new LogInputAdapter();

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * @param document       Lines of the email document to display.
     * @param acceptedAnswer The plaintext answer the player must type (lowercase).
     * @param highlightWord  Any line containing this word gets a green highlight.
     * @param hintText       Short instruction shown above the input box.
     * @param wrongText      Message shown when the player submits a wrong answer.
     */
    public LogAnalysisGame(String[] document, String acceptedAnswer,
                           String highlightWord, String hintText, String wrongText) {
        this.document       = document;
        this.acceptedAnswer = acceptedAnswer;
        this.highlightWord  = highlightWord;
        this.hintText       = hintText;
        this.wrongText      = wrongText;
    }

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private void buildFonts() {
        disposeFonts();
        bigFont   = FontManager.create(1.1f);
        medFont   = FontManager.create(0.9f);
        smallFont = FontManager.create(0.76f);
        glLayout  = new GlyphLayout();
    }
    private void disposeFonts() {
        if (bigFont   != null) { bigFont.dispose();   bigFont   = null; }
        if (medFont   != null) { medFont.dispose();   medFont   = null; }
        if (smallFont != null) { smallFont.dispose(); smallFont = null; }
    }

    // ── IMiniGame ─────────────────────────────────────────────────────────────
    @Override
    public void open() {
        open         = true;
        solved       = false;
        panicked     = false;
        stateTime    = 0f;
        wrongFlash   = 0f;
        solveTimer   = 0f;
        scrollOffset = 0;
        inputBuf.setLength(0);
        buildFonts();
        Gdx.input.setInputProcessor(adapter);
    }

    @Override
    public void close() {
        open = false;
        disposeFonts();
        Gdx.input.setInputProcessor(null);
    }

    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String  getTitle()    { return "DOC ANALYSIS // INTERCEPTED MAIL"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 2.2f) close();
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open || medFont == null) return;

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 2f);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;

        Gdx.gl.glEnable(GL20.GL_BLEND);

        float wx = 80f, wy = 30f, ww = 1120f, wh = 644f;
        float titleH     = 38f;
        float inputAreaH = 68f;
        float docY  = wy + inputAreaH + 8f;
        float docH  = wh - titleH - inputAreaH - 16f;

        float inputY     = wy + 8f;
        float hintStripY = inputY + 34f + 4f;
        float hintStripH = 18f;

        // ── ShapeRenderer: backgrounds ────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.92f);
        sr.rect(0, 0, W, H);

        sr.setColor(0.03f, 0.04f, 0.06f, 1f);
        sr.rect(wx, wy, ww, wh);
        sr.setColor(0.12f, 0.08f, 0.0f, 1f);
        sr.rect(wx, wy + wh - titleH, ww, titleH);
        sr.setColor(0.5f, 0.35f * pulse, 0f, 0.18f);
        sr.rect(wx - 5, wy - 5, ww + 10, wh + 10);
        sr.setColor(0.01f, 0.03f, 0.04f, 1f);
        sr.rect(wx + 20, docY, ww - 40, docH);

        // Scrollbar
        sr.setColor(0.06f, 0.06f, 0.08f, 1f);
        sr.rect(wx + ww - 24, docY, 10, docH);
        float maxScroll = Math.max(0, document.length - VISIBLE_LINES);
        float thumbH    = Math.max(24, docH * VISIBLE_LINES / document.length);
        float thumbY    = docY + (docH - thumbH) * (maxScroll > 0 ? (1f - (float) scrollOffset / maxScroll) : 1f);
        sr.setColor(0.4f, 0.3f, 0f, 0.8f);
        sr.rect(wx + ww - 24, thumbY, 10, thumbH);

        // Hint strip background
        sr.setColor(0.08f, 0.06f, 0.02f, 1f);
        sr.rect(wx + 20, hintStripY, ww - 40, hintStripH);

        // Input box
        sr.setColor(0.03f, 0.05f, 0.03f, 1f);
        sr.rect(wx + 20, inputY, ww - 40, 34f);

        if (wrongFlash > 0) {
            sr.setColor(0.7f, 0f, 0f, wrongFlash * 0.35f);
            sr.rect(wx, wy, ww, wh);
        }

        // Green highlight on lines containing the highlight word
        float lineH      = 15f;
        float lineX      = wx + 30f;
        float lineYStart = docY + docH - 14f;
        int   start      = scrollOffset;
        int   end        = Math.min(document.length, start + VISIBLE_LINES);

        for (int i = start; i < end; i++) {
            if (document[i].contains(highlightWord)) {
                float currentLineY = lineYStart - (i - start) * lineH;
                float hl = 0.3f + 0.2f * pulse;
                sr.setColor(0f, hl * 0.5f, 0f, 0.35f);
                sr.rect(lineX - 4, currentLineY - 14, ww - 60, lineH);
            }
        }

        sr.end();

        // ── Borders ───────────────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.6f, 0.45f * pulse, 0.05f, 1f);
        sr.rect(wx, wy, ww, wh);
        sr.line(wx, wy + wh - titleH, wx + ww, wy + wh - titleH);
        sr.setColor(wrongFlash > 0 ? 1f : 0f,
            wrongFlash > 0 ? 0.1f : 0.5f,
            0f, 1f);
        sr.rect(wx + 20, inputY, ww - 40, 34f);
        sr.setColor(0.3f, 0.22f, 0f, 0.5f);
        sr.rect(wx + 20, docY, ww - 40, docH);
        sr.end();

        // ── Text ──────────────────────────────────────────────────────────────
        batch.begin();

        // Title bar
        bigFont.setColor(1f, 0.78f, 0.2f, 1f);
        medFont.draw(batch, "  [ DOC FORENSICS // INTERCEPTED MAIL DUMP ]    [ESC/TAB CLOSE]",
            wx + 10, wy + wh - 12f);
        smallFont.setColor(0.5f, 0.4f, 0.15f, 1f);
        smallFont.draw(batch, "[UP/DOWN = SCROLL]", wx + ww - 175f, wy + wh - 12f);

        // Document lines with syntax colouring
        float lineY = lineYStart;
        for (int i = start; i < end; i++) {
            String line = document[i];

            if (line.startsWith("===") || line.startsWith("---")) {
                medFont.setColor(0.3f, 0.5f, 0.55f, 1f);
            } else if (line.startsWith("FROM:") || line.startsWith("TO:")
                || line.startsWith("DATE:") || line.startsWith("SUBJECT:")) {
                medFont.setColor(0.85f, 0.70f, 0.25f, 1f);
            } else if (line.contains(highlightWord)) {
                medFont.setColor(0f, 1f, 0.4f, 1f);   // bright green — draws the eye
            } else if (line.startsWith("  [") || line.startsWith("     >>>")) {
                medFont.setColor(0.9f, 0.9f, 0.55f, 1f);
            } else if (line.startsWith("Dear") || line.startsWith("Team,")
                || line.startsWith("Robert") || line.startsWith("   --")) {
                medFont.setColor(0.82f, 0.82f, 0.82f, 1f);
            } else if (line.isEmpty()) {
                medFont.setColor(0.2f, 0.2f, 0.2f, 1f);
            } else {
                medFont.setColor(0.72f, 0.72f, 0.72f, 1f);
            }
            medFont.draw(batch, line, lineX, lineY);
            lineY -= lineH;
        }

        // Scroll indicators
        if (scrollOffset > 0) {
            smallFont.setColor(0.5f, 0.4f, 0.1f, 1f);
            smallFont.draw(batch, "Line " + (scrollOffset + 1) + "/" + document.length + ". Press UP to scroll.",
                wx + 25, docY + docH - 2f);
        }
        if (scrollOffset < document.length - VISIBLE_LINES) {
            smallFont.setColor(0.5f, 0.4f, 0.1f, 1f);
            smallFont.draw(batch, "More below. Press DOWN to scroll.", wx + 25, docY + 16f);
        }

        // Input prompt
        medFont.setColor(0.9f, 0.65f, 0.1f, 1f);
        medFont.draw(batch, "$ submit>", wx + 28, inputY + 24f);
        medFont.setColor(Color.WHITE);
        medFont.draw(batch, inputBuf.toString() + (blink && !solved ? "|" : " "), wx + 120, inputY + 24f);

        // Hint strip
        smallFont.setColor(0.7f, 0.55f, 0.2f, 1f);
        smallFont.draw(batch, hintText, wx + 28, hintStripY + 14f);

        // Result messages
        if (solved) {
            float fl = 0.5f + 0.5f * (float) Math.sin(stateTime * 8f);
            medFont.setColor(0f, fl, fl * 0.4f, 1f);
            medFont.draw(batch, "[OK]  CREDENTIAL EXTRACTED - KEY ACQUIRED  (closing...)", wx + 200, wy + 46f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.1f, 1f);
            medFont.draw(batch, "[X]  " + wrongText, wx + 310, wy + 46f);
        }

        batch.end();
    }

    // ── Input adapter ─────────────────────────────────────────────────────────
    private class LogInputAdapter extends InputAdapter {

        @Override
        public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true; close(); return true;
            }
            if (solved) return false;

            if (k == Input.Keys.UP)        { scrollOffset = Math.max(0, scrollOffset - 1); return true; }
            if (k == Input.Keys.DOWN)      { scrollOffset = Math.min(document.length - VISIBLE_LINES, scrollOffset + 1); return true; }
            if (k == Input.Keys.PAGE_UP)   { scrollOffset = Math.max(0, scrollOffset - 8); return true; }
            if (k == Input.Keys.PAGE_DOWN) { scrollOffset = Math.min(document.length - VISIBLE_LINES, scrollOffset + 8); return true; }
            if (k == Input.Keys.HOME)      { scrollOffset = 0; return true; }
            if (k == Input.Keys.END)       { scrollOffset = Math.max(0, document.length - VISIBLE_LINES); return true; }

            if ((k == Input.Keys.BACKSPACE || k == Input.Keys.DEL || k == Input.Keys.FORWARD_DEL)
                && inputBuf.length() > 0) {
                inputBuf.deleteCharAt(inputBuf.length() - 1); return true;
            }
            if (k == Input.Keys.ENTER || k == Input.Keys.NUMPAD_ENTER) {
                String typed = inputBuf.toString().trim().toLowerCase();
                if (typed.equals(acceptedAnswer)) {
                    solved     = true;
                    solveTimer = 0f;
                } else {
                    wrongFlash = 0.8f;
                    inputBuf.setLength(0);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char c) {
            if (solved || !open) return true;
            if (c >= 32 && c < 127 && inputBuf.length() < 32) inputBuf.append(c);
            return true;
        }

        @Override
        public boolean scrolled(float ax, float ay) {
            int mScroll = Math.max(0, document.length - VISIBLE_LINES);
            scrollOffset = Math.max(0, Math.min(mScroll, scrollOffset + (int)(ay * 2)));
            return true;
        }
    }
}
