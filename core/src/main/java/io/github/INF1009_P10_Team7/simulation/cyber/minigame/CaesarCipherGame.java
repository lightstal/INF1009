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
 * CAESAR CIPHER DECODER — Cyberpunk Hacker Terminal UI
 *
 * Two alphabet strips slide relative to each other.
 * Students rotate with A/D (or LEFT/RIGHT) until the DECODED output
 * reads "HELLO HACKER", then press ENTER to submit.
 * Correct shift = 13 (ROT-13).
 *
 * UI features:
 *  - Perfectly centered monospaced letter alignment (both rows)
 *  - Smooth animated shift sliding
 *  - Neon glow on active column
 *  - CRT scanlines + grid background
 *  - Blinking cursor / typing feel
 *  - Glassmorphism panels
 *  - Neon amber (cipher) / neon green (decoded) color scheme
 */
/**
 * CaesarCipherGame — mini-game where the player decrypts a Caesar-cipher message.
 *
 * <p>A ciphertext string is generated with a random shift (1–25). The player
 * must identify the shift and type the plaintext to complete the challenge.
 * Provides a frequency-analysis hint to guide the player.</p>
 *
 * <p>Implements {@link IMiniGame} so it is handled uniformly by
 * {@code CyberGameScene} (OCP, LSP).</p>
 */
