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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.INF1009_P10_Team7.engine.render.FontManager;

public class CyberVictoryRenderer {
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

    public CyberVictoryRenderer(float worldW, float worldH) {
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
        bigFont = FontManager.create(2.8f);
        medFont = FontManager.create(1.3f);
        smallFont = FontManager.create(0.95f);
        layout = new GlyphLayout();
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
    }

    public void render(float stateTime,
                       int keysCollected, int keysRequired,
                       int missionTimeSeconds, int level,
                       int respawnsUsed, int hintsUsed,
                       int score, String rank) {
        if (sr == null || batch == null || camera == null) return;

        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float pulse = 0.4f + 0.4f * (float)Math.sin(stateTime * 3f);
        sr.begin(ShapeType.Filled);
        sr.setColor(pulse * 0.3f, 0f, pulse * 0.5f, 1f);
        sr.rect(0, 0, worldW, worldH);

        for (int i = 0; i < 60; i++) {
            float sx = (float)((Math.sin(i * 137.5f + stateTime) * 0.5f + 0.5f) * worldW);
            float sy = (float)((Math.cos(i * 73.1f + stateTime * 0.8f) * 0.5f + 0.5f) * worldH);
            float sp = 0.5f + 0.5f * (float)Math.sin(stateTime * 2f + i);
            sr.setColor(sp, 0f, 1f, sp * 0.7f);
            sr.circle(sx, sy, 3f + 2f * sp, 8);
        }
        sr.end();

        batch.begin();
        float c = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f);
        bigFont.setColor(c, 0f, 1f, 1f);
        String msg = "BREACH SUCCESSFUL";
        layout.setText(bigFont, msg);
        bigFont.draw(batch, msg, worldW / 2f - layout.width / 2f, worldH / 2f + 92f);

        // Use a fixed centered text column so the whole block reads centered.
        float colW = Math.min(980f, worldW - 120f);
        float colX = worldW / 2f - colW / 2f;

        // Center the stats block lines (within the column).
        String l1 = "Terminals hacked : " + keysCollected + " / " + keysRequired;
        String l2 = "Mission time     : " + missionTimeSeconds + "s";
        String l3 = "Respawns used    : " + respawnsUsed;
        String l4 = "Signal pings used: " + hintsUsed;

        medFont.setColor(Color.YELLOW);
        layout.setText(medFont, l1, medFont.getColor(), colW, Align.center, false);
        medFont.draw(batch, layout, colX, worldH / 2f + 28f);
        layout.setText(medFont, l2, medFont.getColor(), colW, Align.center, false);
        medFont.draw(batch, layout, colX, worldH / 2f - 8f);

        medFont.setColor(Color.WHITE);
        layout.setText(medFont, l3, medFont.getColor(), colW, Align.center, false);
        medFont.draw(batch, layout, colX, worldH / 2f - 44f);
        layout.setText(medFont, l4, medFont.getColor(), colW, Align.center, false);
        medFont.draw(batch, layout, colX, worldH / 2f - 80f);

        bigFont.setColor(rank.equals("S") ? new Color(1f, 0.84f, 0f, 1f) : new Color(0.8f, 0.9f, 1f, 1f));
        layout.setText(bigFont, "RANK " + rank);
        bigFont.draw(batch, "RANK " + rank,
            worldW / 2f - layout.width / 2f, worldH / 2f - 150f);

        smallFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        String scoreLine = "SCORE: " + score;
        String tipLine = "Fast clears boost score (time bonus). Each respawn or signal ping costs points.";
        String navLine = "[SPACE] Level Select     [ESC] Main Menu";
        layout.setText(smallFont, scoreLine, smallFont.getColor(), colW, Align.center, false);
        smallFont.draw(batch, layout, colX, worldH / 2f - 190f);

        // Allow the tip line to wrap so it stays centered on all resolutions.
        layout.setText(smallFont, tipLine, smallFont.getColor(), colW, Align.center, true);
        smallFont.draw(batch, layout, colX, worldH / 2f - 210f);

        layout.setText(smallFont, navLine, smallFont.getColor(), colW, Align.center, false);
        smallFont.draw(batch, layout, colX, worldH / 2f - 248f);
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

