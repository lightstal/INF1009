package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

class CyberGameOverScene extends Scene {

    private final CyberSceneFactory factory;
    private final int level;
    private ShapeRenderer  sr;
    private SpriteBatch    batch;
    private BitmapFont     bigFont, medFont, smallFont;
    private GlyphLayout    layout;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private float stateTime = 0f;

    CyberGameOverScene(IInputController input, IAudioController audio,
                       SceneNavigator nav, CyberSceneFactory factory, int level) {
        super(input, audio, nav);
        this.factory = factory;
        this.level = level;
    }

    @Override
    protected void onLoad() {
        camera   = new OrthographicCamera();
        viewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(TileMap.WORLD_W / 2f, TileMap.WORLD_H / 2f, 0);
        camera.update();
        sr     = new ShapeRenderer();
        batch  = new SpriteBatch();
        bigFont   = makeFont(3f);
        medFont   = makeFont(1.2f);
        smallFont = makeFont(1.0f);
        layout = new GlyphLayout();
        audio.stopMusic();
    }

    private BitmapFont makeFont(float s) {
        return FontManager.create(s);
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        Gdx.input.setInputProcessor(null);
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createGameScene(level));
        }
        if (input.isActionJustPressed("MENU_BACK")) {
            nav.requestScene(factory.createLevelSelectScene());
        }
    }

    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.05f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float flicker = 0.3f + 0.2f * (float) Math.sin(stateTime * 12f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(flicker * 0.5f, 0f, 0f, 1f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
        sr.end();

        batch.begin();
        bigFont.setColor(1f, 0.1f, 0.1f, 1f);
        String msg = "SYSTEM COMPROMISED";
        layout.setText(bigFont, msg);
        bigFont.draw(batch, msg, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f + 60f);

        medFont.setColor(Color.WHITE);
        String sub = "No integrity lives remained on Level " + level + ".";
        layout.setText(medFont, sub);
        medFont.draw(batch, sub, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f);

        smallFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        smallFont.draw(batch, "[SPACE] / [E] Retry current level     [ESC] Mission Select",
            TileMap.WORLD_W / 2f - 250f, TileMap.WORLD_H / 2f - 50f);
        batch.end();
    }

    @Override public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }
    @Override protected void onUnload()  { }
    @Override protected void onDispose() {
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (bigFont != null) bigFont.dispose();
        if (medFont != null) medFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}

class CyberVictoryScene extends Scene {

    private final CyberSceneFactory factory;
    private final int keysCollected;
    private final int keysRequired;
    private final int missionTimeSeconds;
    private final int level;
    private final int respawnsUsed;
    private final int hintsUsed;

    private ShapeRenderer  sr;
    private SpriteBatch    batch;
    private BitmapFont     bigFont, medFont, smallFont;
    private GlyphLayout    layout;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private float stateTime = 0f;

    CyberVictoryScene(IInputController input, IAudioController audio,
                      SceneNavigator nav, CyberSceneFactory factory,
                      int keysCollected, int keysRequired, int timeRemaining, int level,
                      int respawnsUsed, int hintsUsed) {
        super(input, audio, nav);
        this.factory        = factory;
        this.keysCollected  = keysCollected;
        this.keysRequired   = keysRequired;
        this.missionTimeSeconds = timeRemaining;
        this.level          = level;
        this.respawnsUsed   = respawnsUsed;
        this.hintsUsed      = hintsUsed;
    }

    @Override
    protected void onLoad() {
        camera   = new OrthographicCamera();
        viewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(TileMap.WORLD_W / 2f, TileMap.WORLD_H / 2f, 0);
        camera.update();
        sr     = new ShapeRenderer();
        batch  = new SpriteBatch();
        bigFont   = makeFont(2.8f);
        medFont   = makeFont(1.3f);
        smallFont = makeFont(0.95f);
        layout = new GlyphLayout();
        audio.stopMusic();
        audio.playSound("audio/bell.mp3");
    }

    private BitmapFont makeFont(float s) {
        return FontManager.create(s);
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        Gdx.input.setInputProcessor(null);
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createLevelSelectScene());
        }
        if (input.isActionJustPressed("MENU_BACK")) {
            nav.requestScene(factory.createMainMenuScene());
        }
    }

    private int getScore() {
        int timeBonus = Math.max(0, 2400 - missionTimeSeconds * 8);
        int score = keysCollected * 1000 + timeBonus + level * 150;
        score -= respawnsUsed * 300;
        score -= hintsUsed * 180;
        return Math.max(0, score);
    }

    private String getRank() {
        int score = getScore();
        if (score >= 5600) return "S";
        if (score >= 4700) return "A";
        if (score >= 3800) return "B";
        if (score >= 3000) return "C";
        return "D";
    }

    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float pulse = 0.4f + 0.4f * (float) Math.sin(stateTime * 3f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(pulse * 0.3f, 0f, pulse * 0.5f, 1f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);

        for (int i = 0; i < 60; i++) {
            float sx = (float)((Math.sin(i * 137.5f + stateTime) * 0.5f + 0.5f) * TileMap.WORLD_W);
            float sy = (float)((Math.cos(i * 73.1f + stateTime * 0.8f) * 0.5f + 0.5f) * TileMap.WORLD_H);
            float sp = 0.5f + 0.5f * (float) Math.sin(stateTime * 2f + i);
            sr.setColor(sp, 0f, 1f, sp * 0.7f);
            sr.circle(sx, sy, 3f + 2f * sp, 8);
        }
        sr.end();

        batch.begin();
        float c = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f);
        bigFont.setColor(c, 0f, 1f, 1f);
        String msg = "BREACH SUCCESSFUL";
        layout.setText(bigFont, msg);
        bigFont.draw(batch, msg, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f + 92f);

        medFont.setColor(Color.YELLOW);
        medFont.draw(batch, "Terminals hacked : " + keysCollected + " / " + keysRequired,
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f + 28f);
        medFont.draw(batch, "Mission time     : " + missionTimeSeconds + "s",
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f - 8f);

        medFont.setColor(Color.WHITE);
        medFont.draw(batch, "Respawns used    : " + respawnsUsed,
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f - 44f);
        medFont.draw(batch, "Signal pings used: " + hintsUsed,
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f - 80f);

        String rank = getRank();
        bigFont.setColor(rank.equals("S") ? new Color(1f, 0.84f, 0f, 1f) : new Color(0.8f, 0.9f, 1f, 1f));
        layout.setText(bigFont, "RANK " + rank);
        bigFont.draw(batch, "RANK " + rank, TileMap.WORLD_W / 2f - layout.width / 2f, TileMap.WORLD_H / 2f - 150f);

        smallFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        smallFont.draw(batch, "SCORE: " + getScore(), TileMap.WORLD_W / 2f - 60f, TileMap.WORLD_H / 2f - 190f);
        smallFont.draw(batch, "Faster clears improve rank, but lives now matter more than the old timer.",
            TileMap.WORLD_W / 2f - 250f, TileMap.WORLD_H / 2f - 208f);
        smallFont.draw(batch, "[SPACE] Level Select     [ESC] Main Menu",
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f - 230f);
        batch.end();
    }

    @Override public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }
    @Override protected void onUnload()  { }
    @Override protected void onDispose() {
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (bigFont != null) bigFont.dispose();
        if (medFont != null) medFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}
