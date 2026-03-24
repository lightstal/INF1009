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
 * CHALLENGE: PORT MAPPER
 *
 * Students must match 5 network ports to their corresponding services.
 * Left column: ports (numbered 1-5)
 * Right column: services in scrambled order (lettered A-E)
 *
 * The correct mappings are:
 *   22   -> SSH
 *   80   -> HTTP
 *   443  -> HTTPS
 *   3306 -> MySQL
 *   21   -> FTP
 *
 * Students type a number (1-5) to pick a port, then a letter (A-E) to match it
 * to a service.  Correct pairs turn green; wrong pairs flash red.
 * All 5 matched = solved.
 */
public class PortMatchGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    // Ports (displayed in order 1-5)
    private static final String[] PORTS = { "22", "80", "443", "3306", "21" };
    // Services  -  displayed in scrambled order (A-E)
    private static final String[] SERVICES = { "MySQL", "FTP", "SSH", "HTTPS", "HTTP" };
    // Correct answer: ports[i] matches services[CORRECT_SERVICE_IDX[i]]
    // Port 22=SSH -> index 2(C), 80=HTTP -> index 4(E), 443=HTTPS -> index 3(D),
    // 3306=MySQL -> index 0(A), 21=FTP -> index 1(B)
    private static final int[] CORRECT = { 2, 4, 3, 0, 1 };  // CORRECT[portIdx] = serviceIdx

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime  = 0f;
    // Pre-scaled fonts  -  built once in open(), never rescaled in render()
    private BitmapFont bigFont, medFont, smallFont;
    private GlyphLayout glLayout;


    private float   wrongFlash = 0f;
    private float   solveTimer = 0f;

    // -1 = unmatched; otherwise = matched service index
    private int[]   portMatched;
    // tracks which services are already taken
    private boolean[] serviceTaken;

    // Input state machine
    private int selectedPort = -1;   // 0-4, -1 = none selected yet
    private String wrongMsg  = "";

    private final InputAdapter adapter = new PortInputAdapter();

    // -------------------------------------------------------------------------

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
        buildFonts();
        wrongFlash = 0f;
        solveTimer = 0f;
        selectedPort = -1;
        portMatched  = new int[]{ -1, -1, -1, -1, -1 };
        serviceTaken = new boolean[5];
        wrongMsg = "";
        Gdx.input.setInputProcessor(adapter);
    }

    @Override
    public void close() {
        open = false;
        disposeFonts();
        selectedPort = -1;
        wrongMsg = "";
        Gdx.input.setInputProcessor(null);
    }

    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String getTitle()     { return "NETWORK CONFIG // PORT MAPPER"; }

    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open) return;

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 3f);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // --- Overlay ---
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.90f);
        sr.rect(0, 0, W, H);

        float wx = 160f, wy = 80f, ww = 960f, wh = 544f;
        float titleH = 40f;

        sr.setColor(0.03f, 0.03f, 0.06f, 1f);
        sr.rect(wx, wy, ww, wh);
        sr.setColor(0.10f, 0.04f, 0.0f, 1f);
        sr.rect(wx, wy + wh - titleH, ww, titleH);
        sr.setColor(pulse * 0.3f, 0.1f, 0f, 0.25f);
        sr.rect(wx - 5, wy - 5, ww + 10, wh + 10);

        // Port column bg
        sr.setColor(0.02f, 0.02f, 0.04f, 1f);
        sr.rect(wx + 40, wy + 140, 300, 300);
        // Service column bg
        sr.rect(wx + 620, wy + 140, 300, 300);

        // Row highlights
        float rowH  = 56f;
        float rowY0 = wy + 140 + 240;  // top row bottom
        for (int i = 0; i < 5; i++) {
            float ry = rowY0 - i * rowH;
            // Port row
            boolean pSel   = selectedPort == i;
            boolean pDone  = portMatched[i] >= 0;
            sr.setColor(pDone ? 0f : (pSel ? 0f : 0.03f),
                        pDone ? 0.25f : (pSel ? pulse * 0.35f : 0.04f),
                        pDone ? 0.05f : 0f, 1f);
            sr.rect(wx + 42, ry + 4, 296, rowH - 6);

            // Service row
            boolean sDone = serviceTaken[i];
            sr.setColor(sDone ? 0f : 0.03f,
                        sDone ? 0.25f : 0.04f,
                        sDone ? 0.05f : 0f, 1f);
            sr.rect(wx + 622, ry + 4, 296, rowH - 6);
        }

        // Connector lines for matched pairs
        for (int pi = 0; pi < 5; pi++) {
            if (portMatched[pi] < 0) continue;
            int si = portMatched[pi];
            float py = rowY0 - pi * rowH + rowH / 2f;
            float sy = rowY0 - si * rowH + rowH / 2f;
            sr.setColor(0f, 0.7f, 0.3f, 0.5f);
            // Thick connector: draw 4 horizontal lines
            for (int t = -1; t <= 1; t++) {
                sr.rectLine(wx + 342, py + t, wx + 618, sy + t, 1.5f);
            }
        }

        // Wrong flash
        if (wrongFlash > 0) {
            sr.setColor(0.7f, 0f, 0f, wrongFlash * 0.35f);
            sr.rect(wx, wy, ww, wh);
        }
        sr.end();

        // Borders
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(pulse, 0.3f, 0f, 0.9f);
        sr.rect(wx, wy, ww, wh);
        sr.line(wx, wy + wh - titleH, wx + ww, wy + wh - titleH);
        sr.setColor(0.5f, 0.2f, 0f, 0.5f);
        sr.rect(wx + 40, wy + 140, 300, 300);
        sr.rect(wx + 620, wy + 140, 300, 300);
        if (wrongFlash > 0) { sr.setColor(1f, 0.2f, 0.2f, wrongFlash); sr.rect(wx, wy, ww, wh); }
        sr.end();

        // --- Text ---
        batch.begin();
        bigFont.setColor(1f, 0.6f, 0.1f, 1f);
        medFont.draw(batch, "  [ NETWORK CONFIG // PORT MAPPER ]    [ESC/TAB=CLOSE]",
            wx + 10, wy + wh - 13f);

        // Column headers
        medFont.setColor(new Color(0.7f, 0.4f, 0f, 1f));
        medFont.draw(batch, "PORTS", wx + 155, wy + 445f);
        medFont.draw(batch, "SERVICES", wx + 718, wy + 445f);

        // Instructions — between title bar and columns
        smallFont.setColor(0.45f, 0.45f, 0.45f, 1f);
        smallFont.draw(batch, "Instructions:  Press a port number ( 1  -  5 ) to select it,",
            wx + 55, wy + wh - 48f);
        smallFont.draw(batch, "then press a service letter ( A  -  E ) to make the match.",
            wx + 55, wy + wh - 64f);

        // Port and service rows
        float rowY0f = wy + 140 + 240;
        for (int i = 0; i < 5; i++) {
            float ry = rowY0f - i * rowH + rowH / 2f + 8f;

            // Port number badge
            medFont.setColor(0.5f, 0.5f, 0.55f, 1f);
            medFont.draw(batch, "[" + (i+1) + "]", wx + 50, ry);

            // Port value
            boolean pDone = portMatched[i] >= 0;
            boolean pSel  = selectedPort == i;
            medFont.setColor(pDone ? Color.GREEN : (pSel ? Color.YELLOW : Color.WHITE));
            medFont.draw(batch, PORTS[i], wx + 90, ry);

            // Matched service annotation on port side
            if (pDone) {
                smallFont.setColor(0f, 0.7f, 0.3f, 0.8f);
                medFont.draw(batch, "-> " + SERVICES[portMatched[i]] + " [OK]", wx + 170, ry);
            }

            // Service letter badge
            medFont.setColor(0.5f, 0.5f, 0.55f, 1f);
            char letter = (char)('A' + i);
            medFont.draw(batch, "[" + letter + "]", wx + 630, ry);

            // Service name
            boolean sDone = serviceTaken[i];
            medFont.setColor(sDone ? Color.GREEN : Color.WHITE);
            medFont.draw(batch, SERVICES[i], wx + 668, ry);
        }

        // Status / selection prompt
        if (selectedPort < 0) {
            medFont.setColor(Color.WHITE);
            medFont.draw(batch, "Press a port number:  1  2  3  4  5", wx + 280, wy + 115f);
        } else {
            medFont.setColor(Color.YELLOW);
            medFont.draw(batch, "Port " + PORTS[selectedPort] + " selected  -  now press a service letter:  A  B  C  D  E",
                wx + 130, wy + 115f);
        }

        // Wrong message
        if (solved) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            medFont.setColor(0f, fl, fl * 0.4f, 1f);
            medFont.draw(batch, "[OK] ALL PORTS MAPPED - KEY ACQUIRED (closing...)", wx + 180, wy + 48f);
        } else if (wrongFlash > 0) {
            medFont.setColor(1f, 0.2f, 0.2f, 1f);
            medFont.draw(batch, "[X] " + wrongMsg, wx + 280, wy + 48f);
        }

        batch.end();
    }

    // -------------------------------------------------------------------------
    private class PortInputAdapter extends InputAdapter {

        @Override
        public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) {
                panicked = true; close(); return true;
            }
            if (solved) return false;

            // Select a port (keys 1-5)
            if (k >= Input.Keys.NUM_1 && k <= Input.Keys.NUM_5) {
                int idx = k - Input.Keys.NUM_1;
                if (portMatched[idx] >= 0) {
                    wrongMsg = "PORT " + PORTS[idx] + " IS ALREADY MATCHED";
                    wrongFlash = 0.7f;
                } else {
                    selectedPort = idx;
                }
                return true;
            }

            // Select a service (keys A-E)
            if (selectedPort >= 0) {
                int svcIdx = -1;
                if (k == Input.Keys.A) svcIdx = 0;
                if (k == Input.Keys.B) svcIdx = 1;
                if (k == Input.Keys.C) svcIdx = 2;
                if (k == Input.Keys.D) svcIdx = 3;
                if (k == Input.Keys.E) svcIdx = 4;

                if (svcIdx >= 0) {
                    if (serviceTaken[svcIdx]) {
                        wrongMsg = "SERVICE " + SERVICES[svcIdx] + " IS ALREADY TAKEN";
                        wrongFlash = 0.7f;
                        selectedPort = -1;
                        return true;
                    }
                    // Check correctness
                    if (CORRECT[selectedPort] == svcIdx) {
                        portMatched[selectedPort] = svcIdx;
                        serviceTaken[svcIdx]      = true;
                        selectedPort              = -1;
                        wrongFlash = 0f;
                        // Check all matched
                        boolean allDone = true;
                        for (int m : portMatched) if (m < 0) { allDone = false; break; }
                        if (allDone) { solved = true; solveTimer = 0f; }
                    } else {
                        wrongMsg   = "WRONG MATCH  -  TRY AGAIN";
                        wrongFlash = 0.8f;
                        selectedPort = -1;
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
