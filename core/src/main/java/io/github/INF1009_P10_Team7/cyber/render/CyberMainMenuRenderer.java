package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

/**
 * LibGDX-backed renderer for {@code cyber.scenes.CyberMainMenuScene}.
 *
 * <p>Kept in engine so cyber code contains no LibGDX imports/calls.</p>
 */
public class CyberMainMenuRenderer {

    private final float worldW;
    private final float worldH;

    private ShapeRenderer sr;
    private SpriteBatch batch;
    private GlyphLayout layout;
    private OrthographicCamera camera;
    private StretchViewport viewport;

    private BitmapFont titleFont;
    private BitmapFont subFont;
    private BitmapFont dotFont;
    private BitmapFont rainFont;
    private BitmapFont briefFont;
    private BitmapFont startFont;

    private float startBtnX, startBtnY, startBtnW, startBtnH;
    private float exitBtnX, exitBtnY, exitBtnW, exitBtnH;
    private boolean startHover = false;
    private boolean exitHover = false;

    // matrix rain columns
    private static final int RAIN_COLS = 50;
    private final float[] rainY = new float[RAIN_COLS];
    private final float[] rainX = new float[RAIN_COLS];
    private final float[] rainSpeed = new float[RAIN_COLS];
    private final char[] rainChar = new char[RAIN_COLS];

    private static final String CHARS = "01アイウエオカキクケコABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%&";

    private final Vector3 touchWorld = new Vector3();

    public CyberMainMenuRenderer(float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;
    }

    public void load() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(worldW, worldH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(worldW / 2f, worldH / 2f, 0);
        camera.update();

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        // Pre-generate each font at its exact needed size via FreeType
        titleFont = FontManager.createBold(2.5f);
        subFont = FontManager.create(0.85f);
        dotFont = FontManager.create(0.7f);
        rainFont = FontManager.create(0.8f);
        briefFont = FontManager.create(0.9f);
        startFont = FontManager.createBold(1.05f);

        for (int i = 0; i < RAIN_COLS; i++) {
            rainX[i] = i * (worldW / RAIN_COLS);
            rainY[i] = (float)Math.random() * worldH;
            rainSpeed[i] = 60f + (float)Math.random() * 120f;
            rainChar[i] = CHARS.charAt((int)(Math.random() * CHARS.length()));
        }
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
    }

    public void updateRain(float dt) {
        for (int i = 0; i < RAIN_COLS; i++) {
            rainY[i] -= rainSpeed[i] * dt;
            if (rainY[i] < -16f) {
                rainY[i] = worldH + 16f;
                rainChar[i] = CHARS.charAt((int)(Math.random() * CHARS.length()));
            }
        }
    }

    public void updateHover(float mouseScreenX, float mouseScreenY) {
        float panW = 560f, panH = 292f;
        float panX = worldW / 2f - panW / 2f;
        float panY = worldH / 2f - panH / 2f;

        startBtnW = 170f;
        startBtnH = 36f;
        exitBtnW = 126f;
        exitBtnH = 36f;
        float gap = 18f;
        float totalW = startBtnW + gap + exitBtnW;
        startBtnX = worldW / 2f - totalW / 2f;
        exitBtnX = startBtnX + startBtnW + gap;
        startBtnY = panY + 34f;
        exitBtnY = panY + 34f;

        touchWorld.set(mouseScreenX, mouseScreenY, 0f);
        viewport.unproject(touchWorld);
        startHover = contains(startBtnX, startBtnY, startBtnW, startBtnH, touchWorld.x, touchWorld.y);
        exitHover = contains(exitBtnX, exitBtnY, exitBtnW, exitBtnH, touchWorld.x, touchWorld.y);
    }

