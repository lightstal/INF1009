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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * CHALLENGE: FIREWALL ACL
 *
 * A set of firewall rules are displayed in a SCRAMBLED order.
 * The player must re-order them into the correct sequence by pressing
 * number keys to pick which rule goes into the next slot.
 * Correct order: block malicious traffic first, then allow legitimate,
 * then default deny at the end.
 *
 * Implements IMiniGame (Strategy pattern).
 */
public class FirewallACLGame implements IMiniGame {

    private static final float W = 1280f;
    private static final float H = 704f;

    // ── Rule sets  -  each is {correctOrder-label, description} ────────────
    private static final String[][] RULE_SET_A = {
        {"DENY",  "src 10.0.0.0/8  dst ANY  port 4444    # C2 callback"},
        {"DENY",  "src ANY          dst ANY  port 23      # telnet (insecure)"},
        {"ALLOW", "src 192.168.1.0/24 dst 10.0.0.5 port 443  # internal HTTPS"},
        {"ALLOW", "src 192.168.1.0/24 dst 10.0.0.10 port 22  # SSH admin"},
        {"DENY",  "src ANY          dst ANY  port ANY     # default deny all"},
    };

    private static final String[][] RULE_SET_B = {
        {"DENY",  "src ANY          dst ANY  port 445     # SMB (WannaCry)"},
        {"DENY",  "src 203.0.113.0/24 dst ANY port ANY    # known attacker range"},
        {"ALLOW", "src 10.1.0.0/16  dst 10.2.0.5 port 3306  # MySQL replication"},
        {"ALLOW", "src 10.1.0.0/16  dst 10.2.0.1 port 80    # web server"},
        {"DENY",  "src ANY          dst ANY  port ANY     # default deny all"},
    };

    private static final String[][][] ALL_SETS = { RULE_SET_A, RULE_SET_B };

    private boolean open = false, solved = false, panicked = false;
    private float stateTime = 0f, wrongFlash = 0f, solveTimer = 0f;

    // Game state
    private String[][] currentRules;       // the correct order
    private List<Integer> shuffledIndices;  // scrambled display order
    private List<Integer> playerOrder;      // rules the player has placed so far
    private int setIdx = 0;

    private BitmapFont titleFont, bodyFont, ruleFont, hintFont, resultFont;
    private GlyphLayout glLayout;
    private final InputAdapter adapter = new ACLInputAdapter();
    private final Random rng = new Random();

    @Override
    public void open() {
        open = true; solved = false; panicked = false;
        stateTime = 0f; wrongFlash = 0f; solveTimer = 0f;

        setIdx = rng.nextInt(ALL_SETS.length);
        currentRules = ALL_SETS[setIdx];

        // Shuffle indices
        shuffledIndices = new ArrayList<>();
        for (int i = 0; i < currentRules.length; i++) shuffledIndices.add(i);
        Collections.shuffle(shuffledIndices, rng);

        playerOrder = new ArrayList<>();

        disposeFonts();
        titleFont  = makeFont(1.1f);
        bodyFont   = makeFont(0.85f);
        ruleFont   = makeFont(0.80f);
        hintFont   = makeFont(0.75f);
        resultFont = makeFont(1.2f);
        glLayout   = new GlyphLayout();

        Gdx.input.setInputProcessor(adapter);
    }

    @Override public void close()          { open = false; disposeFonts(); Gdx.input.setInputProcessor(null); }
    @Override public boolean isOpen()      { return open; }
    @Override public boolean isSolved()    { return solved; }
    @Override public boolean wasPanicked() { return panicked; }
    @Override public String getTitle()     { return "FIREWALL ACL"; }

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

