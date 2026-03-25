package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory; // used in static create()
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * LevelCutsceneScene — quick mission-briefing cutscene shown before each level.
 *
 * Flow: FADE IN ► lines typewrite in ► brief hold ► FADE OUT ► game starts.
 * Skippable at any time with SPACE / ENTER / click.
 */
/**
 * LevelCutsceneScene — pre-level cinematic scene shown before gameplay.
 *
 * <p>Plays a scrolling text / animated intro sequence that sets up the
 * level narrative. After the sequence ends (or the player skips it) the
 * scene transitions to {@link CyberGameScene} for the selected level.</p>
 */
public class LevelCutsceneScene extends Scene {

    private static final float W = 1280f, H = 704f;

    // ── Timing ──────────────────────────────────────────────────────────────
    private static final float FADE_IN_DUR   = 0.55f;
    private static final float CHAR_DELAY    = 0.032f;   // seconds per character
    private static final float LINE_PAUSE    = 0.28f;    // extra pause between lines
    private static final float HOLD_DUR      = 1.6f;     // pause after all text done
    private static final float FADE_OUT_DUR  = 0.55f;

    // ── Per-level briefing data ──────────────────────────────────────────────
    // Format: each entry is {accent_r, accent_g, accent_b, missionTag, lines...}
    private static final Object[][][] LEVELS = {
        // Level 1 — INITIATION
        {
            { 0.20f, 0.45f, 1.00f },                          // accent colour
            { "MISSION 01", "INITIATION — STAR FORMATION" },  // title lines
            {                                                  // body lines
                "Intel confirmed. Five isolated research labs",
                "are holding the access tokens you need.",
                "",
                "Breach every terminal. Collect all 5 keys.",
                "Then reach the extraction point.",
                "",
                "No drones on site tonight. Stay sharp.",
            },
            { "BREACH ALL TERMINALS.",  "GET THE KEY.  ESCAPE." }  // kicker
        },
        // Level 2 — INFILTRATION
        {
            { 0.00f, 0.80f, 0.55f },                              // accent colour
            { "MISSION 02", "INFILTRATION — Z-SHAPE COMPLEX" },   // title lines
            {
                "Two wings. One bridge. Enemy drones patrol",
                "the corridor in sweeping arcs.",
                "",
                "Five terminals scattered across both wings.",
                "Breach them all — the key spawns at the exit.",
                "",
                "One drone tag and the alarm goes live. Move fast.",
            },
            { "BREACH ALL TERMINALS.",  "GET THE KEY.  ESCAPE." }
        },
    };

    // ── State ────────────────────────────────────────────────────────────────
    private enum Phase { FADE_IN, TYPING, HOLD, FADE_OUT }
    private Phase phase = Phase.FADE_IN;

    private final int level;
    private float     phaseTimer   = 0f;
    private float     charTimer    = 0f;
    private int       lineIdx      = 0;   // which body line we're on
    private int       charIdx      = 0;   // how many chars revealed in current line
    private boolean   typingDone   = false;
    private float     overlayAlpha = 1f;  // 1 = black, 0 = clear

    // Pre-created game scene — set by static factory method
    private io.github.INF1009_P10_Team7.engine.scene.Scene gameSceneRef;

    // ── Resources ────────────────────────────────────────────────────────────
    private ShapeRenderer    sr;
    private SpriteBatch      batch;
    private BitmapFont       titleFont, labelFont, bodyFont, kickerFont, hintFont;
    private GlyphLayout      layout;
    private OrthographicCamera camera;
    private StretchViewport   viewport;

    // ── Parsed data for this level ───────────────────────────────────────────
    private float[]    accent;
    private String[]   titles;
    private String[]   bodyLines;
    private String[]   kicker;

