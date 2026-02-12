package io.github.INF1009_P10_Team7.engine.scene;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.entity.EntityDefinition;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;

/**
 * SettingsScene (clean UI + stable resize + clickable button + working volume control)
 */
public class SettingsScene extends Scene {

    private final Scene previousScene;

    // Render/Camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shape;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    // ECS
    @SuppressWarnings("unused")
    private Map<String, GameEntity> entities;

    // "volume" demo (0..1) - matches AudioOutput default of 0.4 (40%)
    private float volume01;

    // UI rects (world coords)
    private float panelX, panelY, panelW, panelH;
    private float sliderX, sliderY, sliderW, sliderH;
    private float btnX, btnY, btnW, btnH;

    // Virtual resolution for stable layout
    private static final float VW = 800f;
    private static final float VH = 600f;

    public SettingsScene(SceneManager sceneManager, Scene previousScene) {
        super(sceneManager);
        this.previousScene = previousScene;

        // Scene only DECLARES what exists
        entityDefinitions.add(new EntityDefinition.Builder(
            "SettingsTitle",
            EntityDefinition.EntityType.STATIC_OBJECT,
            new io.github.INF1009_P10_Team7.engine.utils.Vector2(0f, 0f))
            .collisionRadius(0f)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "VolumeSlider",
            EntityDefinition.EntityType.STATIC_OBJECT,
            new io.github.INF1009_P10_Team7.engine.utils.Vector2(0f, 0f))
            .collisionRadius(0f)
            .build());

