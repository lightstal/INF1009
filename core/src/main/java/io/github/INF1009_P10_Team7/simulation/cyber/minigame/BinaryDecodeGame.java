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

/**
 * BINARY DECODER
 *
 * Five 8-bit bytes spell "FLAGS". Students decode each byte to its ASCII
 * character and type it into the terminal prompt.
 *
 * FIX: The InputAdapter is set as the processor in open() and is NEVER
 * overwritten by the game scene (CyberGameScene.onUpdate only resets the
 * processor when no challenge is active). TAB/ESC now reliably close.
 */
public class BinaryDecodeGame implements IMiniGame {

    private static final float W = 1280f, H = 704f;
    private static final float WX = 140f, WY = 50f, WW = 1000f, WH = 600f;
    private static final float TITLE_H = 40f;

    private static final String[] BINARY  = { "01000110","01001100","01000001","01000111","01010011" };
    private static final char[]   ANSWERS = { 'F','L','A','G','S' };

    private boolean open, solved, panicked;
    private float   stateTime, wrongFlash, solveTimer;
    private int     currentByte;
    private char[]  decoded;

    private BitmapFont bigFont, medFont, smallFont;
    private GlyphLayout layout;

    private final StringBuilder inputBuf = new StringBuilder();

    private final InputAdapter adapter = new InputAdapter() {
        @Override
        public boolean keyDown(int k) {
            // TAB/ESC always close — checked first
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true; close(); return true;
            }
            if (!open || solved) return false;
            if (k == Input.Keys.BACKSPACE && inputBuf.length() > 0) {
                inputBuf.deleteCharAt(inputBuf.length()-1); return true;
            }
            if (k == Input.Keys.ENTER) {
                String typed = inputBuf.toString().trim().toUpperCase();
                inputBuf.setLength(0);
                if (typed.length() == 1 && typed.charAt(0) == ANSWERS[currentByte]) {
                    decoded[currentByte] = ANSWERS[currentByte];
                    currentByte++;
                    if (currentByte >= 5) { solved = true; solveTimer = 0f; }
                } else {
                    wrongFlash = 0.7f;
                }
                return true;
            }
            return false;
        }
        @Override
        public boolean keyTyped(char c) {
            if (!open || solved) return true;
            if (c >= 32 && c < 127 && inputBuf.length() < 3) inputBuf.append(c);
            return true;
        }
    };

    @Override
    public void open() {
        open = true; solved = false; panicked = false;
        stateTime = 0f; wrongFlash = 0f; solveTimer = 0f;
        currentByte = 0;
        decoded = new char[5];
        inputBuf.setLength(0);
        buildFonts();
        Gdx.input.setInputProcessor(adapter);
    }

    private void buildFonts() {
        disposeFonts();
        bigFont   = new BitmapFont(); bigFont.getData().setScale(1.4f);    bigFont.setUseIntegerPositions(true);
        medFont   = new BitmapFont(); medFont.getData().setScale(1.0f);    medFont.setUseIntegerPositions(true);
        smallFont = new BitmapFont(); smallFont.getData().setScale(0.78f); smallFont.setUseIntegerPositions(true);
        layout    = new GlyphLayout();
    }
    private void disposeFonts() {
        if (bigFont   != null) bigFont.dispose();
        if (medFont   != null) medFont.dispose();
        if (smallFont != null) smallFont.dispose();
        bigFont = medFont = smallFont = null;
    }

    @Override public void close()          { open = false; Gdx.input.setInputProcessor(null); }
    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String  getTitle()    { return "BINARY DECODER // INTERCEPTED SIGNAL"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) { solveTimer += dt; if (solveTimer > 2f) close(); }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignored) {
        if (!open || bigFont == null) return;

        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // ── Background ──────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.90f); sr.rect(0, 0, W, H);
        sr.setColor(0.02f, 0.03f, 0.04f, 1f); sr.rect(WX, WY, WW, WH);
        sr.setColor(0.03f, 0.10f, 0.03f, 1f); sr.rect(WX, WY+WH-TITLE_H, WW, TITLE_H);
        sr.setColor(0f, pulse*0.4f, 0f, 0.22f); sr.rect(WX-5, WY-5, WW+10, WH+10);

        // Signal display area (5 byte slots)
        float sigY = WY + WH - 200f, sigH = 140f;
        float slotW = (WW - 60f) / 5f;
        sr.setColor(0.01f, 0.03f, 0.01f, 1f); sr.rect(WX+30, sigY, WW-60, sigH);

        // Active slot highlight
        if (!solved) {
            sr.setColor(0f, 0.35f, 0f, 0.30f);
            sr.rect(WX + 30 + currentByte * slotW, sigY, slotW, sigH);
        }

        // Bit cells for current byte
        if (!solved) {
            String curBin = BINARY[currentByte];
            float bitW = (slotW - 10f) / 8f;
            float bitX = WX + 30 + currentByte * slotW + 5f;
            float bitY = sigY + 20f;
            for (int b = 0; b < 8; b++) {
                boolean isOne = curBin.charAt(b) == '1';
                sr.setColor(isOne ? 0f   : 0.04f,
                            isOne ? pulse*0.8f : 0.10f,
                            isOne ? 0f   : 0.02f, 1f);
                sr.rect(bitX + b*bitW, bitY, bitW-2, 30f);
            }
        }

        // Progress track boxes
        for (int i = 0; i < 5; i++) {
            boolean done = decoded[i] != 0;
            boolean cur  = i == currentByte && !solved;
            sr.setColor(done ? 0f : (cur ? 0f : 0.03f),
                        done ? 0.55f : (cur ? pulse*0.4f : 0.07f),
                        done ? 0.1f : 0f, 1f);
            sr.rect(WX + 140 + i * 140f, WY + 105f, 110f, 50f);
        }

        // Input box
        float inputY = WY + 165f;
        sr.setColor(0.02f, 0.04f, 0.02f, 1f);
        sr.rect(WX + 40, inputY, WW - 80, 38f);

        // Wrong flash
        if (wrongFlash > 0) { sr.setColor(0.6f, 0f, 0f, wrongFlash*0.35f); sr.rect(WX, WY, WW, WH); }
        sr.end();

        // ── Borders ─────────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0f, 0.75f, 0.35f, 0.9f); sr.rect(WX, WY, WW, WH);
        sr.line(WX, WY+WH-TITLE_H, WX+WW, WY+WH-TITLE_H);
        sr.setColor(0f, 0.65f, 0.3f, 0.55f); sr.rect(WX+30, sigY, WW-60, sigH);
        sr.setColor(wrongFlash > 0 ? 1f : 0f,
                    wrongFlash > 0 ? 0.1f : 0.5f,
                    wrongFlash > 0 ? 0.1f : 0.2f,
                    wrongFlash > 0 ? wrongFlash : 0.6f);
        sr.rect(WX+40, inputY, WW-80, 38f);
        sr.end();

        // ── Text ────────────────────────────────────────────────────────
        batch.begin();

        // Title
        medFont.setColor(0f, 0.9f, 0.45f, 1f);
        medFont.draw(batch, "  [ BINARY DECODER // INTERCEPTED SIGNAL ]    [TAB = CLOSE]",
            WX + 10, WY + WH - 13f);

        // All 5 binary strings in signal display
        for (int i = 0; i < 5; i++) {
            float bx = WX + 30 + 4f + i * slotW;
            boolean isCur = (i == currentByte && !solved);
            medFont.setColor(isCur ? Color.GREEN : new Color(0f, 0.45f, 0.22f, 1f));
            medFont.draw(batch, BINARY[i], bx + 2f, sigY + sigH - 12f);
            smallFont.setColor(0.3f, 0.3f, 0.3f, 1f);
            smallFont.draw(batch, "BYTE " + (i+1), bx + 12f, sigY + sigH - 32f);
        }

        // Large current-byte display
        if (!solved) {
            // Format "0100 0110" with space in middle
            String bin = BINARY[currentByte];
            String display = bin.substring(0,4) + "  " + bin.substring(4);
            bigFont.setColor(Color.GREEN);
            layout.setText(bigFont, display);
            bigFont.draw(batch, display, WX + WW/2f - layout.width/2f, WY + WH - 215f);

            medFont.setColor(Color.WHITE);
            String prompt = "Decode byte " + (currentByte+1) + " of 5 — type the ASCII character it represents:";
            layout.setText(medFont, prompt);
            medFont.draw(batch, prompt, WX + WW/2f - layout.width/2f, WY + 230f);
        }

        // Progress character boxes
        for (int i = 0; i < 5; i++) {
            if (decoded[i] != 0) {
                bigFont.setColor(0f, 1f, 0.4f, 1f);
                bigFont.draw(batch, String.valueOf(decoded[i]), WX + 175 + i*140f, WY + 145f);
            } else if (i == currentByte && !solved) {
                float fl = 0.4f + 0.4f*(float)Math.sin(stateTime*6f);
                medFont.setColor(fl, fl, fl, 1f);
                medFont.draw(batch, "_", WX + 175 + i*140f, WY + 140f);
            } else {
                medFont.setColor(0.18f, 0.18f, 0.18f, 1f);
                medFont.draw(batch, "_", WX + 175 + i*140f, WY + 140f);
            }
        }

        // Progress label
        smallFont.setColor(0.38f, 0.38f, 0.38f, 1f);
        smallFont.draw(batch, "DECODED PROGRESS:", WX + 100, WY + 104f);

        // Input prompt
        if (!solved) {
            medFont.setColor(0f, 0.8f, 0.35f, 1f);
            medFont.draw(batch, "$ decode>", WX + 50, inputY + 26f);
            medFont.setColor(Color.WHITE);
            String typed = inputBuf.toString();
            medFont.draw(batch, typed + (blink ? "█" : " "), WX + 170, inputY + 26f);

            smallFont.setColor(0.45f, 0.45f, 0.45f, 1f);
            smallFont.draw(batch, "Type the character (e.g.  F )  then press  ENTER",
                WX + 50, WY + 155f);
        }

        // Hint
        smallFont.setColor(0.28f, 0.42f, 0.28f, 1f);
        smallFont.draw(batch,
            "HINT:  Use an ASCII table to decode each 8-bit byte.  Try:  asciitable.com",
            WX + 50, WY + 70f);

        // Result
        if (solved) {
            float fl = 0.5f + 0.5f*(float)Math.sin(stateTime*8f);
            medFont.setColor(0f, fl, fl*0.5f, 1f);
            layout.setText(medFont, "✓  SIGNAL DECODED: FLAGS — KEY ACQUIRED  (closing...)");
            medFont.draw(batch, "✓  SIGNAL DECODED: FLAGS — KEY ACQUIRED  (closing...)",
                WX + WW/2f - layout.width/2f, WY + 50f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.2f, 1f);
            layout.setText(medFont, "✗  WRONG — CHECK YOUR ASCII TABLE AND TRY AGAIN");
            medFont.draw(batch, "✗  WRONG — CHECK YOUR ASCII TABLE AND TRY AGAIN",
                WX + WW/2f - layout.width/2f, WY + 50f);
        }

        batch.end();
    }
}