    private boolean contains(float x, float y, float w, float h, float px, float py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public boolean isStartHover() { return startHover; }
    public boolean isExitHover() { return exitHover; }

    public void render(float stateTime) {
        if (sr == null || batch == null || viewport == null || camera == null) return;

        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0f, 0f, 0.01f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Background
        sr.begin(ShapeType.Filled);
        sr.setColor(0f, 0f, 0.02f, 1f);
        sr.rect(0, 0, worldW, worldH);

        // Hex grid background
        float gridAlpha = 0.06f;
        sr.setColor(0f, 0.7f, 0.4f, gridAlpha);
        float hexR = 22f;
        float hexW = hexR * 1.732f;
        float hexH = hexR * 2f;
        for (float hx = 0; hx < worldW + hexW; hx += hexW) {
            for (float hy = -hexH / 2f; hy < worldH + hexH; hy += hexH * 0.75f) {
                float offX = ((int)(hy / hexH) % 2 == 0) ? 0 : hexW / 2f;
                drawHexOutline(sr, hx + offX, hy, hexR * 0.9f);
            }
        }
        sr.end();

        // Matrix rain
        batch.begin();
        for (int i = 0; i < RAIN_COLS; i++) {
            float lifeAlpha = 0.12f + 0.08f * (float)Math.sin(stateTime * 2f + i);
            rainFont.setColor(0f, lifeAlpha * 2.5f, lifeAlpha, 1f);
            rainFont.draw(batch, String.valueOf(rainChar[i]), rainX[i], rainY[i]);
            // leading bright char
            rainFont.setColor(0f, 1f, 0.5f, 0.6f);
            rainFont.draw(batch,
                String.valueOf(CHARS.charAt(((int)(stateTime * 20f) + i * 3) % CHARS.length())),
                rainX[i], rainY[i] + 16f);
        }
        batch.end();

        sr.begin(ShapeType.Filled);

        // Central dark panel
        float panW = 560f, panH = 292f;
        float panX = worldW / 2f - panW / 2f;
        float panY = worldH / 2f - panH / 2f;
        sr.setColor(0f, 0f, 0f, 0.75f);
        sr.rect(panX - 2, panY - 2, panW + 4, panH + 4);
        sr.setColor(0.01f, 0.02f, 0.04f, 0.92f);
        sr.rect(panX, panY, panW, panH);

        // Scanline over panel
        sr.setColor(0f, 0f, 0f, 0.06f);
        for (float sy = panY; sy < panY + panH; sy += 3f) sr.rect(panX, sy, panW, 1.5f);

        // Corner brackets
        float bLen = 18f, bThick = 2f;
        sr.setColor(0f, 0.9f, 0.5f, 0.9f);
        sr.rect(panX, panY + panH - bThick, bLen, bThick);
        sr.rect(panX, panY + panH - bLen, bThick, bLen);
        sr.rect(panX + panW - bLen, panY + panH - bThick, bLen, bThick);
        sr.rect(panX + panW - bThick, panY + panH - bLen, bThick, bLen);
        sr.rect(panX, panY, bLen, bThick);
        sr.rect(panX, panY, bThick, bLen);
        sr.rect(panX + panW - bLen, panY, bLen, bThick);
        sr.rect(panX + panW - bThick, panY, bThick, bLen);

        // Pulsing top border line
        float glow = 0.5f + 0.5f * (float)Math.sin(stateTime * 2.5f);
        sr.setColor(0f, glow, 0.4f, 0.8f);
        sr.rect(panX, panY + panH - 2, panW, 2);
        sr.rect(panX, panY, panW, 2);
        sr.end();

        float startGlow = startHover ? 1f : 0.84f + 0.16f * (float)Math.sin(stateTime * 4.0f);
        float exitGlow = exitHover ? 1f : 0.82f + 0.18f * (float)Math.sin(stateTime * 3.3f + 0.7f);

        sr.begin(ShapeType.Filled);
        sr.setColor(0.04f, 0.16f, 0.10f, 0.96f);
        sr.rect(startBtnX, startBtnY, startBtnW, startBtnH);
        sr.setColor(0.00f, 0.90f * startGlow, 0.48f * startGlow, 0.95f);
        sr.rect(startBtnX, startBtnY + startBtnH - 2f, startBtnW, 2f);
        sr.rect(startBtnX, startBtnY, 2f, startBtnH);

        sr.setColor(0.16f, 0.04f, 0.05f, 0.96f);
        sr.rect(exitBtnX, exitBtnY, exitBtnW, exitBtnH);
        sr.setColor(1.00f * exitGlow, 0.34f * exitGlow, 0.30f * exitGlow, 0.95f);
        sr.rect(exitBtnX, exitBtnY + exitBtnH - 2f, exitBtnW, 2f);
        sr.rect(exitBtnX + exitBtnW - 2f, exitBtnY, 2f, exitBtnH);
        sr.end();

        // Text, each font is pre-generated at its native size, no bitmap stretching
        batch.begin();

        // Title
        float titlePulse = 0.7f + 0.3f * (float)Math.sin(stateTime * 2f);
        titleFont.setColor(0f, titlePulse, titlePulse * 0.55f, 1f);
        String title = "CYBER MAZE ESCAPE";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, worldW / 2f - layout.width / 2f, panY + panH - 20f);

        // Subtitle
        subFont.setColor(0.3f, 0.6f, 0.7f, 0.8f);
        String sub = "[ WHITE-HAT INFILTRATION SIMULATION ]";
        layout.setText(subFont, sub);
        subFont.draw(batch, sub, worldW / 2f - layout.width / 2f, panY + panH - 52f);

        // Divider dots
        dotFont.setColor(0f, 0.4f, 0.25f, 0.7f);
        dotFont.draw(batch, "· · · · · · · · · · · · · · · · · · · · · · · · · ·",
            panX + 20f, panY + panH - 72f);

        // Mission brief
        String[] brief = {
            "Three servers. Three CTF challenges. One exit.",
            "Hack each terminal without triggering the Hunter Drone.",
            "",
            "WASD   -  move          [E]  -  access terminal",
            "TAB    -  panic-close terminal",
            "↑↓ arrows  -  command history inside terminal",
        };
        float by = panY + panH - 106f;
        for (String line : brief) {
            if (line.isEmpty()) { by -= 10f; continue; }
            briefFont.setColor(line.startsWith("W") || line.startsWith("T") || line.startsWith("↑")
                ? new Color(0.55f, 0.55f, 0.6f, 1f)
                : new Color(0.75f, 0.85f, 0.75f, 1f));
            briefFont.draw(batch, line, panX + 30f, by);
            by -= 19f;
        }

        // buttons label
        float dummy = 0f;
        float startBtnLabelY = startBtnY + startBtnH / 2f;

        startFont.setColor(0.90f, 1f, 0.95f, 0.98f);
        String startLabel = "START MISSION";
        layout.setText(startFont, startLabel);
        startFont.draw(batch, startLabel,
            startBtnX + startBtnW / 2f - layout.width / 2f,
            startBtnLabelY + layout.height / 2f - 2f);

        startFont.setColor(1f, 0.90f, 0.90f, 0.98f);
        String exitLabel = "EXIT";
        layout.setText(startFont, exitLabel);
        startFont.draw(batch, exitLabel,
            exitBtnX + exitBtnW / 2f - layout.width / 2f,
            exitBtnY + exitBtnH / 2f + layout.height / 2f - 2f);

        briefFont.setColor(0.44f, 0.86f, 0.74f, 0.88f);
        String hotkeys = "[SPACE] START      [ESC] EXIT";
        layout.setText(briefFont, hotkeys);
        briefFont.draw(batch, hotkeys, worldW / 2f - layout.width / 2f, panY + 18f);

        batch.end();
    }

