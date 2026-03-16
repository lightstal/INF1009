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

/**
 * Game-over screen: shown when the drone catches the player or time runs out.
 */
class CyberGameOverScene extends Scene {

    private final CyberSceneFactory factory;
    private ShapeRenderer  sr;
    private SpriteBatch    batch;
    private BitmapFont     font;
    private GlyphLayout    layout;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private float stateTime = 0f;

    CyberGameOverScene(IInputController input, IAudioController audio,
                       SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
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
        font   = new BitmapFont();
        layout = new GlyphLayout();
        audio.stopMusic();
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        Gdx.input.setInputProcessor(null);
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createGameScene());
        }
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene());
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

        // Flickering red vignette
        float flicker = 0.3f + 0.2f * (float) Math.sin(stateTime * 12f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(flicker * 0.5f, 0f, 0f, 1f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
        sr.end();

        batch.begin();
        font.getData().setScale(3f);
        font.setColor(1f, 0.1f, 0.1f, 1f);
        String msg = "SYSTEM COMPROMISED";
        layout.setText(font, msg);
        font.draw(batch, msg, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f + 60f);

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        String sub = "The Hunter Drone detected your intrusion.";
        layout.setText(font, sub);
        font.draw(batch, sub, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f);

        font.getData().setScale(1.0f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(batch, "[SPACE] / [E] Retry     [BACKSPACE] Main Menu",
            TileMap.WORLD_W / 2f - 170f, TileMap.WORLD_H / 2f - 50f);
        batch.end();
    }

    @Override public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }
    @Override protected void onUnload()  { }
    @Override protected void onDispose() {
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}

/**
 * Victory screen: shown when the player escapes through the exit door.
 */
class CyberVictoryScene extends Scene {

    private final CyberSceneFactory factory;
    private final int keysCollected;
    private final int timeRemaining;
    private final int level;

    private ShapeRenderer  sr;
    private SpriteBatch    batch;
    private BitmapFont     font;
    private GlyphLayout    layout;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private float stateTime = 0f;

    CyberVictoryScene(IInputController input, IAudioController audio,
                      SceneNavigator nav, CyberSceneFactory factory,
                      int keysCollected, int timeRemaining, int level) {
        super(input, audio, nav);
        this.factory        = factory;
        this.keysCollected  = keysCollected;
        this.timeRemaining  = timeRemaining;
        this.level          = level;
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
        font   = new BitmapFont();
        layout = new GlyphLayout();
        audio.stopMusic();
        audio.playSound("bell.mp3");
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        Gdx.input.setInputProcessor(null);
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createLevelSelectScene());
        }
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene());
        }
    }

    @Override
    protected void onRender() {
        viewport.apply();
        camera.update();
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Pulsing magenta/purple glow
        float pulse = 0.4f + 0.4f * (float) Math.sin(stateTime * 3f);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(pulse * 0.3f, 0f, pulse * 0.5f, 1f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);

        // Particle stars
        for (int i = 0; i < 60; i++) {
            float sx = (float)((Math.sin(i * 137.5f + stateTime) * 0.5f + 0.5f) * TileMap.WORLD_W);
            float sy = (float)((Math.cos(i * 73.1f + stateTime * 0.8f) * 0.5f + 0.5f) * TileMap.WORLD_H);
            float sp = 0.5f + 0.5f * (float) Math.sin(stateTime * 2f + i);
            sr.setColor(sp, 0f, 1f, sp * 0.7f);
            sr.circle(sx, sy, 3f + 2f * sp, 8);
        }
        sr.end();

        batch.begin();
        font.getData().setScale(2.8f);
        float c = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f);
        font.setColor(c, 0f, 1f, 1f);
        String msg = "BREACH SUCCESSFUL";
        layout.setText(font, msg);
        font.draw(batch, msg, TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H / 2f + 80f);

        font.getData().setScale(1.3f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Terminals hacked : " + keysCollected + " / " + (level == 2 ? 4 : 3),
            TileMap.WORLD_W / 2f - 130f, TileMap.WORLD_H / 2f + 20f);
        font.draw(batch, "Time remaining   : " + timeRemaining + "s",
            TileMap.WORLD_W / 2f - 130f, TileMap.WORLD_H / 2f - 10f);

        int score = keysCollected * 1000 + timeRemaining * 10;
        font.getData().setScale(1.6f);
        font.setColor(Color.WHITE);
        font.draw(batch, "SCORE: " + score,
            TileMap.WORLD_W / 2f - 80f, TileMap.WORLD_H / 2f - 55f);

        font.getData().setScale(0.95f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(batch, "[SPACE] Play Again     [BACKSPACE] Main Menu",
            TileMap.WORLD_W / 2f - 165f, TileMap.WORLD_H / 2f - 100f);
        batch.end();
    }

    @Override public void resize(int w, int h) { if (viewport != null) viewport.update(w, h, true); }
    @Override protected void onUnload()  { }
    @Override protected void onDispose() {
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