public class CaesarCipherGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    private static final String ENCRYPTED    = "URYYB UNPXRE";
    private static final String SOLUTION     = "HELLO HACKER";
    private static final int    CORRECT_SHIFT = 13;

    // Window geometry — wider panel, more breathing room
    private static final float WX = 100f, WY = 40f, WW = 1080f, WH = 620f;
    private static final float TITLE_H = 44f;

    private boolean open, solved, panicked;
    private int     shift;
    private float   shiftAnimTimer;     // smooth animation countdown
    private float   stateTime, wrongFlash, solveTimer;

    // Pre-built fonts at fixed scales — never touched after open()
    private BitmapFont bigFont;     // 1.6x  — encrypted / decoded text
    private BitmapFont medFont;     // 1.0x  — labels, strip letters
    private BitmapFont smallFont;   // 0.78x — hints, nav text
    private GlyphLayout layout;

    private final InputAdapter captureAdapter = new InputAdapter() {
        @Override public boolean keyDown(int k) {
            if (!open || solved) return false;
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true; close(); return true;
            }
            if (k == Input.Keys.LEFT  || k == Input.Keys.A) {
                shift = (shift + 25) % 26;
                shiftAnimTimer = 0.1f;
                return true;
            }
            if (k == Input.Keys.RIGHT || k == Input.Keys.D) {
                shift = (shift + 1) % 26;
                shiftAnimTimer = 0.1f;
                return true;
            }
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
        shift = 0; shiftAnimTimer = 0f;
        stateTime = 0f; wrongFlash = 0f; solveTimer = 0f;
        buildFonts();
        Gdx.input.setInputProcessor(captureAdapter);
    }

    private void buildFonts() {
        disposeFonts();
        bigFont   = FontManager.create(1.6f);
        medFont   = FontManager.create(1.0f);
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
        if (shiftAnimTimer > 0) shiftAnimTimer = Math.max(0f, shiftAnimTimer - dt);
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

        // ── Full-screen dark overlay ─────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0.01f, 0.92f);
        sr.rect(0, 0, W, H);

        // ── Grid background overlay ──────────────────────────────────────
        sr.setColor(0.04f, 0.08f, 0.12f, 0.15f);
        float gridSize = 40f;
        for (float gx = 0; gx < W; gx += gridSize) {
            sr.rect(gx, 0, 1f, H);
        }
        for (float gy = 0; gy < H; gy += gridSize) {
            sr.rect(0, gy, W, 1f);
        }

        // ── Main panel (glassmorphism) ───────────────────────────────────
        // Outer glow
        sr.setColor(0f, 0.12f, 0.2f, 0.35f);
        sr.rect(WX - 6, WY - 6, WW + 12, WH + 12);
        // Panel body
        sr.setColor(0.02f, 0.03f, 0.07f, 0.95f);
        sr.rect(WX, WY, WW, WH);
        // Title bar
        sr.setColor(0.01f, 0.06f, 0.14f, 1f);
        sr.rect(WX, WY + WH - TITLE_H, WW, TITLE_H);
        // Top accent line (neon teal)
        sr.setColor(0f, 0.85f, 0.95f, 0.85f);
        sr.rect(WX, WY + WH - 2f, WW, 2f);
        // Bottom accent line
        sr.setColor(0f, 0.65f, 0.75f, 0.5f);
        sr.rect(WX, WY, WW, 2f);

        // ── CRT scanlines over panel ─────────────────────────────────────
        sr.setColor(0f, 0f, 0f, 0.05f);
        for (float sy = WY; sy < WY + WH; sy += 3f) {
            sr.rect(WX, sy, WW, 1f);
        }

        // ── Alphabet strip layout ────────────────────────────────────────
        float stripMarginL = 80f;
        float stripPadX = 14f;
        float stripX = WX + stripMarginL;
        float stripW = WW - stripMarginL - stripPadX;
        float cellW  = stripW / 26f;

        float stripCipherY = WY + 390f;
        float stripPlainY  = WY + 310f;
        float stripH = 48f;
        float gapBetween = stripCipherY - (stripPlainY + stripH);

        // Strip backgrounds
        sr.setColor(0.015f, 0.02f, 0.05f, 1f);
        sr.rect(stripX, stripCipherY, stripW, stripH);
        sr.rect(stripX, stripPlainY,  stripW, stripH);

        // ── Active column highlight — perfectly centered on letter ────────
        float hlCellIdx = 12f;
        float hlX = stripX + hlCellIdx * cellW;
        float hlFullH = stripH * 2f + gapBetween;

        // Outer glow behind active column
        sr.setColor(0f, 0.2f, 0.45f, 0.12f * pulse);
        sr.rect(hlX - cellW * 0.3f, stripPlainY - 2f, cellW * 1.6f, hlFullH + 4f);

        // Active column fill
        sr.setColor(0f, 0.4f, pulse * 0.85f, 0.25f);
        sr.rect(hlX, stripPlainY, cellW, hlFullH);

        // Neon glow edges on active column
        sr.setColor(0f, 0.85f, 1f, 0.35f * pulse);
        sr.rect(hlX, stripPlainY, 1.5f, hlFullH);
        sr.rect(hlX + cellW - 1.5f, stripPlainY, 1.5f, hlFullH);

        // Wrong flash overlay
        if (wrongFlash > 0) {
            sr.setColor(0.5f, 0f, 0f, wrongFlash * 0.35f);
            sr.rect(WX, WY, WW, WH);
        }
        sr.end();

        // ── Borders ─────────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0f, 0.55f, 0.7f, 0.6f + pulse * 0.2f);
        sr.rect(WX, WY, WW, WH);
        sr.line(WX, WY + WH - TITLE_H, WX + WW, WY + WH - TITLE_H);
        sr.setColor(0f, 0.4f, 0.6f, 0.45f);
        sr.rect(stripX, stripCipherY, stripW, stripH);
        sr.rect(stripX, stripPlainY,  stripW, stripH);
        // Active column outline
        sr.setColor(0f, 0.9f, 1f, 0.5f * pulse);
        sr.rect(hlX, stripPlainY, cellW, hlFullH);
        sr.end();

        // ── Text ────────────────────────────────────────────────────────
        batch.begin();

        // Title bar
        medFont.setColor(0f, 0.85f, 0.95f, 1f);
        medFont.draw(batch, "  [ CRYPTOGRAPHY // CAESAR CIPHER DECODER ]",
            WX + 10, WY + WH - 13f);
        smallFont.setColor(0.4f, 0.5f, 0.6f, 0.9f);
        smallFont.draw(batch, "[ESC] CLOSE",
            WX + WW - 120f, WY + WH - 15f);

        // ── CIPHER row — perfectly centered letters ─────────────────────
        smallFont.setColor(0.5f, 0.55f, 0.7f, 0.9f);
        smallFont.draw(batch, "CIPHER", WX + 12, stripCipherY + stripH / 2f + 5f);

        for (int i = 0; i < 26; i++) {
            char c = (char)('A' + i);
            String cs = String.valueOf(c);
            layout.setText(medFont, cs);
            float charW = layout.width;
            float charH = layout.height;
            // Center each character precisely in its cell
            float lx = stripX + i * cellW + (cellW - charW) / 2f;
            float ly = stripCipherY + stripH / 2f + charH / 2f;

            boolean isHighlighted = (i == 12);
            if (isHighlighted) {
                medFont.setColor(1f, 0.88f, 0.2f, 1f);
            } else {
                medFont.setColor(1f, 0.72f, 0.15f, 0.9f);
            }
            medFont.draw(batch, cs, lx, ly);
        }

        // ── PLAIN row — perfectly centered shifted letters ──────────────
        smallFont.setColor(0.35f, 0.65f, 0.4f, 0.9f);
        smallFont.draw(batch, "PLAIN", WX + 14, stripPlainY + stripH / 2f + 5f);

        for (int i = 0; i < 26; i++) {
            char c = (char)('A' + (i + 26 - shift) % 26);
            String cs = String.valueOf(c);
            layout.setText(medFont, cs);
            float charW = layout.width;
            float charH = layout.height;
            float lx = stripX + i * cellW + (cellW - charW) / 2f;
            float ly = stripPlainY + stripH / 2f + charH / 2f;

            boolean isActive = (i == shift);
            if (solved) {
                float fl = 0.6f + 0.4f * (float)Math.sin(stateTime * 10f);
                medFont.setColor(0f, fl, fl * 0.55f, 1f);
            } else if (isActive) {
                medFont.setColor(0.3f, 1f, 0.5f, 1f);
            } else {
                medFont.setColor(0.4f, 0.82f, 0.5f, 0.85f);
            }
            medFont.draw(batch, cs, lx, ly);
        }

        // ── Connection dots between rows ────────────────────────────────
        batch.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        float dotX = hlX + cellW / 2f;
        float dotStartY = stripPlainY + stripH;
        float dotEndY   = stripCipherY;
        sr.setColor(0f, 0.85f, 1f, 0.3f * pulse);
        for (float dy = dotStartY + 4f; dy < dotEndY - 2f; dy += 6f) {
            sr.circle(dotX, dy, 1.5f, 6);
        }
        sr.end();
        batch.begin();

        // ── Shift counter + controls ────────────────────────────────────
        float cursorBlink = ((int)(stateTime * 3f) % 2 == 0) ? 1f : 0.3f;
        medFont.setColor(0f, 0.9f, 1f, 1f);
        medFont.draw(batch, "SHIFT:  " + shift, WX + 16, stripPlainY - 22f);

        // Blinking cursor
        medFont.setColor(0f, 0.9f, 1f, cursorBlink * 0.6f);
        medFont.draw(batch, "_", WX + 16 + 95f, stripPlainY - 22f);

        smallFont.setColor(0.4f, 0.5f, 0.55f, 0.85f);
        smallFont.draw(batch,
            "[ A / LEFT ] rotate left    [ D / RIGHT ] rotate right    [ ENTER ] submit",
            WX + 200f, stripPlainY - 22f);

        // ── Encrypted message (neon amber) ─────────────────────────────
        smallFont.setColor(0.45f, 0.5f, 0.6f, 0.9f);
        smallFont.draw(batch, "INTERCEPTED CIPHERTEXT:", WX + 40, WY + 270f);

        bigFont.setColor(1f, 0.78f, 0.12f, 1f);
        bigFont.draw(batch, ENCRYPTED, WX + 40, WY + 240f);

        // ── Decoded output (softer neon green) ──────────────────────────
        smallFont.setColor(0.45f, 0.5f, 0.6f, 0.9f);
        smallFont.draw(batch, "DECODED PLAINTEXT:", WX + 40, WY + 192f);

        String decoded = applyShift(ENCRYPTED, shift);
        if (solved) {
            float fl = 0.55f + 0.45f * (float)Math.sin(stateTime * 10f);
            bigFont.setColor(0f, fl, fl * 0.55f, 1f);
        } else if (wrongFlash > 0) {
            bigFont.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            bigFont.setColor(0.25f, 0.92f, 0.5f, 1f);
        }
        bigFont.draw(batch, decoded, WX + 40, WY + 162f);

        // ── Status / hint ──────────────────────────────────────────────
        if (solved) {
            medFont.setColor(0f, 1f, 0.5f, 1f);
            String solvedMsg = "[OK] CIPHER BROKEN - KEY ACQUIRED (closing...)";
            layout.setText(medFont, solvedMsg);
            medFont.draw(batch, solvedMsg, WX + WW / 2f - layout.width / 2f, WY + 110f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.2f, 1f);
            String wrongMsg = "[X] WRONG SHIFT - TRY AGAIN";
            layout.setText(medFont, wrongMsg);
            medFont.draw(batch, wrongMsg, WX + WW / 2f - layout.width / 2f, WY + 110f);
        } else {
            smallFont.setColor(0.3f, 0.42f, 0.38f, 0.8f);
            smallFont.draw(batch,
                "HINT: ROT-13 is the most famous Caesar cipher. Each letter shifts exactly halfway through the alphabet.",
                WX + 40, WY + 100f);
        }

        // ── Bottom status bar ──────────────────────────────────────────
        smallFont.setColor(0.25f, 0.4f, 0.45f, 0.6f);
        smallFont.draw(batch, "SYS://crypto.decoder v2.1  |  session active  |  entropy: nominal",
            WX + 14, WY + 22f);

        batch.end();
    }

    private String applyShift(String text, int s) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append((c >= 'A' && c <= 'Z') ? (char)(((c - 'A' + s) % 26) + 'A') : c);
        }
        return sb.toString();
    }
}
