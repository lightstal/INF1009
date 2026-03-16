package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;

/**
 * Level Select screen — three mission cards, non-progressive.
 * A/D or LEFT/RIGHT to browse; SPACE or ENTER to launch; ESC to go back.
 */
public class LevelSelectScene extends Scene {

    private static final float W = 1280f, H = 704f;

    // ── Card layout ─────────────────────────────────────────────────────────
    private static final int   NUM_LEVELS = 3;
    private static final float CARD_W     = 340f;
    private static final float CARD_H     = 440f;
    private static final float GAP        = 32f;
    private static final float TOTAL_W    = NUM_LEVELS * CARD_W + (NUM_LEVELS - 1) * GAP;
    private static final float CARDS_X    = (W - TOTAL_W) / 2f;  // centred
    private static final float CARD_Y     = (H - CARD_H) / 2f - 20f;

    // ── Level metadata ───────────────────────────────────────────────────────
    private static final String[] TITLES    = { "INITIATION",      "INFILTRATION",      "BREACH"           };
    private static final String[] SUBTITLES = { "STAR FORMATION",  "Z-SHAPE COMPLEX",   "4-CORNERS + VAULT"};
    private static final String[] FLAVOUR   = {
        "Five isolated labs arranged in a star. Locate each terminal and extract the data — no patrols tonight.",
        "Two wings connected by a central bridge. Enemy drones sweep the bridge corridor. Choose your path.",
        "A central vault surrounded by four wings. Three drones on rotating patrol. Breach the vault last."
    };
    private static final int[] TERMINALS = { 5, 5, 5 };
    private static final int[] DRONES    = { 0, 2, 3 };
    private static final String[] TIMERS = { "4:00", "3:40", "3:20" };
    private static final String[] DIFF   = { "ROOKIE",     "AGENT",         "PHANTOM"       };

    // Card accent colours  (R, G, B)
    private static final float[][] ACCENT = {
        { 0.20f, 0.45f, 1.00f },   // Level 1 – blue
        { 0.00f, 0.80f, 0.55f },   // Level 2 – teal/cyan
        { 0.75f, 0.25f, 1.00f },   // Level 3 – violet
    };

    // ── State ────────────────────────────────────────────────────────────────
    private int   selected  = 0;
    private float stateTime = 0f;
    private float selectAnim = 0f;

    // ── References ───────────────────────────────────────────────────────────
    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator   nav;
    private final CyberSceneFactory factory;

    private ShapeRenderer sr;
    private SpriteBatch   batch;
    private BitmapFont    titleFont, labelFont, bodyFont, smallFont;
    private GlyphLayout   layout;


