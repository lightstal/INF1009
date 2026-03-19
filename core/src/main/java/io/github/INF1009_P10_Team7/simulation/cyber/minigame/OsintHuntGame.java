package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * CHALLENGE: OPEN SOURCE INTELLIGENCE (OSINT)
 *
 * Students must navigate to a REAL website, find the answer, and type it in.
 *
 * BUG-1 FIX: render() now uses its own pre-scaled fonts created in open(),
 * instead of calling font.getData().setScale() on the shared hudFont.
 *
 * LAYOUT FIX: Redistributed vertical space — input box moved higher,
 * hint placed between question and input, "NAVIGATE TO:" styled prominently,
 * dead space at bottom eliminated.
 */
public class OsintHuntGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    private final String challengeTitle;
    private final String missionUrl;
    private final String question;
    private final String hintText;
    private final String acceptedAnswer;

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime = 0f;
    private float   wrongFlash = 0f;
    private float   solveTimer = 0f;

    // Own pre-scaled fonts (BUG-1 FIX)
    private BitmapFont titleFont;   // 1.1
    private BitmapFont bodyFont;    // 0.85
    private BitmapFont urlFont;     // 1.0
    private BitmapFont questionFont;// 0.95
    private BitmapFont hintFont;    // 0.78
    private BitmapFont resultFont;  // 1.2
    private BitmapFont headerFont;  // 0.9
    private BitmapFont navLabelFont;// 0.95 — prominent "NAVIGATE TO:" label

    private final StringBuilder inputBuf = new StringBuilder();
    private final InputAdapter  adapter  = new OsintInputAdapter();

    public OsintHuntGame(String challengeTitle,
                         String missionUrl,
                         String question,
                         String hintText,
                         String acceptedAnswer) {
        this.challengeTitle = challengeTitle;
        this.missionUrl     = missionUrl;
        this.question       = question;
        this.hintText       = hintText;
        this.acceptedAnswer = acceptedAnswer.trim().toLowerCase();
    }

    @Override
    public void open() {
        open      = true;
        solved    = false;
        panicked  = false;
        stateTime = 0f;
        wrongFlash = 0f;
        solveTimer = 0f;
        inputBuf.setLength(0);

        // Create own fonts at fixed scales  -  NEVER re-scaled later
        disposeFonts();
        titleFont    = makeFont(1.1f);
        bodyFont     = makeFont(0.85f);
        urlFont      = makeFont(1.0f);
        questionFont = makeFont(0.95f);
        hintFont     = makeFont(0.78f);
        resultFont   = makeFont(1.2f);
        headerFont   = makeFont(0.9f);
        navLabelFont = makeFont(0.95f);

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
    @Override public String getTitle()     { return challengeTitle; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 2f) { close(); }
        }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open || bodyFont == null) return;

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 2.5f);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // --- Window geometry (redistributed to reduce dead space) ---
        float wx = 160f, wy = 40f, ww = 960f, wh = 624f;
        float titleH = 40f;

        // Vertical layout positions (top to bottom, all relative to window)
        float briefingTop  = wy + wh - titleH - 16f;   // below title bar
        float briefingH    = 110f;
        float briefingBot  = briefingTop - briefingH;

        float navTop       = briefingBot - 20f;          // navigate section
        float navBoxH      = 36f;

        float qTop         = navTop - navBoxH - 16f;     // question section
        float qBoxH        = 80f;

        float hintY        = qTop - qBoxH - 8f;          // hint (between question and input)

        float instrY       = hintY - 22f;                 // "TYPE YOUR ANSWER" instruction

        float inputY       = instrY - 30f;                // input box
        float inputH       = 34f;

        float resultY      = inputY - 30f;                // result / status area

        // --- Overlay ---
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.90f);
        sr.rect(0, 0, W, H);

        // Window background
        sr.setColor(0.02f, 0.04f, 0.08f, 1f);
        sr.rect(wx, wy, ww, wh);

        // Title bar
        sr.setColor(0.0f, 0.08f, 0.22f, 1f);
        sr.rect(wx, wy + wh - titleH, ww, titleH);

        // Glow border
        sr.setColor(0.05f, 0.2f, pulse * 0.6f, 0.25f);
        sr.rect(wx - 6, wy - 6, ww + 12, wh + 12);

        // Mission briefing background
        sr.setColor(0.03f, 0.07f, 0.14f, 1f);
        sr.rect(wx + 40, briefingBot, ww - 80, briefingH);

        // Navigate box background
        sr.setColor(0.0f, 0.05f, 0.0f, 1f);
        sr.rect(wx + 60, navTop - navBoxH, ww - 120, navBoxH);

        // Question background
        sr.setColor(0.05f, 0.05f, 0f, 1f);
        sr.rect(wx + 40, qTop - qBoxH, ww - 80, qBoxH);

        // Input box background
        sr.setColor(0.02f, 0.05f, 0.02f, 1f);
        sr.rect(wx + 40, inputY, ww - 80, inputH);

        // Wrong flash overlay
        if (wrongFlash > 0) {
            sr.setColor(0.8f, 0f, 0f, wrongFlash * 0.4f);
            sr.rect(wx, wy, ww, wh);
        }
        sr.end();

        // --- Borders ---
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.15f, 0.4f, pulse * 0.8f, 1f);
        sr.rect(wx, wy, ww, wh);
        sr.line(wx, wy + wh - titleH, wx + ww, wy + wh - titleH);

        // Input border (changes color on wrong/solved)
        Color inputBorder = wrongFlash > 0 ? new Color(1f, 0.1f, 0.1f, 1f)
                          : solved          ? new Color(0f, 1f, 0.4f, 1f)
                                           : new Color(0f, 0.6f, 0.3f, 0.8f);
        sr.setColor(inputBorder);
        sr.rect(wx + 40, inputY, ww - 80, inputH);

        // Navigate box border
        sr.setColor(0f, 0.7f, 0.3f, 0.7f);
        sr.rect(wx + 60, navTop - navBoxH, ww - 120, navBoxH);
        sr.end();

        // --- Text (own fonts) ---
        batch.begin();

        // Title bar
        titleFont.setColor(0.4f, 0.75f, 1f, 1f);
        titleFont.draw(batch, "  [ " + challengeTitle + " ]    [ESC/TAB CLOSE]",
            wx + 10, wy + wh - 13f);

        // Mission briefing
        headerFont.setColor(new Color(0.4f, 0.7f, 1f, 0.7f));
        headerFont.draw(batch, "MISSION BRIEFING", wx + 60, briefingTop - 12f);

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "To complete this terminal you must leave the game and", wx + 60, briefingTop - 36f);
        bodyFont.draw(batch, "research the answer using an external resource.", wx + 60, briefingTop - 56f);
        bodyFont.draw(batch, "Return here and type your answer when ready.", wx + 60, briefingTop - 76f);

        // Navigate label — bright and prominent
        navLabelFont.setColor(0.2f, 0.9f, 0.6f, 1f);
        navLabelFont.draw(batch, "> NAVIGATE TO:", wx + 68, navTop - 4f);
        urlFont.setColor(0f, 1f, 0.5f, 1f);
        urlFont.draw(batch, "  " + missionUrl, wx + 68, navTop - navBoxH + 10f);

        // Question
        bodyFont.setColor(1f, 0.85f, 0.1f, 1f);
        bodyFont.draw(batch, "QUESTION:", wx + 58, qTop - 8f);
        questionFont.setColor(Color.WHITE);
        String q = question;
        if (q.length() > 75) {
            int mid = q.lastIndexOf(' ', 75);
            questionFont.draw(batch, q.substring(0, mid),  wx + 58, qTop - 28f);
            questionFont.draw(batch, q.substring(mid + 1), wx + 58, qTop - 48f);
        } else {
            questionFont.draw(batch, q, wx + 58, qTop - 28f);
        }

        // Hint — placed between question and input, readable color
        hintFont.setColor(0.45f, 0.7f, 0.45f, 1f);
        hintFont.draw(batch, "HINT: " + hintText, wx + 58, hintY);

        // Instruction
        bodyFont.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        bodyFont.draw(batch, "TYPE YOUR ANSWER BELOW - PRESS [ENTER] TO SUBMIT", wx + 42, instrY);

        // Input prompt
        urlFont.setColor(0f, 0.7f, 0.3f, 1f);
        urlFont.draw(batch, "$ recon>", wx + 50, inputY + 24f);
        urlFont.setColor(Color.WHITE);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;
        urlFont.draw(batch, inputBuf.toString() + (blink ? "|" : " "), wx + 145, inputY + 24f);

        // Result / status
        if (solved) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            resultFont.setColor(0f, fl, fl * 0.4f, 1f);
            resultFont.draw(batch, "[OK]  INTEL ACQUIRED - ACCESS GRANTED  (closing...)", wx + 150, resultY);
        } else if (wrongFlash > 0) {
            resultFont.setColor(1f, 0.2f, 0.1f, 1f);
            resultFont.draw(batch, "[X]  INCORRECT - RE-CHECK YOUR SOURCE", wx + 250, resultY);
        }

        batch.end();
    }

    // ---- Font helpers ----
    private BitmapFont makeFont(float scale) {
        return FontManager.create(scale);
    }

    private void disposeFonts() {
        if (titleFont    != null) { titleFont.dispose();    titleFont    = null; }
        if (bodyFont     != null) { bodyFont.dispose();     bodyFont     = null; }
        if (urlFont      != null) { urlFont.dispose();      urlFont     = null; }
        if (questionFont != null) { questionFont.dispose(); questionFont = null; }
        if (hintFont     != null) { hintFont.dispose();     hintFont     = null; }
        if (resultFont   != null) { resultFont.dispose();   resultFont   = null; }
        if (headerFont   != null) { headerFont.dispose();   headerFont   = null; }
        if (navLabelFont != null) { navLabelFont.dispose(); navLabelFont = null; }
    }

    private class OsintInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true;
                close();
                return true;
            }
            if ((k == Input.Keys.BACKSPACE || k == Input.Keys.DEL || k == Input.Keys.FORWARD_DEL) && inputBuf.length() > 0) {
                inputBuf.deleteCharAt(inputBuf.length() - 1);
                return true;
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
            if (c >= 32 && c < 127 && inputBuf.length() < 64) {
                inputBuf.append(c);
            }
            return true;
        }
    }
}
