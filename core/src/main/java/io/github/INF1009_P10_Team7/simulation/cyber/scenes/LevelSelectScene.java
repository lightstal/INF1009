package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * Level Select screen — two mission cards with key stats and flavour text.
 * A/D or LEFT/RIGHT to browse; SPACE or ENTER to launch; ESC to go back.
 */
/**
 * LevelSelectScene — lets the player choose which level to play.
 *
 * <p>Renders a card for each available level with its name and
 * difficulty description. On selection it transitions to the corresponding
 * {@link LevelCutsceneScene} before loading the game.</p>
 */
public class LevelSelectScene extends Scene {

    private static final float W = 1280f, H = 704f;

    // ── Card layout (2 cards) ───────────────────────────────────────────────
    private static final int   NUM_LEVELS = 2;
    private static final float CARD_W     = 380f;
    private static final float CARD_H     = 440f;
    private static final float GAP        = 40f;
    private static final float TOTAL_W    = NUM_LEVELS * CARD_W + (NUM_LEVELS - 1) * GAP;
    private static final float CARDS_X    = (W - TOTAL_W) / 2f;
    private static final float CARD_Y     = (H - CARD_H) / 2f - 20f;

    // ── Level metadata ───────────────────────────────────────────────────────
    private static final String[] TITLES    = { "INITIATION", "INFILTRATION" };
    private static final String[] SUBTITLES = { "STAR FORMATION", "Z-SHAPE COMPLEX" };
    private static final String[] FLAVOUR   = {
        "Five isolated labs in a star. No patrols tonight.",
        "Two wings + bridge. Three drones sweep the corridor. Eight cameras guard every angle. Step into the light and they all come.",
    };
    private static final int[] TERMINALS = { 5, 5 };
    private static final int[] DRONES    = { 0, 3 };
    private static final String[] TIMERS = { "6:00", "6:30" };
    private static final String[] DIFF   = { "ROOKIE", "AGENT" };

    private static final float[][] ACCENT = {
        { 0.20f, 0.45f, 1.00f },   // blue
        { 0.00f, 0.80f, 0.55f },   // teal
    };

    private int   selected  = 0;
    private float stateTime = 0f;
    private float selectAnim = 0f;

    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator   nav;
    private final CyberSceneFactory factory;

    private ShapeRenderer sr;
    private SpriteBatch   batch;
    private BitmapFont    titleFont, labelFont, bodyFont, smallFont;
    private GlyphLayout   layout;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    public LevelSelectScene(IInputController input, IAudioController audio, SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.input = input; this.audio = audio; this.nav = nav; this.factory = factory;
    }

    @Override
    protected void onLoad() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(W, H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(W / 2f, H / 2f, 0f);
        camera.update();

        sr    = new ShapeRenderer();
        batch = new SpriteBatch();
        titleFont = makeFont(2.0f);
        labelFont = makeFont(1.1f);
        bodyFont  = makeFont(0.78f);
        smallFont = makeFont(0.68f);
        layout    = new GlyphLayout();
    }