        float wx = 100f, wy = 50f, ww = 1080f, wh = 604f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.92f); sr.rect(0, 0, W, H);
        sr.setColor(0.03f, 0.03f, 0.07f, 1f); sr.rect(wx, wy, ww, wh);
        sr.setColor(0.08f, 0.02f, 0f, 1f); sr.rect(wx, wy + wh - 38, ww, 38);
        sr.setColor(0.6f * pulse, 0.15f * pulse, 0f, 0.15f); sr.rect(wx - 5, wy - 5, ww + 10, wh + 10);

        // Rule slots
        float slotX = wx + 30, slotY = wy + wh - 150, slotW = ww - 60, slotH = 36;
        for (int i = 0; i < currentRules.length; i++) {
            float y = slotY - i * (slotH + 8);
            boolean placed = i < playerOrder.size();
            if (placed) {
                int ruleIdx = playerOrder.get(i);
                boolean isCorrect = ruleIdx == i; // correct if placed at natural index
                sr.setColor(isCorrect ? new Color(0f, 0.12f, 0.05f, 1f)
                                      : new Color(0.04f, 0.04f, 0.08f, 1f));
            } else if (i == playerOrder.size()) {
                // next slot to fill  -  highlighted
                sr.setColor(0.08f, 0.06f, 0.02f, 1f);
            } else {
                sr.setColor(0.02f, 0.02f, 0.04f, 1f);
            }
            sr.rect(slotX, y, slotW, slotH);
        }

        // Available rules panel
        float avY = wy + 60f;
        sr.setColor(0.02f, 0.03f, 0.05f, 1f);
        sr.rect(slotX, avY, slotW, 140f);

        if (wrongFlash > 0) { sr.setColor(0.7f, 0f, 0f, wrongFlash * 0.3f); sr.rect(wx, wy, ww, wh); }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.7f, 0.3f, 0.1f, 0.7f); sr.rect(wx, wy, ww, wh);
        for (int i = 0; i < currentRules.length; i++) {
            float y = slotY - i * (slotH + 8);
            boolean isNext = (i == playerOrder.size());
            sr.setColor(isNext ? new Color(1f, 0.7f, 0f, 0.8f) : new Color(0.3f, 0.3f, 0.4f, 0.4f));
            sr.rect(slotX, y, slotW, slotH);
        }
        sr.setColor(0.3f, 0.4f, 0.5f, 0.4f); sr.rect(slotX, avY, slotW, 140f);
        sr.end();

        batch.begin();

        titleFont.setColor(1f, 0.6f, 0.2f, 1f);
        titleFont.draw(batch, "  [ FIREWALL ACL ORDERING ]    [ESC/TAB=CLOSE]", wx + 10, wy + wh - 12f);

        bodyFont.setColor(0.7f, 0.75f, 0.8f, 1f);
        bodyFont.draw(batch, "ORDER THE RULES: Block malicious first, allow legitimate, default deny last.",
            wx + 40, wy + wh - 56f);
        bodyFont.draw(batch, "Press the NUMBER KEY (1-" + currentRules.length + ") of the rule to place in the next slot.",
            wx + 40, wy + wh - 76f);

        // Placed rules
        for (int i = 0; i < playerOrder.size(); i++) {
            float y = slotY - i * (slotH + 8);
            int ri = playerOrder.get(i);
            String[] rule = currentRules[ri];
            ruleFont.setColor(rule[0].equals("DENY") ? new Color(1f, 0.4f, 0.3f, 1f)
                                                      : new Color(0.3f, 1f, 0.5f, 1f));
            ruleFont.draw(batch, "SLOT " + (i+1) + ": [" + rule[0] + "]  " + rule[1], slotX + 8, y + 26f);
        }

        // Empty slot labels
        for (int i = playerOrder.size(); i < currentRules.length; i++) {
            float y = slotY - i * (slotH + 8);
            hintFont.setColor(0.4f, 0.4f, 0.5f, 0.6f);
            hintFont.draw(batch, "SLOT " + (i+1) + ": (empty)", slotX + 8, y + 24f);
        }

        // Available rules list
        hintFont.setColor(0.5f, 0.5f, 0.6f, 1f);
        hintFont.draw(batch, "AVAILABLE RULES (press number to select):", slotX + 4, avY + 136f);
        int shown = 0;
        for (int si = 0; si < shuffledIndices.size(); si++) {
            int ri = shuffledIndices.get(si);
            if (playerOrder.contains(ri)) continue;
            String[] rule = currentRules[ri];
            ruleFont.setColor(rule[0].equals("DENY") ? new Color(1f, 0.5f, 0.4f, 1f)
                                                      : new Color(0.4f, 1f, 0.6f, 1f));
            ruleFont.draw(batch, "  [" + (si + 1) + "]  " + rule[0] + "  " + rule[1],
                slotX + 8, avY + 116f - shown * 22f);
            shown++;
        }

        if (solved) {
            float fl = 0.5f + 0.5f * (float)Math.sin(stateTime * 8f);
            resultFont.setColor(0f, fl, fl * 0.4f, 1f);
            resultFont.draw(batch, "[OK]  ACL CONFIGURED - KEY ACQUIRED", wx + 250, wy + 26f);
        } else if (wrongFlash > 0) {
            resultFont.setColor(1f, 0.2f, 0.1f, 1f);
            resultFont.draw(batch, "[X]  WRONG ORDER - RESET & TRY AGAIN", wx + 250, wy + 26f);
        }

        batch.end();
    }

    private void tryPlaceRule(int displayNum) {
        if (solved || displayNum < 1 || displayNum > shuffledIndices.size()) return;
        int ri = shuffledIndices.get(displayNum - 1);
        if (playerOrder.contains(ri)) return;

        int nextSlot = playerOrder.size();
        // Check if this is the correct rule for this slot position
        if (ri == nextSlot) {
            playerOrder.add(ri);
            if (playerOrder.size() == currentRules.length) {
                solved = true;
                solveTimer = 0f;
            }
        } else {
            wrongFlash = 0.8f;
            playerOrder.clear(); // reset on wrong placement
        }
    }

    private BitmapFont makeFont(float s) {
        return FontManager.create(s);
    }
    private void disposeFonts() {
        if (titleFont  != null) titleFont.dispose();
        if (bodyFont   != null) bodyFont.dispose();
        if (ruleFont   != null) ruleFont.dispose();
        if (hintFont   != null) hintFont.dispose();
        if (resultFont != null) resultFont.dispose();
        titleFont = bodyFont = ruleFont = hintFont = resultFont = null;
    }

    private class ACLInputAdapter extends InputAdapter {
        @Override public boolean keyDown(int k) {
            if (k == Input.Keys.TAB || k == Input.Keys.ESCAPE) { panicked = true; close(); return true; }
            if (k >= Input.Keys.NUM_1 && k <= Input.Keys.NUM_9) {
                tryPlaceRule(k - Input.Keys.NUM_1 + 1);
                return true;
            }
            return false;
        }
    }
}
