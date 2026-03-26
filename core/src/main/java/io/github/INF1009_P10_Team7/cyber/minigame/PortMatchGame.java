package io.github.INF1009_P10_Team7.cyber.minigame;

import io.github.INF1009_P10_Team7.engine.inputoutput.KeyCode;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;
import io.github.INF1009_P10_Team7.engine.render.ITextDraw;
import io.github.INF1009_P10_Team7.engine.render.MiniGameRenderContext;

/**
 * CHALLENGE: PORT MAPPER
 *
 * Students must match 5 network ports to their corresponding services.
 * Left column : ports  (numbered 1-5)
 * Right column: services in scrambled order (lettered A-E)
 *
 * INPUT: Press 1-5 to select a port, then A-E to match it to a service.
 * Correct pairs turn green; wrong pairs flash red. All 5 matched = solved.
 *
 * To change the port/service data per level, pass different arrays into the
 * constructor from LevelConfig.  The game logic never needs to change.
 *
 * Level 1 (easy)  — common ports: 22/SSH, 80/HTTP, 443/HTTPS, 3306/MySQL, 21/FTP
 * Level 2 (hard)  — obscure ports: 25/SMTP, 110/POP3, 143/IMAP, 3389/RDP, 5432/PostgreSQL
 */
/**
 * PortMatchGame — mini-game where the player matches network port numbers to
 * their corresponding services.
 *
 * <p>The ports, service names, and correct answer indices are all configurable
 * at construction time. Level 1 uses well-known ports (22, 80, 443 …);
 * Level 2 uses less common protocols (SMTP, POP3, IMAP …).</p>
 *
 * <p>Implements {@link IMiniGame} (OCP, LSP). Receives input directly from 
 * the Scene via inherited listener methods.</p>
 */
