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
 * Renders an intercepted email chain the player must scroll to find an
 * OVERRIDE CODE, then type it into the input prompt.
 *
 * The highlight rectangle is drawn in the same ShapeRenderer pass as
 * the backgrounds (before batch.begin()) to avoid IllegalStateException.
 * Hint text is rendered above the input box on a dark strip for visibility.
 */
public class LogAnalysisGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;
    private static final String ACCEPTED_ANSWER = "override_7734_alpha";

    private static final String[] DOCUMENT = {
        "=========================================================",
        "  INTERCEPTED TRANSMISSION - FILE: corp_mail_dump.txt",
        "  Source: Internal SMTP relay @ 192.168.10.11",
        "=========================================================",
        "",
        "FROM:    audit-bot@corpserver.internal",
        "TO:      it-ops@corpserver.internal",
        "DATE:    Mon Nov 11 09:14:02 2024",
        "SUBJECT: [AUTOMATED] Credential Rotation Required - ACTION NEEDED",
        "",
        "-----BEGIN MESSAGE-----",
        "",
        "Dear IT Operations Team,",
        "",
        "Routine security audit has flagged THREE expired service tokens.",
        "Per policy CP-12, these must be rotated within 48 hours.",
        "",
        "  [1] backup-svc      token expired:  2024-11-08",
        "  [2] ci-pipeline     token expired:  2024-11-07",
        "  [3] monitoring-agent  last rotated: 347 days ago",
        "",
        "Please log in to the internal portal and rotate the above.",
        "Reference Internal Knowledge Base Article #7734 for the",
        "step-by-step procedure.",
        "",
        "-----END MESSAGE-----",
        "",
        "=========================================================",
        "",
        "FROM:    r.chen@corpserver.internal (Robert Chen, Head of IT)",
        "TO:      it-ops@corpserver.internal",
        "DATE:    Mon Nov 11 11:32:17 2024",
        "SUBJECT: RE: [AUTOMATED] Credential Rotation Required",
        "",
        "-----BEGIN MESSAGE-----",
        "",
        "Team,",
        "",
        "Quick update - I've pre-rotated the backup-svc and ci-pipeline",
        "tokens already. The monitoring-agent key is more sensitive,",
        "so I'm handling that one separately.",
        "",
        "For anyone who needs emergency access during the rotation window,",
        "the temporary OVERRIDE code is active until Thursday 23:59.",
        "",
        "     >>> OVERRIDE CODE: OVERRIDE_7734_ALPHA <<<",
        "",
        "DO NOT share this outside the ops team. Memorise it,",
        "then DELETE this email.",
        "",
        "(Yes I know I should not put this in email. I was in a rush.)",
        "   -- R. Chen",
        "",
        "-----END MESSAGE-----",
        "",
        "=========================================================",
        "",
        "FROM:    j.santos@corpserver.internal (Joao Santos, Sysadmin)",
        "TO:      r.chen@corpserver.internal",
        "DATE:    Mon Nov 11 14:05:44 2024",
        "SUBJECT: RE: RE: [AUTOMATED] Credential Rotation Required",
        "",
        "-----BEGIN MESSAGE-----",
        "",
        "Robert,",
        "",
        "Got it. I'll rotate monitoring-agent this evening after the",
        "backup window closes (~22:00). Will confirm once done.",
        "",
        "Also - you might want to send that override code via Signal",
        "next time, not email. The SMTP relay logs EVERYTHING.",
        "",
        "   -- J. Santos",
        "",
        "-----END MESSAGE-----",
        "",
        "=========================================================",
        "  END OF INTERCEPTED TRANSMISSION",
        "  Total messages: 3   |   Attachments: 0",
        "=========================================================",
    };

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime = 0f;

    // Own pre-scaled fonts
    private BitmapFont bigFont, medFont, smallFont;
    private GlyphLayout glLayout;

    private float   wrongFlash = 0f;
    private float   solveTimer = 0f;

    private int     scrollOffset = 0;
    private static final int VISIBLE_LINES = 32;

    private final StringBuilder inputBuf = new StringBuilder();
    private final InputAdapter  adapter  = new LogInputAdapter();

    private void buildFonts() {
        disposeFonts();
        bigFont = FontManager.create(1.1f);
        medFont = FontManager.create(0.9f);
        smallFont = FontManager.create(0.76f);
        glLayout  = new GlyphLayout();
    }
    private void disposeFonts() {
        if (bigFont   != null) { bigFont.dispose();   bigFont   = null; }
        if (medFont   != null) { medFont.dispose();   medFont   = null; }
        if (smallFont != null) { smallFont.dispose(); smallFont = null; }
    }

    @Override
    public void open() {
        open      = true;
        solved    = false;
        panicked  = false;
        stateTime = 0f;
        wrongFlash = 0f;
        solveTimer = 0f;
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
    @Override public String getTitle()     { return "DOC ANALYSIS // INTERCEPTED MAIL"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 2.2f) { close(); }
        }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open || medFont == null) return;

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 2f);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // Window layout — increased input area to fit hint above input
        float wx = 80f, wy = 30f, ww = 1120f, wh = 644f;
        float titleH = 38f;
        float inputAreaH = 68f;   // increased from 46 to accommodate hint line
        float docY  = wy + inputAreaH + 8f;
        float docH  = wh - titleH - inputAreaH - 16f;

        // Input box position
        float inputY = wy + 8f;
        // Hint strip sits between input box and document area
        float hintStripY = inputY + 34f + 4f;  // 4px above input box top
        float hintStripH = 18f;

        // ── ShapeRenderer pass 1: backgrounds ────────────────────────────────
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
        float maxScroll = Math.max(0, DOCUMENT.length - VISIBLE_LINES);
        float thumbH = Math.max(24, docH * VISIBLE_LINES / DOCUMENT.length);
        float thumbY = docY + (docH - thumbH) * (maxScroll > 0 ? (1f - (float)scrollOffset / maxScroll) : 1f);
        sr.setColor(0.4f, 0.3f, 0f, 0.8f);
        sr.rect(wx + ww - 24, thumbY, 10, thumbH);

        // Hint background strip (subtle dark strip for contrast)
        sr.setColor(0.08f, 0.06f, 0.02f, 1f);
        sr.rect(wx + 20, hintStripY, ww - 40, hintStripH);

        // Input box
        sr.setColor(0.03f, 0.05f, 0.03f, 1f);
        sr.rect(wx + 20, inputY, ww - 40, 34f);

        if (wrongFlash > 0) {
            sr.setColor(0.7f, 0f, 0f, wrongFlash * 0.35f);
            sr.rect(wx, wy, ww, wh);
        }

        // ── Draw highlight backgrounds in the same sr pass (before batch) ──
        float lineH  = 15f;
        float lineX  = wx + 30f;
        float lineYStart = docY + docH - 14f;
        int   start  = scrollOffset;
        int   end    = Math.min(DOCUMENT.length, start + VISIBLE_LINES);

        for (int i = start; i < end; i++) {
            String line = DOCUMENT[i];
            if (line.contains("OVERRIDE_7734_ALPHA") || line.contains("OVERRIDE CODE")) {
                float currentLineY = lineYStart - (i - start) * lineH;
                float hl = 0.3f + 0.2f * pulse;
                sr.setColor(0f, hl * 0.5f, 0f, 0.35f);
                sr.rect(lineX - 4, currentLineY - 14, ww - 60, lineH);
            }
        }

        sr.end();

        // ── Borders ─────────────────────────────────────────────────────────
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

        // ── Text (own fonts  -  no sr calls inside batch!) ────────────────────
        batch.begin();

        // Title bar
        bigFont.setColor(1f, 0.78f, 0.2f, 1f);
        medFont.draw(batch, "  [ DOC FORENSICS // INTERCEPTED MAIL DUMP ]    [ESC/TAB CLOSE]",
            wx + 10, wy + wh - 12f);
        smallFont.setColor(0.5f, 0.4f, 0.15f, 1f);
        smallFont.draw(batch, "[UP/DOWN = SCROLL]", wx + ww - 175f, wy + wh - 12f);

        // Document content
        float lineY = lineYStart;
        for (int i = start; i < end; i++) {
            String line = DOCUMENT[i];

            if (line.startsWith("===") || line.startsWith("---")) {
                medFont.setColor(0.3f, 0.5f, 0.55f, 1f);
            } else if (line.startsWith("FROM:") || line.startsWith("TO:") ||
                       line.startsWith("DATE:") || line.startsWith("SUBJECT:")) {
                medFont.setColor(0.85f, 0.70f, 0.25f, 1f);
            } else if (line.contains("OVERRIDE_7734_ALPHA") || line.contains("OVERRIDE CODE")) {
                medFont.setColor(0f, 1f, 0.4f, 1f);
            } else if (line.startsWith("  [") || line.startsWith("     >>>")) {
                medFont.setColor(0.9f, 0.9f, 0.55f, 1f);
            } else if (line.startsWith("Dear") || line.startsWith("Team,") ||
                       line.startsWith("Robert") || line.startsWith("   --")) {
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
            smallFont.draw(batch, "Line " + (scrollOffset + 1) + "/" + DOCUMENT.length + ". Press UP to scroll.",
                wx + 25, docY + docH - 2f);
        }
        if (scrollOffset < DOCUMENT.length - VISIBLE_LINES) {
            smallFont.setColor(0.5f, 0.4f, 0.1f, 1f);
            smallFont.draw(batch, "More below. Press DOWN to scroll.", wx + 25, docY + 16f);
        }

        // Input prompt
        medFont.setColor(0.9f, 0.65f, 0.1f, 1f);
        medFont.draw(batch, "$ submit>", wx + 28, inputY + 24f);
        medFont.setColor(Color.WHITE);
        medFont.draw(batch, inputBuf.toString() + (blink && !solved ? "|" : " "), wx + 120, inputY + 24f);

        // Hint text drawn ABOVE the input box on its own contrasting strip
        smallFont.setColor(0.7f, 0.55f, 0.2f, 1f);
        smallFont.draw(batch, "Read the document above. Find the OVERRIDE CODE and press ENTER.",
            wx + 28, hintStripY + 14f);

        // Result / status messages
        if (solved) {
            float fl = 0.5f + 0.5f * (float) Math.sin(stateTime * 8f);
            medFont.setColor(0f, fl, fl * 0.4f, 1f);
            medFont.draw(batch, "[OK]  CREDENTIAL EXTRACTED - KEY ACQUIRED  (closing...)", wx + 200, wy + 46f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.1f, 1f);
            medFont.draw(batch, "[X]  INCORRECT - KEEP READING THE DOCUMENT", wx + 310, wy + 46f);
        }

        batch.end();
    }

    private class LogInputAdapter extends InputAdapter {

        @Override
        public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true;
                close();
                return true;
            }
            if (solved) return false;

            if (k == Input.Keys.UP)   { scrollOffset = Math.max(0, scrollOffset - 1); return true; }
            if (k == Input.Keys.DOWN) { scrollOffset = Math.min(DOCUMENT.length - VISIBLE_LINES, scrollOffset + 1); return true; }
            if (k == Input.Keys.PAGE_UP)   { scrollOffset = Math.max(0, scrollOffset - 8); return true; }
            if (k == Input.Keys.PAGE_DOWN) { scrollOffset = Math.min(DOCUMENT.length - VISIBLE_LINES, scrollOffset + 8); return true; }
            if (k == Input.Keys.HOME) { scrollOffset = 0; return true; }
            if (k == Input.Keys.END)  { scrollOffset = Math.max(0, DOCUMENT.length - VISIBLE_LINES); return true; }

            if ((k == Input.Keys.BACKSPACE || k == Input.Keys.DEL || k == Input.Keys.FORWARD_DEL) && inputBuf.length() > 0) {
                inputBuf.deleteCharAt(inputBuf.length() - 1);
                return true;
            }
            if (k == Input.Keys.ENTER || k == Input.Keys.NUMPAD_ENTER) {
                String typed = inputBuf.toString().trim().toLowerCase();
                if (typed.equals(ACCEPTED_ANSWER)) {
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
            if (c >= 32 && c < 127 && inputBuf.length() < 32) {
                inputBuf.append(c);
            }
            return true;
        }

        @Override
        public boolean scrolled(float ax, float ay) {
            int mScroll = Math.max(0, DOCUMENT.length - VISIBLE_LINES);
            scrollOffset  = Math.max(0, Math.min(mScroll, scrollOffset + (int)(ay * 2)));
            return true;
        }
    }
}
