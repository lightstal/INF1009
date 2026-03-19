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
 * CAESAR CIPHER DECODER
 *
 * Two alphabet strips slide relative to each other.
 * Students rotate with A/D (or LEFT/RIGHT) until the DECODED output
 * reads "HELLO HACKER", then press ENTER to submit.
 * Correct shift = 13 (ROT-13).
 *
 * FIX: All fonts are created at a fixed scale  -  setScale() is
 * NEVER called inside render(), which caused the original
 * misalignment visible in the screenshots.
 */
public class CaesarCipherGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    private static final String ENCRYPTED    = "URYYB UNPXRE";
    private static final String SOLUTION     = "HELLO HACKER";
    private static final int    CORRECT_SHIFT = 13;

    // Window geometry (constant)
    private static final float WX = 140f, WY = 60f, WW = 1000f, WH = 580f;
    private static final float TITLE_H = 40f;

    private boolean open, solved, panicked;
    private int     shift;
    private float   stateTime, wrongFlash, solveTimer;

    // Pre-built fonts at fixed scales  -  never touched after open()
    private BitmapFont bigFont;     // 1.6×  -  encrypted / decoded text
    private BitmapFont medFont;     // 1.0×  -  labels, strip letters
    private BitmapFont smallFont;   // 0.78×  -  hints, nav text
    private GlyphLayout layout;

    private final InputAdapter captureAdapter = new InputAdapter() {
        @Override public boolean keyDown(int k) {
            if (!open || solved) return false;
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true; close(); return true;
            }
            if (k == Input.Keys.LEFT  || k == Input.Keys.A) { shift = (shift+25)%26; return true; }
            if (k == Input.Keys.RIGHT || k == Input.Keys.D) { shift = (shift+1)%26;  return true; }
            if (k == Input.Keys.ENTER) {
                if (shift == CORRECT_SHIFT) { solved = true; solveTimer = 0f; }
                else wrongFlash = 0.7f;
                return true;
            }
            return false;
        }
    };

    @Override
    public void open() {
        open = true; solved = false; panicked = false;
        shift = 0; stateTime = 0f; wrongFlash = 0f; solveTimer = 0f;
        buildFonts();
        Gdx.input.setInputProcessor(captureAdapter);
    }

    private void buildFonts() {
        disposeFonts();
        bigFont = FontManager.create(1.6f);
        medFont = FontManager.create(1.0f);
        smallFont = FontManager.create(0.78f);
        layout    = new GlyphLayout();
    }
    private void disposeFonts() {
        if (bigFont   != null) bigFont.dispose();
        if (medFont   != null) medFont.dispose();
        if (smallFont != null) smallFont.dispose();
        bigFont = medFont = smallFont = null;
    }

    @Override public void close()          { open = false; disposeFonts(); Gdx.input.setInputProcessor(null); }
    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String  getTitle()    { return "CRYPTOGRAPHY // CAESAR CIPHER"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 1.8f) close();
        }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignored) {
        if (!open || bigFont == null) return;
        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 3f);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // ── Background ──────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.88f); sr.rect(0, 0, W, H);
        sr.setColor(0.04f, 0.04f, 0.08f, 1f); sr.rect(WX, WY, WW, WH);
        sr.setColor(0f, 0.10f, 0.28f, 1f); sr.rect(WX, WY+WH-TITLE_H, WW, TITLE_H);
        sr.setColor(0.08f, 0.25f, pulse*0.7f, 0.25f); sr.rect(WX-4, WY-4, WW+8, WH+8);
        sr.end();

        // ── Alphabet strip backgrounds ─────────────────────────────────
        // cipher strip Y = WY + 390,  plain strip Y = WY + 310
        float stripCipherY = WY + 390f;
        float stripPlainY  = WY + 305f;
        float stripH = 52f;
        float stripX = WX + 10f, stripW = WW - 20f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.02f, 0.02f, 0.06f, 1f);
        sr.rect(stripX, stripCipherY, stripW, stripH);
        sr.rect(stripX, stripPlainY,  stripW, stripH);

        // Active-column highlight (centred on letter 13 = N in cipher)
        float lw = stripW / 26f;
        float hlX = stripX + 12 * lw;
        sr.setColor(0f, 0.35f, pulse*0.8f, 0.30f);
        sr.rect(hlX, stripPlainY - 2, lw, stripH*2 + 14f);

        // Wrong flash overlay
        if (wrongFlash > 0) {
            sr.setColor(0.5f, 0f, 0f, wrongFlash * 0.4f);
            sr.rect(WX, WY, WW, WH);
        }
        sr.end();

        // ── Borders ─────────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.2f, 0.5f, pulse, 1f); sr.rect(WX, WY, WW, WH);
        sr.line(WX, WY+WH-TITLE_H, WX+WW, WY+WH-TITLE_H);
        sr.setColor(0f, 0.45f, 0.7f, 0.5f);
        sr.rect(stripX, stripCipherY, stripW, stripH);
        sr.rect(stripX, stripPlainY,  stripW, stripH);
        sr.end();

        // ── Text ────────────────────────────────────────────────────────
        batch.begin();

        // Title bar
        medFont.setColor(0.4f, 0.75f, 1f, 1f);
        medFont.draw(batch, "  [ CRYPTOGRAPHY // CAESAR CIPHER DECODER ]    [ESC/TAB = CLOSE]",
            WX + 10, WY + WH - 12f);

        // ── CIPHER row label + letters ─────────────────────────────────
        smallFont.setColor(0.4f, 0.5f, 0.7f, 1f);
        smallFont.draw(batch, "CIPHER", WX + 12, stripCipherY + 36f);

        for (int i = 0; i < 26; i++) {
            float lx = stripX + 40f + i * lw;
            char  c  = (char)('A' + i);
            boolean hi = (i == 12);  // 'N' column = shift 13 reference
            medFont.setColor(hi ? Color.CYAN : new Color(0.55f, 0.72f, 1f, 1f));
            medFont.draw(batch, String.valueOf(c), lx, stripCipherY + 36f);
        }

        // ── PLAIN row label + shifted letters ─────────────────────────
        smallFont.setColor(0.4f, 0.65f, 0.4f, 1f);
        smallFont.draw(batch, "PLAIN", WX + 12, stripPlainY + 36f);

        for (int i = 0; i < 26; i++) {
            float lx = stripX + 40f + i * lw;
            char  c  = (char)('A' + (i + 26 - shift) % 26);
            boolean hi = (i == shift);
            if (solved) {
                medFont.setColor(0f, 1f, 0.4f, 1f);
            } else {
                medFont.setColor(hi ? Color.YELLOW : new Color(0.65f, 0.88f, 0.55f, 1f));
            }
            medFont.draw(batch, String.valueOf(c), lx, stripPlainY + 36f);
        }

        // ── Shift counter ──────────────────────────────────────────────
        medFont.setColor(Color.YELLOW);
        medFont.draw(batch, "SHIFT:  " + shift, WX + 14, stripPlainY - 18f);

        smallFont.setColor(0.5f, 0.5f, 0.55f, 1f);
        smallFont.draw(batch,
            "[ A / LEFT ] rotate left    [ D / RIGHT ] rotate right    [ ENTER ] submit",
            WX + 180f, stripPlainY - 18f);

        // ── Encrypted message ──────────────────────────────────────────
        medFont.setColor(0.5f, 0.5f, 0.55f, 1f);
        medFont.draw(batch, "ENCRYPTED MESSAGE:", WX + 40, WY + 270f);

        bigFont.setColor(1f, 0.65f, 0.1f, 1f);
        bigFont.draw(batch, ENCRYPTED, WX + 40, WY + 240f);

        // ── Decoded output ─────────────────────────────────────────────
        medFont.setColor(0.5f, 0.5f, 0.55f, 1f);
        medFont.draw(batch, "DECODED:", WX + 40, WY + 192f);

        String decoded = applyShift(ENCRYPTED, shift);
        if (solved) {
            float fl = 0.55f + 0.45f * (float)Math.sin(stateTime * 10f);
            bigFont.setColor(0f, fl, fl * 0.55f, 1f);
        } else if (wrongFlash > 0) {
            bigFont.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            bigFont.setColor(0f, 1f, 0.4f, 1f);
        }
        bigFont.draw(batch, decoded, WX + 40, WY + 162f);

        // ── Status / hint ──────────────────────────────────────────────
        if (solved) {
            medFont.setColor(0f, 1f, 0.5f, 1f);
            layout.setText(medFont, "[OK] CIPHER BROKEN - KEY ACQUIRED (closing...)");
            medFont.draw(batch, "[OK] CIPHER BROKEN - KEY ACQUIRED (closing...)",
                WX + WW/2f - layout.width/2f, WY + 110f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.2f, 1f);
            layout.setText(medFont, "[X] WRONG SHIFT - TRY AGAIN");
            medFont.draw(batch, "[X] WRONG SHIFT - TRY AGAIN",
                WX + WW/2f - layout.width/2f, WY + 110f);
        } else {
            smallFont.setColor(0.3f, 0.45f, 0.3f, 1f);
            smallFont.draw(batch,
                "HINT: ROT-13 is the most famous Caesar cipher. Each letter shifts exactly halfway through the alphabet.",
                WX + 40, WY + 100f);
        }

        batch.end();
    }

    private String applyShift(String text, int s) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append((c >= 'A' && c <= 'Z') ? (char)(((c-'A'+s)%26)+'A') : c);
        }
        return sb.toString();
    }
}