    public LevelSelectScene(IInputController input, IAudioController audio, SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);  // Add this line
        this.input = input; this.audio = audio; this.nav = nav; this.factory = factory;
    }

    @Override
    protected void onLoad() {
        sr    = new ShapeRenderer();
        batch = new SpriteBatch();

        titleFont = makeFont(2.0f);
        labelFont = makeFont(1.2f);
        bodyFont  = makeFont(0.88f);
        smallFont = makeFont(0.72f);
        layout    = new GlyphLayout();
    }

    private BitmapFont makeFont(float scale) {
        BitmapFont f = new BitmapFont();
        f.getData().setScale(scale);
        f.setUseIntegerPositions(true);
        return f;
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        if (selectAnim > 0) selectAnim -= dt * 4f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            nav.setScene(factory.createMainMenuScene());
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)  || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selected = (selected + NUM_LEVELS - 1) % NUM_LEVELS; selectAnim = 1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selected = (selected + 1) % NUM_LEVELS; selectAnim = 1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            nav.setScene(factory.createGameScene(selected + 1));
        }
    }

    @Override
    protected void onRender() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 2.5f);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Background grid dots
        sr.setColor(0.10f, 0.18f, 0.28f, 0.35f);
        for (float gx = 0; gx < W; gx += 40) for (float gy = 0; gy < H; gy += 40) sr.rect(gx, gy, 2, 2);

        // ── Draw each card ───────────────────────────────────────────────────
        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y;
            boolean sel = (i == selected);

            float[] ac = ACCENT[i];
            float lift = sel ? 8f : 0f;
            cy += lift;

            // Shadow
            if (sel) {
                sr.setColor(ac[0]*0.3f, ac[1]*0.3f, ac[2]*0.3f, 0.4f);
                sr.rect(cx + 4, cy - 4, CARD_W, CARD_H);
            }

            // Card body
            sr.setColor(0.05f, 0.06f, 0.09f, 1f);
            sr.rect(cx, cy, CARD_W, CARD_H);

            // Top accent bar
            float barA = sel ? 1f : 0.6f;
            sr.setColor(ac[0], ac[1], ac[2], barA);
            sr.rect(cx, cy + CARD_H - 8, CARD_W, 8);

            // Difficulty badge
            sr.setColor(ac[0]*0.3f, ac[1]*0.3f, ac[2]*0.3f, 0.8f);
            sr.rect(cx + 14, cy + CARD_H - 52, 110, 28);

            // Level number circle
            sr.setColor(ac[0]*0.2f, ac[1]*0.2f, ac[2]*0.2f, 0.7f);
            sr.circle(cx + CARD_W - 36, cy + CARD_H - 36, 22f, 24);

            // Stat icon boxes
            sr.setColor(0.08f, 0.10f, 0.14f, 1f);
            sr.rect(cx + 14, cy + 94,  CARD_W - 28, 32);
            sr.rect(cx + 14, cy + 54,  CARD_W - 28, 32);
            sr.rect(cx + 14, cy + 14,  CARD_W - 28, 32);

            // Selected: glow halo + animated border pulse
            if (sel) {
                float g = pulse * 0.6f;
                sr.setColor(ac[0]*g, ac[1]*g, ac[2]*g, 0.18f);
                sr.rect(cx - 6, cy - 6, CARD_W + 12, CARD_H + 12);
            }
        }
        sr.end();

        // ── Border lines ─────────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y + (i == selected ? 8f : 0f);
            float[] ac = ACCENT[i];
            boolean sel = (i == selected);
            float alpha = sel ? 0.85f + 0.15f * pulse : 0.35f;
            sr.setColor(ac[0], ac[1], ac[2], alpha);
            sr.rect(cx, cy, CARD_W, CARD_H);
            // Inner inset border
            sr.setColor(ac[0]*0.4f, ac[1]*0.4f, ac[2]*0.4f, sel ? 0.5f : 0.2f);
            sr.rect(cx + 3, cy + 3, CARD_W - 6, CARD_H - 6);
        }
        sr.end();

        // ── Text ─────────────────────────────────────────────────────────────
        batch.begin();

        // Page title
        titleFont.setColor(0.45f, 0.75f, 1f, 1f);
        layout.setText(titleFont, "SELECT MISSION");
        titleFont.draw(batch, "SELECT MISSION", W/2f - layout.width/2f, H - 38f);

        // Subtitle
        smallFont.setColor(0.4f, 0.55f, 0.65f, 1f);
        layout.setText(smallFont, "[ A / ◄ ]  browse  [ D / ► ]          [ ENTER / SPACE ]  deploy          [ ESC ]  back");
        smallFont.draw(batch, "[ A / ◄ ]  browse  [ D / ► ]          [ ENTER / SPACE ]  deploy          [ ESC ]  back",
            W/2f - layout.width/2f, 28f);

        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y + (i == selected ? 8f : 0f);
            boolean sel = (i == selected);
            float[] ac = ACCENT[i];
            Color acC = new Color(ac[0], ac[1], ac[2], 1f);

            // Difficulty badge text
            smallFont.setColor(acC);
            smallFont.draw(batch, DIFF[i], cx + 18, cy + CARD_H - 32f);

            // Level number
            labelFont.setColor(acC);
            layout.setText(labelFont, String.valueOf(i + 1));
            labelFont.draw(batch, String.valueOf(i + 1), cx + CARD_W - 36 - layout.width/2f, cy + CARD_H - 26f);

            // Mission title
            labelFont.setColor(sel ? acC : new Color(0.7f, 0.7f, 0.8f, 1f));
            layout.setText(labelFont, TITLES[i]);
            labelFont.draw(batch, TITLES[i], cx + CARD_W/2f - layout.width/2f, cy + CARD_H - 70f);

            // Subtitle
            smallFont.setColor(sel ? new Color(ac[0]*0.9f, ac[1]*0.9f, ac[2]*0.9f, 1f)
                                   : new Color(0.45f, 0.5f, 0.6f, 1f));
            layout.setText(smallFont, SUBTITLES[i]);
            smallFont.draw(batch, SUBTITLES[i], cx + CARD_W/2f - layout.width/2f, cy + CARD_H - 94f);

            // Divider (done via ShapeRenderer above)

            // Flavour text — word wrap manually by splitting
            bodyFont.setColor(0.55f, 0.60f, 0.68f, 1f);
            drawWrapped(batch, bodyFont, FLAVOUR[i], cx + 16, cy + CARD_H - 120f, CARD_W - 32, 20f);

            // Stats
            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, "  TERMINALS", cx + 18, cy + 120f);
            labelFont.setColor(acC);
            layout.setText(labelFont, String.valueOf(TERMINALS[i]));
            labelFont.draw(batch, String.valueOf(TERMINALS[i]), cx + CARD_W - 18 - layout.width, cy + 120f);

            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, "  DRONES", cx + 18, cy + 80f);
            labelFont.setColor(DRONES[i] == 0 ? new Color(0.3f, 0.9f, 0.5f, 1f) : new Color(1f, 0.5f, 0.2f, 1f));
            layout.setText(labelFont, DRONES[i] == 0 ? "NONE" : String.valueOf(DRONES[i]));
            labelFont.draw(batch, DRONES[i] == 0 ? "NONE" : String.valueOf(DRONES[i]),
                cx + CARD_W - 18 - layout.width, cy + 80f);

            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, "  TIME LIMIT", cx + 18, cy + 40f);
            labelFont.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            layout.setText(labelFont, TIMERS[i]);
            labelFont.draw(batch, TIMERS[i], cx + CARD_W - 18 - layout.width, cy + 40f);

            // SELECTED: "PRESS ENTER" call to action
            if (sel) {
                float bp = 0.6f + 0.4f * pulse;
                bodyFont.setColor(ac[0]*bp, ac[1]*bp, ac[2]*bp, 1f);
                layout.setText(bodyFont, "▶  PRESS ENTER TO DEPLOY");
                bodyFont.draw(batch, "▶  PRESS ENTER TO DEPLOY",
                    cx + CARD_W/2f - layout.width/2f, cy - 24f);
            }
        }

        batch.end();
    }

    /** Naive word-wrap: draw bodyFont text into width, descending by lineH each line */
    private void drawWrapped(SpriteBatch b, BitmapFont f, String text, float x, float y, float maxW, float lineH) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float curY = y;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            layout.setText(f, test);
            if (layout.width > maxW && line.length() > 0) {
                f.draw(b, line.toString(), x, curY);
                curY -= lineH;
                line.setLength(0);
                line.append(w);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(w);
            }
        }
        if (line.length() > 0) f.draw(b, line.toString(), x, curY);
    }

    @Override
    protected void onDispose() {
        if (sr    != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (labelFont != null) labelFont.dispose();
        if (bodyFont  != null) bodyFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }

    @Override
    public void onUnload() {
        // Clean up resources here if needed
    }
}
