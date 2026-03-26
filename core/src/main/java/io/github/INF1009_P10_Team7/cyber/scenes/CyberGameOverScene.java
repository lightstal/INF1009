package io.github.INF1009_P10_Team7.cyber.scenes;

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
import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.TileMap;
import io.github.INF1009_P10_Team7.cyber.FontManager;

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