        entityDefinitions.add(new EntityDefinition.Builder(
            "BackButton",
            EntityDefinition.EntityType.STATIC_OBJECT,
            new io.github.INF1009_P10_Team7.engine.utils.Vector2(0f, 0f))
            .collisionRadius(0f)
            .build());
    }

    @Override
    protected void onLoad() {
        if (camera == null) {
            camera = new OrthographicCamera();
            viewport = new FitViewport(VW, VH, camera);
            viewport.apply(true);

            // CRITICAL FIX: Force initial viewport update to prevent black screen
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }

        if (shape == null) shape = new ShapeRenderer();
        if (batch == null) batch = new SpriteBatch();
        if (font == null) font = new BitmapFont();
        if (layout == null) layout = new GlyphLayout();

        if (entityManager == null) {
            entityManager = new EntityManager(context.getEventBus());
            entities = entityManager.createEntitiesFromDefinitions(entityDefinitions, null);
        }

        volume01 = context.getAudioController().getMusicVolume();
        Gdx.app.log("AudioController", "SettingScene Loaded current volume: " + (int)(volume01 * 100) + "%");

        recalcUI(); // compute positions once

        Gdx.app.log("Scene", "SettingsScene loaded");
    }

    private void recalcUI() {
        // Panel
        panelW = 620f;
        panelH = 420f;
        panelX = (VW - panelW) / 2f;
        panelY = (VH - panelH) / 2f;

        // Slider
        sliderW = 460f;
        sliderH = 16f;
        sliderX = panelX + 80f;
        sliderY = panelY + panelH - 120f;

        // Button
        btnW = 260f;
        btnH = 56f;
        btnX = panelX + (panelW - btnW) / 2f;
        btnY = panelY + 90f;
    }

    @Override
    protected void onUpdate(float delta) {
        if (entityManager != null) entityManager.updateAll(delta);

        // Keyboard return (your existing action)
        if (context.getInputController().isActionJustPressed("BACK")) {
            Gdx.app.log("InputController", "Key binded to 'BACK' action was pressed");
            sceneManager.requestScene(previousScene);
            return;
        }

        // Optional: ENTER also returns
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            sceneManager.requestScene(previousScene);
            return;
        }

        // Store previous volume to detect changes
        float previousVolume = volume01;

        // Optional: slider control with arrows (doesn't break if you don't use it)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) volume01 -= delta * 0.6f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) volume01 += delta * 0.6f;
        volume01 = MathUtils.clamp(volume01, 0f, 1f);

        // Mouse click handling for BACK button
        if (Gdx.input.justTouched()) {
            Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (contains(world.x, world.y, btnX, btnY, btnW, btnH)) {
                sceneManager.requestScene(previousScene);
            }

            // Click on slider to set volume
            if (contains(world.x, world.y, sliderX, sliderY - 12f, sliderW, sliderH + 24f)) {
                float t = (world.x - sliderX) / sliderW;
                volume01 = MathUtils.clamp(t, 0f, 1f);
            }
        }

        if (Math.abs(volume01 - previousVolume) > 0.001f) {
            updateGameVolume();
        }
    }

    /**
     * Sends volume change event to the audio system
     */
    private void updateGameVolume() {
    	
    	context.getAudioController().setMusicVolume(volume01);
        context.getAudioController().setSoundVolume(volume01);

        Gdx.app.log("SettingsScene", "Volume updated to: " + (int)(volume01 * 100) + "%");
    }

    private boolean contains(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    protected void onRender() {
        ScreenUtils.clear(0.08f, 0.09f, 0.11f, 1f);

        viewport.apply();
        camera.update();

        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // ===== Shapes (filled) =====
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Panel background
        shape.setColor(0.13f, 0.14f, 0.17f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);

        // Header strip
        shape.setColor(0.18f, 0.19f, 0.23f, 1f);
        shape.rect(panelX, panelY + panelH - 80f, panelW, 80f);

        // Slider track
        shape.setColor(0.10f, 0.11f, 0.14f, 1f);
        shape.rect(sliderX, sliderY, sliderW, sliderH);

        // Slider fill
        shape.setColor(0.35f, 0.60f, 0.95f, 1f);
        shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);

        // Knob
        float knobX = sliderX + sliderW * volume01;
        float knobY = sliderY + sliderH / 2f;
        shape.setColor(0.92f, 0.92f, 0.95f, 1f);
        shape.circle(knobX, knobY, 14f);

        // Back button
        // Slight "hover" effect
        Vector2 worldMouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hover = contains(worldMouse.x, worldMouse.y, btnX, btnY, btnW, btnH);

        if (hover) shape.setColor(0.23f, 0.24f, 0.29f, 1f);
        else shape.setColor(0.18f, 0.19f, 0.23f, 1f);

        shape.rect(btnX, btnY, btnW, btnH);

        shape.end();

        // ===== Outlines =====
        shape.begin(ShapeRenderer.ShapeType.Line);

        shape.setColor(0.30f, 0.32f, 0.38f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);
        shape.rect(btnX, btnY, btnW, btnH);

        shape.setColor(0.25f, 0.27f, 0.32f, 1f);
        shape.rect(sliderX, sliderY, sliderW, sliderH);

        shape.end();

        // ===== Text =====
        batch.begin();

        // Title
        font.getData().setScale(1.6f);
        font.setColor(0.95f, 0.95f, 0.98f, 1f);
        drawLeft("SETTINGS", panelX + 28f, panelY + panelH - 26f);

        // Volume label + percent aligned cleanly
        font.getData().setScale(1.0f);
        font.setColor(0.80f, 0.82f, 0.88f, 1f);
        drawLeft("VOLUME", sliderX, sliderY + 44f);

        int pct = Math.round(volume01 * 100f);
        font.setColor(0.60f, 0.62f, 0.70f, 1f);
        drawRight(pct + "%", sliderX + sliderW, sliderY + 44f);

        // Button text centered
        font.setColor(0.92f, 0.92f, 0.95f, 1f);
        drawCentered("BACK", btnX + btnW / 2f, btnY + 36f);

        // Hint - updated to show arrow keys work too
        font.setColor(0.60f, 0.62f, 0.70f, 1f);
        drawLeft("BACKSPACE or ENTER to return • Arrow keys to adjust volume", panelX + 28f, panelY + 28f);

        batch.end();
    }

    private void drawLeft(String text, float x, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, x, y);
    }

    private void drawRight(String text, float xRight, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, xRight - layout.width, y);
    }

    private void drawCentered(String text, float cx, float y) {
        layout.setText(font, text);
        font.draw(batch, layout, cx - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "SettingsScene unloading...");
        dispose();
    }

    @Override
    protected void onDispose() {
        if (shape != null) { shape.dispose(); shape = null; }
        if (batch != null) { batch.dispose(); batch = null; }
        if (font != null) { font.dispose(); font = null; }
        if (entityManager != null) { entityManager.dispose(); entityManager = null; }
        Gdx.app.log("Scene", "SettingsScene diposed");
    }
}