public class PortMatchGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    // ── Port / service data injected via constructor ───────────────────────────
    private final String[] ports;
    private final String[] services;
    private final int[]    correct;
    // ─────────────────────────────────────────────────────────────────────────

    private boolean open     = false;
    private boolean solved   = false;
    private boolean panicked = false;
    private float   stateTime  = 0f;

    private float   wrongFlash = 0f;
    private float   solveTimer = 0f;

    private int[]     portMatched;
    private boolean[] serviceTaken;

    private int    selectedPort = -1;   // 0-4, -1 = none selected yet
    private String wrongMsg     = "";

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * @param ports    Port number strings shown on the left, one per row.
     * @param services Service names shown on the right (scrambled), one per letter A-E.
     * @param correct  correct[i] = index into services[] that ports[i] maps to.
     */
    public PortMatchGame(String[] ports, String[] services, int[] correct) {
        this.ports    = ports;
        this.services = services;
        this.correct  = correct;
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
        selectedPort = -1;
        portMatched  = new int[]{ -1, -1, -1, -1, -1 };
        serviceTaken = new boolean[5];
        wrongMsg     = "";
    }

    @Override
    public void close() {
        open         = false;
        selectedPort = -1;
        wrongMsg     = "";
    }

    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String  getTitle()    { return "NETWORK CONFIG // PORT MAPPER"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime  += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) {
            solveTimer += dt;
            if (solveTimer > 2f) close();
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(MiniGameRenderContext context) {
        if (!open) return;
        IShapeDraw sr = context.shape();
        ITextDraw title = context.titleText();
        ITextDraw med = context.text();
        ITextDraw small = context.smallText();

        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 3f);

        // --- Overlay ---
        sr.beginFilled();
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

        // Column backgrounds
        sr.setColor(0.02f, 0.02f, 0.04f, 1f);
        sr.rect(wx + 40,  wy + 140, 300, 300);
        sr.rect(wx + 620, wy + 140, 300, 300);

        // Row highlights
        float rowH  = 56f;
        float rowY0 = wy + 140 + 240;   
        for (int i = 0; i < 5; i++) {
            float ry = rowY0 - i * rowH;

            // Port row
            boolean pSel  = selectedPort == i;
            boolean pDone = portMatched[i] >= 0;
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

        // --- Borders ---
        sr.beginLine();
        sr.setColor(pulse, 0.3f, 0f, 0.9f);
        sr.rect(wx, wy, ww, wh);
        sr.line(wx, wy + wh - titleH, wx + ww, wy + wh - titleH);
        sr.setColor(0.5f, 0.2f, 0f, 0.5f);
        sr.rect(wx + 40,  wy + 140, 300, 300);
        sr.rect(wx + 620, wy + 140, 300, 300);
        if (wrongFlash > 0) { sr.setColor(1f, 0.2f, 0.2f, wrongFlash); sr.rect(wx, wy, ww, wh); }
        sr.end();

        // --- Text ---
        med.begin();
        title.setColor(1f, 0.6f, 0.1f, 1f);
        title.draw("  [ NETWORK CONFIG // PORT MAPPER ]    [ESC/TAB=CLOSE]",
            wx + 10, wy + wh - 13f);

        // Column headers
        med.setColor(0.7f, 0.4f, 0f, 1f);
        med.draw("PORTS",    wx + 155, wy + 445f);
        med.draw("SERVICES", wx + 718, wy + 445f);

        // Instructions
        small.setColor(0.45f, 0.45f, 0.45f, 1f);
        small.draw("Instructions:  Press a port number ( 1  -  5 ) to select it,",
            wx + 55, wy + wh - 48f);
        small.draw("then press a service letter ( A  -  E ) to make the match.",
            wx + 55, wy + wh - 64f);

        // Port and service rows
        float rowY0f = wy + 140 + 240;
        for (int i = 0; i < 5; i++) {
            float ry = rowY0f - i * rowH + rowH / 2f + 8f;

            med.setColor(0.5f, 0.5f, 0.55f, 1f);
            med.draw("[" + (i + 1) + "]", wx + 50, ry);

            boolean pDone = portMatched[i] >= 0;
            boolean pSel  = selectedPort == i;
            if (pDone) med.setColor(0f, 1f, 0f, 1f);
            else if (pSel) med.setColor(1f, 1f, 0f, 1f);
            else med.setColor(1f, 1f, 1f, 1f);
            med.draw(ports[i], wx + 90, ry);

            if (pDone) {
                small.setColor(0f, 0.7f, 0.3f, 0.8f);
                med.draw("-> " + services[portMatched[i]] + " [OK]", wx + 170, ry);
            }

            med.setColor(0.5f, 0.5f, 0.55f, 1f);
            char letter = (char)('A' + i);
            med.draw("[" + letter + "]", wx + 630, ry);

            boolean sDone = serviceTaken[i];
            if (sDone) med.setColor(0f, 1f, 0f, 1f);
            else med.setColor(1f, 1f, 1f, 1f);
            med.draw(services[i], wx + 668, ry);
        }

        // Status / selection prompt
        if (selectedPort < 0) {
            med.setColor(1f, 1f, 1f, 1f);
            med.draw("Press a port number:  1  2  3  4  5", wx + 280, wy + 115f);
        } else {
            med.setColor(1f, 1f, 0f, 1f);
            med.draw(
                "Port " + ports[selectedPort] + " selected  -  now press a service letter:  A  B  C  D  E",
                wx + 130, wy + 115f);
        }

        // Result / error messages
        if (solved) {
            float fl = 0.5f + 0.5f * (float) Math.sin(stateTime * 8f);
            med.setColor(0f, fl, fl * 0.4f, 1f);
            med.draw("[OK] ALL PORTS MAPPED - KEY ACQUIRED (closing...)", wx + 180, wy + 48f);
        } else if (wrongFlash > 0) {
            med.setColor(1f, 0.2f, 0.2f, 1f);
            med.draw("[X] " + wrongMsg, wx + 280, wy + 48f);
        }

        med.end();
    }

    // ── Input Handling (Inherited from ITextInputListener) ────────────────────

    @Override
    public void onCharTyped(char c) {
        // Not used, parsing keys directly via onControlKeyPressed for matching 
    }

    @Override
    public void onControlKeyPressed(int k) {
        if (!open || solved) return;

        if (k == KeyCode.TAB || k == KeyCode.ESCAPE) {
            panicked = true; 
            close(); 
            return;
        }

        // Select a port (keys 1-5)
        if (k >= KeyCode.NUM_1 && k <= KeyCode.NUM_5) {
            int idx = k - KeyCode.NUM_1;
            if (portMatched[idx] >= 0) {
                wrongMsg   = "PORT " + ports[idx] + " IS ALREADY MATCHED";
                wrongFlash = 0.7f;
            } else {
                selectedPort = idx;
            }
            return;
        }

        // Select a service (keys A-E) — only active once a port is selected
        if (selectedPort >= 0) {
            int svcIdx = -1;
            if (k == KeyCode.A) svcIdx = 0;
            else if (k == KeyCode.B) svcIdx = 1;
            else if (k == KeyCode.C) svcIdx = 2;
            else if (k == KeyCode.D) svcIdx = 3;
            else if (k == KeyCode.E) svcIdx = 4;

            if (svcIdx >= 0) {
                if (serviceTaken[svcIdx]) {
                    wrongMsg     = "SERVICE " + services[svcIdx] + " IS ALREADY TAKEN";
                    wrongFlash   = 0.7f;
                    selectedPort = -1;
                    return;
                }
                if (correct[selectedPort] == svcIdx) {
                    portMatched[selectedPort] = svcIdx;
                    serviceTaken[svcIdx]      = true;
                    selectedPort              = -1;
                    wrongFlash = 0f;
                    boolean allDone = true;
                    for (int m : portMatched) if (m < 0) { allDone = false; break; }
                    if (allDone) { solved = true; solveTimer = 0f; }
                } else {
                    wrongMsg     = "WRONG MATCH  -  TRY AGAIN";
                    wrongFlash   = 0.8f;
                    selectedPort = -1;
                }
            }
        }
    }
}