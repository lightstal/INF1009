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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

public class CyberGameOverRenderer {
    private final float worldW;
    private final float worldH;

    private ShapeRenderer sr;
    private SpriteBatch batch;
    private BitmapFont bigFont;
    private BitmapFont medFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    private OrthographicCamera camera;
    private StretchViewport viewport;

    public CyberGameOverRenderer(float worldW, float worldH) {
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
        bigFont = FontManager.create(3f);
        medFont = FontManager.create(1.2f);
        smallFont = FontManager.create(1.0f);
        layout = new GlyphLayout();
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
    }

    public void render(float stateTime, int level) {
        if (sr == null || batch == null || camera == null) return;

        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.05f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float flicker = 0.3f + 0.2f * (float)Math.sin(stateTime * 12f);
        sr.begin(ShapeType.Filled);
        sr.setColor(flicker * 0.5f, 0f, 0f, 1f);
        sr.rect(0, 0, worldW, worldH);
        sr.end();

        batch.begin();
        bigFont.setColor(1f, 0.1f, 0.1f, 1f);
        String msg = "SYSTEM COMPROMISED";
        layout.setText(bigFont, msg);
        bigFont.draw(batch, msg, worldW / 2f - layout.width / 2f, worldH / 2f + 60f);

        medFont.setColor(Color.WHITE);
        String sub = "No integrity lives remained on Level " + level + ".";
        layout.setText(medFont, sub);
        medFont.draw(batch, sub, worldW / 2f - layout.width / 2f, worldH / 2f);

        smallFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        smallFont.draw(batch, "[SPACE] / [E] Retry current level     [ESC] Mission Select",
            worldW / 2f - 250f, worldH / 2f - 50f);
        batch.end();
    }

    public void dispose() {
        try {
            if (sr != null) sr.dispose();
            if (batch != null) batch.dispose();
            if (bigFont != null) bigFont.dispose();
            if (medFont != null) medFont.dispose();
            if (smallFont != null) smallFont.dispose();
        } finally {
            sr = null;
            batch = null;
            bigFont = null;
            medFont = null;
            smallFont = null;
            layout = null;
            camera = null;
            viewport = null;
        }
    }
}

