package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Random;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * CHALLENGE: PACKET SNIFFER
 *
 * A hex dump is displayed. The player must identify the protocol
 * (HTTP, DNS, SSH, FTP, SMTP) from recognisable byte patterns and
 * ASCII-decoded hints in the dump.
 *
 * Implements IMiniGame (Strategy pattern).
 */
/**
 * PacketSnifferGame — mini-game where the player identifies malicious network
 * packets from a simulated packet capture.
 *
 * <p>The player is shown a stream of fake network packets and must select those
 * that match a given suspicious pattern (e.g. unauthorised port, unusual
 * payload, suspicious IP range). Implements {@link IMiniGame} (OCP, LSP).</p>
 */
public class PacketSnifferGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    // ── Packet data sets ────────────────────────────────────────────────────
    private static final String[][] PACKETS = {
        // [0] answer, [1] hint line, [2..] hex rows
        {
            "HTTP",
            "Hint: Look at the ASCII decode on the right  -  a familiar request method.",
            "0000  47 45 54 20 2f 69 6e 64  65 78 2e 68 74 6d 6c 20  |GET /index.html |",
            "0010  48 54 54 50 2f 31 2e 31  0d 0a 48 6f 73 74 3a 20  |HTTP/1.1..Host: |",
            "0020  77 77 77 2e 65 78 61 6d  70 6c 65 2e 63 6f 6d 0d  |www.example.com.|",
            "0030  0a 55 73 65 72 2d 41 67  65 6e 74 3a 20 4d 6f 7a  |.User-Agent: Moz|",
            "0040  69 6c 6c 61 2f 35 2e 30  0d 0a 41 63 63 65 70 74  |illa/5.0..Accept|",
        },
        {
            "DNS",
            "Hint: Port 53 traffic. The query section contains a domain name.",
            "0000  aa bb 01 00 00 01 00 00  00 00 00 00 07 65 78 61  |.............exa|",
            "0010  6d 70 6c 65 03 63 6f 6d  00 00 01 00 01 00 00 00  |mple.com........|",
            "0020  00 00 04 5d b8 d8 22 00  01 00 01 c0 0c 00 01 00  |...]..\".........|",
            "0030  01 00 00 0e 10 00 04 5d  b8 d8 22 c0 0c 00 01 00  |.......]..\".....|",
            "0040  01 00 00 0e 10 00 04 5d  b8 d8 23 00 00 00 00 00  |.......]..#.....|",
        },
        {
            "SSH",
            "Hint: The banner string at the start reveals the protocol version.",
            "0000  53 53 48 2d 32 2e 30 2d  4f 70 65 6e 53 53 48 5f  |SSH-2.0-OpenSSH_|",
            "0010  38 2e 39 70 31 20 55 62  75 6e 74 75 2d 33 75 62  |8.9p1 Ubuntu-3ub|",
            "0020  75 6e 74 75 30 2e 31 0d  0a 00 00 05 14 0a 14 22  |untu0.1........\"|",
            "0030  6c ab 45 98 4d 9f ee 3a  c1 b2 55 4d 67 28 39 8e  |l.E.M..:..UMg(9.|",
            "0040  00 00 00 7e 64 69 66 66  69 65 2d 68 65 6c 6c 6d  |...~diffie-hellm|",
        },
        {
            "FTP",
            "Hint: A greeting message typical of file transfer services.",
            "0000  32 32 30 20 28 76 73 46  54 50 64 20 33 2e 30 2e  |220 (vsFTPd 3.0.|",
            "0010  35 29 0d 0a 55 53 45 52  20 61 6e 6f 6e 79 6d 6f  |5)..USER anonymo|",
            "0020  75 73 0d 0a 33 33 31 20  50 6c 65 61 73 65 20 73  |us..331 Please s|",
            "0030  70 65 63 69 66 79 20 74  68 65 20 70 61 73 73 77  |pecify the passw|",
            "0040  6f 72 64 2e 0d 0a 50 41  53 53 20 67 75 65 73 74  |ord...PASS guest|",
        },
        {
            "SMTP",
            "Hint: Mail server greeting  -  notice the status code and EHLO.",
            "0000  32 32 30 20 6d 61 69 6c  2e 73 65 72 76 65 72 2e  |220 mail.server.|",
            "0010  63 6f 6d 20 45 53 4d 54  50 20 50 6f 73 74 66 69  |com ESMTP Postfi|",
            "0020  78 0d 0a 45 48 4c 4f 20  63 6c 69 65 6e 74 2e 6c  |x..EHLO client.l|",
            "0030  6f 63 61 6c 0d 0a 32 35  30 2d 53 49 5a 45 20 31  |ocal..250-SIZE 1|",
            "0040  30 32 34 30 30 30 30 0d  0a 32 35 30 2d 38 42 49  |0240000..250-8BI|",
        },
    };

    private boolean open = false, solved = false, panicked = false;
    private float stateTime = 0f, wrongFlash = 0f, solveTimer = 0f;
    private int packetIdx = 0;

    private BitmapFont titleFont, bodyFont, hexFont, hintFont, resultFont;
    private final StringBuilder inputBuf = new StringBuilder();
    private final InputAdapter adapter = new SnifferInputAdapter();
    private final Random rng = new Random();

    @Override
    public void open() {
        open = true; solved = false; panicked = false;
        stateTime = 0f; wrongFlash = 0f; solveTimer = 0f;
        inputBuf.setLength(0);
        packetIdx = rng.nextInt(PACKETS.length);
        disposeFonts();
        titleFont  = makeFont(1.1f);
        bodyFont   = makeFont(0.85f);
        hexFont    = makeFont(0.82f);
        hintFont   = makeFont(0.78f);
        resultFont = makeFont(1.2f);
        Gdx.input.setInputProcessor(adapter);
    }

    @Override public void close()         { open = false; disposeFonts(); Gdx.input.setInputProcessor(null); }
    @Override public boolean isOpen()     { return open; }
    @Override public boolean isSolved()   { return solved; }
    @Override public boolean wasPanicked(){ return panicked; }
    @Override public String getTitle()    { return "PACKET SNIFFER"; }

    @Override
    public void update(float dt) {
        if (!open) return;
        stateTime += dt;
        if (wrongFlash > 0) wrongFlash -= dt;
        if (solved) { solveTimer += dt; if (solveTimer > 2f) close(); }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont ignoredFont) {
        if (!open || bodyFont == null) return;
        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 2.5f);

        Gdx.gl.glEnable(GL20.GL_BLEND);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.92f); sr.rect(0, 0, W, H);

        float wx = 140f, wy = 60f, ww = 1000f, wh = 584f;
        sr.setColor(0.02f, 0.03f, 0.06f, 1f); sr.rect(wx, wy, ww, wh);
        sr.setColor(0f, 0.06f, 0.18f, 1f); sr.rect(wx, wy + wh - 38, ww, 38);
        sr.setColor(0.05f, 0.18f * pulse, 0.4f * pulse, 0.2f); sr.rect(wx - 5, wy - 5, ww + 10, wh + 10);

        // Hex dump background
        sr.setColor(0.01f, 0.02f, 0.04f, 1f); sr.rect(wx + 30, wy + 180, ww - 60, 280);

        // Input box
        float inputY = wy + 100f;
        sr.setColor(0.02f, 0.04f, 0.02f, 1f); sr.rect(wx + 30, inputY, ww - 60, 34f);

        if (wrongFlash > 0) { sr.setColor(0.7f, 0f, 0f, wrongFlash * 0.35f); sr.rect(wx, wy, ww, wh); }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.2f, 0.4f, 0.9f, 0.8f); sr.rect(wx, wy, ww, wh);
        sr.setColor(0f, 0.5f, 0.3f, 0.7f); sr.rect(wx + 30, inputY, ww - 60, 34f);
        sr.setColor(0.15f, 0.3f, 0.6f, 0.5f); sr.rect(wx + 30, wy + 180, ww - 60, 280);
        sr.end();

        batch.begin();
        String[] pkt = PACKETS[packetIdx];

        titleFont.setColor(0.4f, 0.7f, 1f, 1f);
        titleFont.draw(batch, "  [ PACKET SNIFFER ]    [ESC/TAB CLOSE]", wx + 10, wy + wh - 12f);

        bodyFont.setColor(0.7f, 0.8f, 0.9f, 1f);
        bodyFont.draw(batch, "CAPTURED PACKET - Identify the protocol below.", wx + 40, wy + wh - 55f);
        bodyFont.draw(batch, "Use the hex dump and ASCII hints in the capture.", wx + 40, wy + wh - 73f);

        hintFont.setColor(0.3f, 0.5f, 0.3f, 1f);
        hintFont.draw(batch, pkt[1], wx + 40, wy + wh - 96f);

        // Hex lines
        float hexY = wy + 440f;
        for (int i = 2; i < pkt.length; i++) {
            hexFont.setColor(0f, 0.85f, 0.55f, 1f);
            hexFont.draw(batch, pkt[i], wx + 40, hexY);
            hexY -= 18f;
        }

        // Input
        bodyFont.setColor(0.5f, 0.5f, 0.6f, 1f);
        bodyFont.draw(batch, "TYPE THE PROTOCOL NAME", wx + 35, inputY + 74f);
        bodyFont.draw(batch, "(HTTP, DNS, SSH, FTP, SMTP)  [ENTER] submit", wx + 35, inputY + 56f);

        hexFont.setColor(0f, 0.7f, 0.3f, 1f);
        hexFont.draw(batch, "$ identify>", wx + 38, inputY + 24f);
        hexFont.setColor(Color.WHITE);
        boolean blink = ((int)(stateTime * 2)) % 2 == 0;
        hexFont.draw(batch, inputBuf.toString() + (blink ? "|" : " "), wx + 150, inputY + 24f);

        if (solved) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            resultFont.setColor(0f, fl, fl * 0.4f, 1f);
            resultFont.draw(batch, "[OK]  PROTOCOL IDENTIFIED - KEY ACQUIRED", wx + 200, wy + 50f);
        } else if (wrongFlash > 0) {
            resultFont.setColor(1f, 0.2f, 0.1f, 1f);
            resultFont.draw(batch, "[X]  WRONG PROTOCOL - TRY AGAIN", wx + 280, wy + 50f);
        }
        batch.end();
    }

    private BitmapFont makeFont(float s) {
        return FontManager.create(s);
    }
    private void disposeFonts() {
        if (titleFont  != null) titleFont.dispose();
        if (bodyFont   != null) bodyFont.dispose();
        if (hexFont    != null) hexFont.dispose();
        if (hintFont   != null) hintFont.dispose();
        if (resultFont != null) resultFont.dispose();
        titleFont = bodyFont = hexFont = hintFont = resultFont = null;
    }

    private class SnifferInputAdapter extends InputAdapter {
        @Override public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) { panicked = true; close(); return true; }
            if ((k == Input.Keys.BACKSPACE || k == Input.Keys.DEL || k == Input.Keys.FORWARD_DEL) && inputBuf.length() > 0) { inputBuf.deleteCharAt(inputBuf.length()-1); return true; }
            if (k == Input.Keys.ENTER || k == Input.Keys.NUMPAD_ENTER) {
                String ans = inputBuf.toString().trim().toUpperCase();
                if (ans.equals(PACKETS[packetIdx][0])) { solved = true; solveTimer = 0f; }
                else { wrongFlash = 0.8f; inputBuf.setLength(0); }
                return true;
            }
            return false;
        }
        @Override public boolean keyTyped(char c) {
            if (solved || !open) return true;
            if (c >= 32 && c < 127 && inputBuf.length() < 16) inputBuf.append(c);
            return true;
        }
    }
}