    private void drawHexOutline(ShapeRenderer sr, float cx, float cy, float r) {
        int sides = 6;
        for (int i = 0; i < sides; i++) {
            double a1 = 2 * Math.PI * i / sides - Math.PI / 6;
            double a2 = 2 * Math.PI * (i + 1) / sides - Math.PI / 6;
            float x1 = (float)Math.cos(a1) * r + cx;
            float y1 = (float)Math.sin(a1) * r + cy;
            float x2 = (float)Math.cos(a2) * r + cx;
            float y2 = (float)Math.sin(a2) * r + cy;
            sr.rectLine(x1, y1, x2, y2, 0.8f);
        }
    }

    public void dispose() {
        try {
            if (sr != null) sr.dispose();
            if (batch != null) batch.dispose();
            if (titleFont != null) titleFont.dispose();
            if (subFont != null) subFont.dispose();
            if (dotFont != null) dotFont.dispose();
            if (rainFont != null) rainFont.dispose();
            if (briefFont != null) briefFont.dispose();
            if (startFont != null) startFont.dispose();
        } finally {
            sr = null;
            batch = null;
            layout = null;
            camera = null;
            viewport = null;
            titleFont = null;
            subFont = null;
            dotFont = null;
            rainFont = null;
            briefFont = null;
            startFont = null;
        }
    }
}

