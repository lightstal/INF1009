package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

public class LevelCutsceneRenderer {

    private static final float W = 1280f;
    private static final float H = 704f;

    private float[] accent;
    private String[] titles;
    private String[] bodyLines;
    private String[] kicker;

    private OrthographicCamera camera;
    private StretchViewport viewport;

    private ShapeRenderer sr;
    private SpriteBatch batch;
    private GlyphLayout layout;

    private BitmapFont titleFont;
    private BitmapFont labelFont;
    private BitmapFont bodyFont;
    private BitmapFont kickerFont;
    private BitmapFont hintFont;

    public LevelCutsceneRenderer(float[] accent, String[] titles, String[] bodyLines, String[] kicker) {
        this.accent = accent;
        this.titles = titles;
        this.bodyLines = bodyLines;
        this.kicker = kicker;
    }

    public void load() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(W, H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(W / 2f, H / 2f, 0f);
        camera.update();

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        titleFont = FontManager.create(1.55f);
        labelFont = FontManager.create(0.95f);
        bodyFont = FontManager.create(0.82f);
        kickerFont = FontManager.create(1.10f);
        hintFont = FontManager.create(0.60f);
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
    }

    public void render(float overlayAlpha,
                        boolean typingPhase,
                        boolean holdPhase,
                        boolean fadeOutPhase,
                        boolean typingDone,
                        float phaseTimer,
                        int lineIdx,
                        int charIdx) {
        if (viewport == null || camera == null || sr == null || batch == null) return;

        viewport.apply();
        camera.update();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Background clear
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeType.Filled);

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

        // Divider under title block
        sr.setColor(accent[0], accent[1], accent[2], 0.40f);
        sr.rect(76, H - 158, W - 152, 1.5f);

        // Divider above kicker
        sr.setColor(accent[0], accent[1], accent[2], 0.40f);
        sr.rect(76, 118, W - 152, 1.5f);

        sr.end();

        // Text
        batch.begin();

        // LABEL row
        labelFont.setColor(accent[0], accent[1], accent[2], 0.90f);
        if (titles != null && titles.length > 0) labelFont.draw(batch, titles[0], 76, H - 68);

        // Big title
        titleFont.setColor(1f, 1f, 1f, 0.96f);
        if (titles != null && titles.length > 1) titleFont.draw(batch, titles[1], 76, H - 98);

        // Body lines (typewriter)
        float bodyTop = H - 178;
        float lineH = 22f;

        for (int i = 0; i < bodyLines.length; i++) {
            if (i > lineIdx) break;

            String display;
            if (i < lineIdx) {
                display = bodyLines[i];
            } else {
                int len = bodyLines[i].length();
                int shown = Math.min(charIdx, len);
                display = (len == 0) ? "" : bodyLines[i].substring(0, shown);
            }

            float alpha = (i == lineIdx && typingPhase) ? 1f : 0.80f;
            bodyFont.setColor(0.78f, 0.85f, 0.92f, alpha);
            bodyFont.draw(batch, display, 76, bodyTop - i * lineH);

            // Blinking cursor on active line
            if (i == lineIdx && typingPhase && display.length() > 0) {
                boolean cursorOn = ((int)(phaseTimer * 6)) % 2 == 0;
                if (cursorOn) {
                    layout.setText(bodyFont, display);
                    bodyFont.setColor(accent[0], accent[1], accent[2], 1f);
                    bodyFont.draw(batch, "|", 76 + layout.width + 1, bodyTop - i * lineH);
                }
            }
        }

        // Kicker lines (shown only after typing done)
        if (typingDone || holdPhase || fadeOutPhase) {
            for (int k = 0; k < kicker.length; k++) {
                kickerFont.setColor(accent[0], accent[1], accent[2], 0.95f);
                layout.setText(kickerFont, kicker[k]);
                kickerFont.draw(batch, kicker[k], W / 2f - layout.width / 2f, 100 - k * 28f);
            }
        }

        // Skip hint
        if (typingPhase || holdPhase) {
            hintFont.setColor(0.35f, 0.45f, 0.55f, 0.80f);
            String hint = "SPACE / ENTER / click to skip";
            layout.setText(hintFont, hint);
            hintFont.draw(batch, hint, W - layout.width - 24, 20);
        }

        batch.end();

        // Fade overlay
        if (overlayAlpha > 0f) {
            sr.begin(ShapeType.Filled);
            sr.setColor(0f, 0f, 0f, Math.min(1f, overlayAlpha));
            sr.rect(0, 0, W, H);
            sr.end();
        }
    }

    public void dispose() {
        try {
            if (sr != null) sr.dispose();
            if (batch != null) batch.dispose();
            if (titleFont != null) titleFont.dispose();
            if (labelFont != null) labelFont.dispose();
            if (bodyFont != null) bodyFont.dispose();
            if (kickerFont != null) kickerFont.dispose();
            if (hintFont != null) hintFont.dispose();
        } finally {
            sr = null;
            batch = null;
            camera = null;
            viewport = null;
        }
    }
}