    public LevelCutsceneScene(IInputController input, IAudioController audio,
                               SceneNavigator nav, int level) {
        super(input, audio, nav);
        this.level = level;
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onLoad() {
        camera   = new OrthographicCamera();
        viewport = new StretchViewport(W, H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(W / 2f, H / 2f, 0f);
        camera.update();

        sr       = new ShapeRenderer();
        batch    = new SpriteBatch();
        layout   = new GlyphLayout();

        titleFont  = FontManager.create(1.55f);
        labelFont  = FontManager.create(0.95f);
        bodyFont   = FontManager.create(0.82f);
        kickerFont = FontManager.create(1.10f);
        hintFont   = FontManager.create(0.60f);

        // Pull data for this level (clamp to available)
        int idx = Math.min(level - 1, LEVELS.length - 1);
        // Accent stored as Object[] of autoboxed Floats -- unbox manually
        Object[] accentObj = LEVELS[idx][0];
        accent = new float[]{ (Float) accentObj[0], (Float) accentObj[1], (Float) accentObj[2] };
        titles    = toStringArray(LEVELS[idx][1]);
        bodyLines = toStringArray(LEVELS[idx][2]);
        kicker    = toStringArray(LEVELS[idx][3]);
    }

    private String[] toStringArray(Object[] src) {
        String[] out = new String[src.length];
        for (int i = 0; i < src.length; i++) out[i] = (String) src[i];
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onUpdate(float dt) {
        phaseTimer += dt;

        // Skip on SPACE / ENTER / click — jump straight to fade-out
        boolean skip = input.isActionJustPressed("START_GAME")
                    || input.isActionJustPressed("MENU_CONFIRM")
                    || Gdx.input.justTouched();

        switch (phase) {

            case FADE_IN:
                overlayAlpha = 1f - (phaseTimer / FADE_IN_DUR);
                if (phaseTimer >= FADE_IN_DUR || skip) {
                    overlayAlpha = 0f;
                    phase      = Phase.TYPING;
                    phaseTimer = 0f;
                    if (skip) skipToFadeOut();
                }
                break;

            case TYPING:
                if (!typingDone) {
                    charTimer += dt;
                    float advance = charTimer / CHAR_DELAY;
                    charTimer -= (int) advance * CHAR_DELAY;
                    for (int a = 0; a < (int) advance && !typingDone; a++) {
                        advanceChar();
                    }
                }
                if (typingDone) {
                    phase      = Phase.HOLD;
                    phaseTimer = 0f;
                }
                if (skip) skipToFadeOut();
                break;

            case HOLD:
                if (phaseTimer >= HOLD_DUR || skip) {
                    phase      = Phase.FADE_OUT;
                    phaseTimer = 0f;
                    overlayAlpha = 0f;
                }
                break;

            case FADE_OUT:
                overlayAlpha = phaseTimer / FADE_OUT_DUR;
                if (phaseTimer >= FADE_OUT_DUR) {
                    overlayAlpha = 1f;
                    launchGame();
                }
                break;
        }
    }

    private void advanceChar() {
        if (lineIdx >= bodyLines.length) { typingDone = true; return; }

        String line = bodyLines[lineIdx];
        if (charIdx < line.length()) {
            charIdx++;
        } else {
            // Line done — pause then move to next
            charTimer -= LINE_PAUSE;
            lineIdx++;
            charIdx = 0;
        }
    }

    private void skipToFadeOut() {
        typingDone   = true;
        lineIdx      = bodyLines.length;
        phase        = Phase.FADE_OUT;
        phaseTimer   = 0f;
        overlayAlpha = 0f;
    }

    private void launchGame() {
        nav.setScene(gameSceneRef);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // ── Background ────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Subtle grid
        sr.setColor(0.08f, 0.14f, 0.22f, 0.30f);
        for (float gx = 0; gx < W; gx += 48) sr.rect(gx, 0, 1, H);
        for (float gy = 0; gy < H; gy += 48) sr.rect(0, gy, W, 1);

        // Scanlines
        sr.setColor(0f, 0f, 0f, 0.10f);
        for (float sy = 0; sy < H; sy += 3f) sr.rect(0, sy, W, 1.5f);

        // Left accent stripe
        sr.setColor(accent[0], accent[1], accent[2], 0.85f);
        sr.rect(54, H * 0.12f, 4, H * 0.76f);

        // Horizontal divider under title block
        sr.setColor(accent[0], accent[1], accent[2], 0.40f);
        sr.rect(76, H - 158, W - 152, 1.5f);

        // Horizontal divider above kicker
        sr.setColor(accent[0], accent[1], accent[2], 0.40f);
        sr.rect(76, 118, W - 152, 1.5f);

        sr.end();

        // ── Text ─────────────────────────────────────────────────────────────
        batch.begin();

        // ---- LABEL row ----
        labelFont.setColor(accent[0], accent[1], accent[2], 0.90f);
        labelFont.draw(batch, titles[0], 76, H - 68);

        // ---- Big title ----
        titleFont.setColor(1f, 1f, 1f, 0.96f);
        titleFont.draw(batch, titles[1], 76, H - 98);

        // ---- Body lines (typewriter) ----
        float bodyTop = H - 178;
        float lineH   = 22f;

        for (int i = 0; i < bodyLines.length; i++) {
            if (i > lineIdx) break;

            String display;
            if (i < lineIdx) {
                display = bodyLines[i];
            } else {
                // Currently typing this line
                display = bodyLines[i].length() == 0 ? "" :
                          bodyLines[i].substring(0, Math.min(charIdx, bodyLines[i].length()));
            }

            float alpha = (i == lineIdx && phase == Phase.TYPING) ? 1f : 0.80f;
            bodyFont.setColor(0.78f, 0.85f, 0.92f, alpha);
            bodyFont.draw(batch, display, 76, bodyTop - i * lineH);

            // Blinking cursor on active line
            if (i == lineIdx && phase == Phase.TYPING && display.length() > 0) {
                boolean cursorOn = ((int)(phaseTimer * 6)) % 2 == 0;
                if (cursorOn) {
                    layout.setText(bodyFont, display);
                    bodyFont.setColor(accent[0], accent[1], accent[2], 1f);
                    bodyFont.draw(batch, "|", 76 + layout.width + 1, bodyTop - i * lineH);
                }
            }
        }

        // ---- Kicker lines (shown only after typing done) ----
        if (typingDone || phase == Phase.HOLD || phase == Phase.FADE_OUT) {
            for (int k = 0; k < kicker.length; k++) {
                kickerFont.setColor(accent[0], accent[1], accent[2], 0.95f);
                layout.setText(kickerFont, kicker[k]);
                kickerFont.draw(batch, kicker[k], W / 2f - layout.width / 2f,
                    100 - k * 28f);
            }
        }

        // ---- Skip hint ----
        if (phase == Phase.TYPING || phase == Phase.HOLD) {
            hintFont.setColor(0.35f, 0.45f, 0.55f, 0.80f);
            String hint = "SPACE / ENTER / click to skip";
            layout.setText(hintFont, hint);
            hintFont.draw(batch, hint, W - layout.width - 24, 20);
        }

        batch.end();

        // ── Fade overlay ─────────────────────────────────────────────────────
        if (overlayAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0f, 0f, 0f, Math.min(1f, overlayAlpha));
            sr.rect(0, 0, W, H);
            sr.end();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
    }

    @Override protected void onUnload() { }

    @Override protected void onDispose() {
        if (sr         != null) sr.dispose();
        if (batch      != null) batch.dispose();
        if (titleFont  != null) titleFont.dispose();
        if (labelFont  != null) labelFont.dispose();
        if (bodyFont   != null) bodyFont.dispose();
        if (kickerFont != null) kickerFont.dispose();
        if (hintFont   != null) hintFont.dispose();
    }

    // ── Static factory method (sets up gameSceneRef cleanly) ─────────────────
    public static LevelCutsceneScene create(IInputController input, IAudioController audio,
                                             SceneNavigator nav, CyberSceneFactory factory,
                                             int level) {
        LevelCutsceneScene scene = new LevelCutsceneScene(input, audio, nav, level);
        // Pre-create the game scene so it's ready for instant transition
        scene.gameSceneRef = factory.createGameScene(level);
        return scene;
    }
}
