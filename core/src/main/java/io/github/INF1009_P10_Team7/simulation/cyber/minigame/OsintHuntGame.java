package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * CHALLENGE: OPEN SOURCE INTELLIGENCE (OSINT)
 *
 * Students must navigate to a REAL website, find the answer, and type it in.
 * The challenge title, URL, question, hint and accepted answer are all
 * injected via the constructor — so the same class serves both levels.
 *
 * Level 1:  https://owasp.org/Top10
 *           "What is the #1 vulnerability in the OWASP Top 10 (2021)?"
 *           Answer: "broken access control"
 *
 * Level 2:  https://nvd.nist.gov/vuln/detail/CVE-2017-0144
 *           "What Microsoft Security Bulletin number covers EternalBlue?"
 *           Answer: "MS17-010"
 */
public class OsintHuntGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    private final String challengeTitle;
    private final String missionUrl;
    private final String question;
    private final String hintText;
    private final String acceptedAnswer;   // checked case-insensitively (trimmed)

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime = 0f;
    private float   wrongFlash = 0f;
    private float   solveTimer = 0f;

    private final StringBuilder inputBuf = new StringBuilder();
    private final InputAdapter  adapter  = new OsintInputAdapter();

    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    @Override
    public void open() {
        open      = true;
        solved    = false;
        panicked  = false;
        stateTime = 0f;
        wrongFlash = 0f;
        solveTimer = 0f;
        inputBuf.setLength(0);
        Gdx.input.setInputProcessor(adapter);
    }

    @Override
    public void close() {
        open = false;
        Gdx.input.setInputProcessor(null);
    }

    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String getTitle()     { return challengeTitle; }

    // -------------------------------------------------------------------------
    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 2f) { open = false; }
        }
    }

    // -------------------------------------------------------------------------
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        if (!open) return;

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 2.5f);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // --- Overlay ---
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.90f);
        sr.rect(0, 0, W, H);

        float wx = 160f, wy = 70f, ww = 960f, wh = 564f;
        float titleH = 40f;

        // Window body
        sr.setColor(0.02f, 0.04f, 0.08f, 1f);
        sr.rect(wx, wy, ww, wh);

        // Title bar - dark blue
        sr.setColor(0.0f, 0.08f, 0.22f, 1f);
        sr.rect(wx, wy + wh - titleH, ww, titleH);

        // Glow
        sr.setColor(0.05f, 0.2f, pulse * 0.6f, 0.25f);
        sr.rect(wx - 6, wy - 6, ww + 12, wh + 12);

        // Mission briefing card
        sr.setColor(0.03f, 0.07f, 0.14f, 1f);
        sr.rect(wx + 40, wy + wh - 260f, ww - 80, 200f);

        // URL box
        sr.setColor(0.0f, 0.05f, 0.0f, 1f);
        sr.rect(wx + 60, wy + wh - 315f, ww - 120, 36f);

        // Question box
        sr.setColor(0.05f, 0.05f, 0f, 1f);
        sr.rect(wx + 40, wy + wh - 420f, ww - 80, 80f);

        // Answer input box
        float inputY = wy + 110f;
        Color inputBorder = wrongFlash > 0 ? new Color(1f, 0.1f, 0.1f, 1f)
                          : solved          ? new Color(0f, 1f, 0.4f, 1f)
                                           : new Color(0f, 0.6f, 0.3f, 0.8f);
        sr.setColor(0.02f, 0.05f, 0.02f, 1f);
        sr.rect(wx + 40, inputY, ww - 80, 34f);

        // Wrong flash
        if (wrongFlash > 0) {
            sr.setColor(0.8f, 0f, 0f, wrongFlash * 0.4f);
            sr.rect(wx, wy, ww, wh);
        }
        sr.end();

        // Borders
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.15f, 0.4f, pulse * 0.8f, 1f);
        sr.rect(wx, wy, ww, wh);
        sr.line(wx, wy + wh - titleH, wx + ww, wy + wh - titleH);
        sr.setColor(inputBorder);
        sr.rect(wx + 40, inputY, ww - 80, 34f);
        // URL box border
        sr.setColor(0f, 0.7f, 0.3f, 0.7f);
        sr.rect(wx + 60, wy + wh - 315f, ww - 120, 36f);
        sr.end();

        // --- Text ---
        batch.begin();

        // Title bar
        font.getData().setScale(1.1f);
        font.setColor(0.4f, 0.75f, 1f, 1f);
        font.draw(batch, "  [ " + challengeTitle + " ]    [TAB=CLOSE]",
            wx + 10, wy + wh - 13f);

        // "MISSION" header
        font.getData().setScale(0.9f);
        font.setColor(new Color(0.4f, 0.7f, 1f, 0.7f));
        font.draw(batch, "MISSION BRIEFING", wx + 60, wy + wh - 92f);

        font.getData().setScale(0.85f);
        font.setColor(Color.WHITE);
        font.draw(batch, "To complete this terminal you must leave the game and", wx + 60, wy + wh - 118f);
        font.draw(batch, "research the answer using an external resource.", wx + 60, wy + wh - 138f);
        font.draw(batch, "Return here and type your answer when ready.", wx + 60, wy + wh - 158f);

        // URL
        font.getData().setScale(0.85f);
        font.setColor(0.4f, 0.4f, 0.45f, 1f);
        font.draw(batch, "NAVIGATE TO:", wx + 68, wy + wh - 298f);
        font.getData().setScale(1.0f);
        font.setColor(0f, 1f, 0.5f, 1f);
        font.draw(batch, "  " + missionUrl, wx + 68, wy + wh - 314f);

        // Question
        font.getData().setScale(0.85f);
        font.setColor(1f, 0.85f, 0.1f, 1f);
        font.draw(batch, "QUESTION:", wx + 58, wy + wh - 356f);
        font.getData().setScale(0.95f);
        font.setColor(Color.WHITE);
        // Word-wrap manually at ~90 chars
        String q = question;
        if (q.length() > 75) {
            int mid = q.lastIndexOf(' ', 75);
            font.draw(batch, q.substring(0, mid), wx + 58, wy + wh - 376f);
            font.draw(batch, q.substring(mid + 1), wx + 58, wy + wh - 396f);
        } else {
            font.draw(batch, q, wx + 58, wy + wh - 376f);
        }

        // Hint
        font.getData().setScale(0.78f);
        font.setColor(0.35f, 0.5f, 0.35f, 1f);
        font.draw(batch, "HINT: " + hintText, wx + 58, wy + wh - 440f);

        // Input label
        font.getData().setScale(0.85f);
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        font.draw(batch, "TYPE YOUR ANSWER BELOW — PRESS [ENTER] TO SUBMIT:", wx + 42, wy + 168f);

        // Input prompt + text
        font.getData().setScale(1.0f);
        font.setColor(0f, 0.7f, 0.3f, 1f);
        font.draw(batch, "$ recon>", wx + 50, inputY + 24f);
        font.setColor(Color.WHITE);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;
        font.draw(batch, inputBuf.toString() + (blink ? "█" : " "), wx + 145, inputY + 24f);

        // Result
        font.getData().setScale(1.2f);
        if (solved) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            font.setColor(0f, fl, fl * 0.4f, 1f);
            font.draw(batch, "✓  INTEL ACQUIRED — ACCESS GRANTED  (closing...)", wx + 150, wy + 62f);
        } else if (wrongFlash > 0) {
            font.setColor(1f, 0.2f, 0.1f, 1f);
            font.draw(batch, "✗  INCORRECT — RE-CHECK YOUR SOURCE", wx + 250, wy + 62f);
        }

        batch.end();
    }

    // -------------------------------------------------------------------------
    private class OsintInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true;
                close();
                return true;
            }
            if (k == Input.Keys.BACKSPACE && inputBuf.length() > 0) {
                inputBuf.deleteCharAt(inputBuf.length() - 1);
                return true;
            }
            if (k == Input.Keys.ENTER) {
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