    private BitmapFont makeFont(float scale) {
        return FontManager.create(scale);
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        if (selectAnim > 0) selectAnim -= dt * 4f;

        if (input.isActionJustPressed("MENU_BACK")) {
            nav.setScene(factory.createMainMenuScene()); return;
        }
        if (input.isActionJustPressed("LEFT") || input.isActionJustPressed("MENU_LEFT")) {
            selected = (selected + NUM_LEVELS - 1) % NUM_LEVELS; selectAnim = 1f;
        }
        if (input.isActionJustPressed("RIGHT") || input.isActionJustPressed("MENU_RIGHT")) {
            selected = (selected + 1) % NUM_LEVELS; selectAnim = 1f;
        }
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("MENU_CONFIRM")) {
            nav.setScene(factory.createCutsceneScene(selected + 1));
        }
    }

    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 2.5f);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(0.10f, 0.18f, 0.28f, 0.35f);
        for (float gx = 0; gx < W; gx += 40) for (float gy = 0; gy < H; gy += 40) sr.rect(gx, gy, 2, 2);

        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y;
            boolean sel = (i == selected);
            float[] ac = ACCENT[i];
            float lift = sel ? 8f : 0f;
            cy += lift;

            if (sel) {
                sr.setColor(ac[0]*0.3f, ac[1]*0.3f, ac[2]*0.3f, 0.4f);
                sr.rect(cx + 4, cy - 4, CARD_W, CARD_H);
            }

            sr.setColor(0.05f, 0.06f, 0.09f, 1f);
            sr.rect(cx, cy, CARD_W, CARD_H);

            float barA = sel ? 1f : 0.6f;
            sr.setColor(ac[0], ac[1], ac[2], barA);
            sr.rect(cx, cy + CARD_H - 8, CARD_W, 8);

            sr.setColor(ac[0]*0.3f, ac[1]*0.3f, ac[2]*0.3f, 0.8f);
            sr.rect(cx + 10, cy + CARD_H - 48, 80, 24);

            sr.setColor(ac[0]*0.2f, ac[1]*0.2f, ac[2]*0.2f, 0.7f);
            sr.circle(cx + CARD_W - 28, cy + CARD_H - 30, 18f, 20);

            sr.setColor(0.08f, 0.10f, 0.14f, 1f);
            sr.rect(cx + 10, cy + 84, CARD_W - 20, 28);
            sr.rect(cx + 10, cy + 50, CARD_W - 20, 28);
            sr.rect(cx + 10, cy + 16, CARD_W - 20, 28);

            if (sel) {
                float g = pulse * 0.6f;
                sr.setColor(ac[0]*g, ac[1]*g, ac[2]*g, 0.18f);
                sr.rect(cx - 6, cy - 6, CARD_W + 12, CARD_H + 12);
            }
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y + (i == selected ? 8f : 0f);
            float[] ac = ACCENT[i];
            boolean sel = (i == selected);
            float alpha = sel ? 0.85f + 0.15f * pulse : 0.35f;
            sr.setColor(ac[0], ac[1], ac[2], alpha);
            sr.rect(cx, cy, CARD_W, CARD_H);
            sr.setColor(ac[0]*0.4f, ac[1]*0.4f, ac[2]*0.4f, sel ? 0.5f : 0.2f);
            sr.rect(cx + 3, cy + 3, CARD_W - 6, CARD_H - 6);
        }
        sr.end();

        batch.begin();

        titleFont.setColor(0.45f, 0.75f, 1f, 1f);
        layout.setText(titleFont, "SELECT MISSION");
        titleFont.draw(batch, "SELECT MISSION", W/2f - layout.width/2f, H - 38f);

        smallFont.setColor(0.4f, 0.55f, 0.65f, 1f);
        String nav_hint = "[ A / LEFT ]  browse  [ D / RIGHT ]          [ ENTER / SPACE ]  deploy          [ ESC ]  back";
        layout.setText(smallFont, nav_hint);
        smallFont.draw(batch, nav_hint, W/2f - layout.width/2f, 28f);

        for (int i = 0; i < NUM_LEVELS; i++) {
            float cx = CARDS_X + i * (CARD_W + GAP);
            float cy = CARD_Y + (i == selected ? 8f : 0f);
            boolean sel = (i == selected);
            float[] ac = ACCENT[i];
            Color acC = new Color(ac[0], ac[1], ac[2], 1f);

            smallFont.setColor(acC);
            smallFont.draw(batch, DIFF[i], cx + 14, cy + CARD_H - 32f);

            labelFont.setColor(acC);
            layout.setText(labelFont, String.valueOf(i + 1));
            labelFont.draw(batch, String.valueOf(i + 1), cx + CARD_W - 28 - layout.width/2f, cy + CARD_H - 20f);

            labelFont.setColor(sel ? acC : new Color(0.7f, 0.7f, 0.8f, 1f));
            layout.setText(labelFont, TITLES[i]);
            labelFont.draw(batch, TITLES[i], cx + CARD_W/2f - layout.width/2f, cy + CARD_H - 62f);

            smallFont.setColor(sel ? new Color(ac[0]*0.9f, ac[1]*0.9f, ac[2]*0.9f, 1f)
                                   : new Color(0.45f, 0.5f, 0.6f, 1f));
            layout.setText(smallFont, SUBTITLES[i]);
            smallFont.draw(batch, SUBTITLES[i], cx + CARD_W/2f - layout.width/2f, cy + CARD_H - 82f);

            bodyFont.setColor(0.55f, 0.60f, 0.68f, 1f);
            drawWrapped(batch, bodyFont, FLAVOUR[i], cx + 12, cy + CARD_H - 108f, CARD_W - 24, 16f);

            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, " TERMINALS", cx + 14, cy + 108f);
            labelFont.setColor(acC);
            layout.setText(labelFont, String.valueOf(TERMINALS[i]));
            labelFont.draw(batch, String.valueOf(TERMINALS[i]), cx + CARD_W - 14 - layout.width, cy + 108f);

            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, " DRONES", cx + 14, cy + 74f);
            labelFont.setColor(DRONES[i] == 0 ? new Color(0.3f, 0.9f, 0.5f, 1f) : new Color(1f, 0.5f, 0.2f, 1f));
            layout.setText(labelFont, DRONES[i] == 0 ? "NONE" : String.valueOf(DRONES[i]));
            labelFont.draw(batch, DRONES[i] == 0 ? "NONE" : String.valueOf(DRONES[i]),
                cx + CARD_W - 14 - layout.width, cy + 74f);

            smallFont.setColor(0.45f, 0.55f, 0.65f, 1f);
            smallFont.draw(batch, " TIME LIMIT", cx + 14, cy + 40f);
            labelFont.setColor(new Color(1f, 0.85f, 0.2f, 1f));
            layout.setText(labelFont, TIMERS[i]);
            labelFont.draw(batch, TIMERS[i], cx + CARD_W - 14 - layout.width, cy + 40f);

            if (sel) {
                float bp = 0.6f + 0.4f * pulse;
                bodyFont.setColor(ac[0]*bp, ac[1]*bp, ac[2]*bp, 1f);
                layout.setText(bodyFont, ">  ENTER / SPACE  TO DEPLOY");
                bodyFont.draw(batch, ">  ENTER / SPACE  TO DEPLOY",
                    cx + CARD_W/2f - layout.width/2f, cy - 24f);
            }
        }

        batch.end();
    }

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
    public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }

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
    public void onUnload() { }
}
