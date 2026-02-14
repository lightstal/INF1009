package io.github.INF1009_P10_Team7.simulation;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputBindElement;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * SettingsScene (UI-only)
 */
public class SettingsScene extends Scene {

    private final SceneFactory factory;

    // Render/Camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shape;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    // Volume demo (0..1)
    private float volume01;

    // UI rects (world coords)
    private float panelX, panelY, panelW, panelH;
    private float sliderX, sliderY, sliderW, sliderH;
    private float btnX, btnY, btnW, btnH;

    // FIXED virtual resolution - NEVER CHANGES!
    private static final float VW = 800f;
    private static final float VH = 600f;

    // For key binding
    private boolean isRebinding = false;
    private String actionToRebind = null;
    private List<InputBindElement> inputBindElements;

    public SettingsScene(InputController input, AudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;

    }

    @Override
    protected void onLoad() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VW, VH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        if (shape == null) {
            shape = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont();
            layout = new GlyphLayout();
        }

        camera.position.set(VW / 2f, VH / 2f, 0);
        camera.update();

        volume01 = audio.getMusicVolume();
        Gdx.app.log("AudioController", "SettingsScene current music volume: " + (int) (volume01 * 100) + "%");

        recalcUI();
        Gdx.app.log("Scene", "SettingsScene loaded");
        Gdx.app.log("SettingsScene", "World locked at: " + VW + "x" + VH);
    }

    private void recalcUI() {
        panelW = 620f;
        panelH = 420f;
        panelX = (VW - panelW) / 2f;
        panelY = (VH - panelH) / 2f;

        sliderW = 460f;
        sliderH = 16f;
        sliderX = panelX + 80f;
        sliderY = panelY + panelH - 120f;

        btnW = 260f;
        btnH = 56f;
        btnX = panelX + (panelW - btnW) / 2f;
        btnY = panelY + 90f;

        // Dimensions
        float startX = VW / 2; // center of screen
        float startY = 350; // height

        // Local variables specifically for keys
        float keyW = 140f;
        float keyH = 35f;
        float gap = 30f;

        // initializing from inputbindelement by oop
        inputBindElements = new java.util.ArrayList<>();

        // addKey, to seperate from calling inputBindElements.add(new
        // InputBindElement()) alot of times
        // // row 1 --- left and right
        addKey("LEFT", startX - keyW - gap, startY, keyW, keyH);
        addKey("RIGHT", startX + gap, startY, keyW, keyH);

        // // row 2 --- up and down
        float row2Y = startY - 60f;
        addKey("UP", startX - keyW - gap, row2Y, keyW, keyH);
        addKey("DOWN", startX + gap, row2Y, keyW, keyH);

        // // row 3 --- shoot
        float row3Y = row2Y - 60f;
        addKey("SHOOT", startX - (keyW / 2), row3Y, keyW, keyH);

    }

    @Override
    protected void onUpdate(float delta) {
        // Use action binding for BACK
        if (input.isActionJustPressed("BACK")) {
            nav.popScene();
            return;
        }

        // Optional: ENTER also returns
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            nav.popScene();
            return;
        }

        float previousVolume = volume01;

        // Optional: slider control with arrows
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            volume01 -= delta * 0.6f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            volume01 += delta * 0.6f;
        volume01 = MathUtils.clamp(volume01, 0f, 1f);

        if (Gdx.input.justTouched()) {
            Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            if (contains(world.x, world.y, btnX, btnY, btnW, btnH)) {
                nav.popScene();
                return;
            }

            if (contains(world.x, world.y, sliderX, sliderY - 12f, sliderW, sliderH + 24f)) {
                float t = (world.x - sliderX) / sliderW;
                volume01 = MathUtils.clamp(t, 0f, 1f);
            }
        }

        if (Math.abs(volume01 - previousVolume) > 0.001f) {
            audio.setMusicVolume(volume01);
            audio.setSoundVolume(volume01);
            Gdx.app.log("SettingsScene", "Volume updated to: " + (int) (volume01 * 100) + "%");
        }

        // for key binding, when touch on the binded key
        if (Gdx.input.justTouched()) {
            Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            // SRP to check by list that wants to bind
            if (!isRebinding) {
                for (InputBindElement element : inputBindElements) {
                    if (element.isClicked(mouse)) {
                        startRebinding(element.getActionName());
                        break;
                    }
                }
            }
        }
    }

    // for binding
    private void startRebinding(String action) {
        isRebinding = true;
        actionToRebind = action;

        // oop to encapsulate 'this' (the scene instance) to the static handler
        input.listenForNextKey(new RebindHandler(this, action));
    }

    // to clean after binding
    private void stopRebinding() {
        isRebinding = false;
        actionToRebind = null;
    }

    private boolean contains(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    protected void onRender() {
        if (camera == null || viewport == null)
            return;

        // CRITICAL ORDER: Apply viewport FIRST, then clear, then draw!
        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shape.begin(ShapeRenderer.ShapeType.Filled);

        // draw key background for keybind
        for (InputBindElement element : inputBindElements) {
            boolean waiting = isRebinding && element.getActionName().equals(actionToRebind);
            element.drawShape(shape, waiting);
        }

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
        shape.setColor(0.30f, 0.75f, 0.45f, 1f);
        shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);

        // Slider knob
        float knobX = sliderX + sliderW * volume01;
        shape.setColor(0.90f, 0.90f, 0.95f, 1f);
        shape.circle(knobX, sliderY + sliderH / 2f, 12f);

        // Back button
        shape.setColor(0.25f, 0.28f, 0.34f, 1f);
        shape.rect(btnX, btnY, btnW, btnH);

        shape.end();

        batch.begin();

        // draw key text for keybind
        for (InputBindElement element : inputBindElements) { // Note: Variable name is same as class (should be
                                                             // inputBindElements)
            boolean waiting = isRebinding && element.getActionName().equals(actionToRebind);

            element.drawText(batch, font, input, waiting);
        }

        // Title
        font.getData().setScale(2.0f);
        layout.setText(font, "SETTINGS");
        font.draw(batch, layout,
                panelX + (panelW - layout.width) / 2f,
                panelY + panelH - 28f);

        // Volume label
        font.getData().setScale(1.3f);
        layout.setText(font, "VOLUME");
        font.draw(batch, layout, sliderX, sliderY + 60f);

        // Percentage
        font.getData().setScale(1.1f);
        String percent = (int) (volume01 * 100) + "%";
        layout.setText(font, percent);
        font.draw(batch, layout, sliderX + sliderW - layout.width, sliderY + 60f);

        // Back button text
        font.getData().setScale(1.2f);
        layout.setText(font, "BACK");
        font.draw(batch, layout,
                btnX + (btnW - layout.width) / 2f,
                btnY + (btnH + layout.height) / 2f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "SettingsScene resize: " + width + "x" + height);

        if (viewport != null) {
            viewport.update(width, height, true);
        }

        recalcUI();
        Gdx.app.log("Scene", "World size locked at: " + VW + "x" + VH);
    }

    @Override
    public boolean blocksWorldUpdate() {
        return true; // pause world while settings is open
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "SettingsScene unloading...");
    }

    @Override
    protected void onDispose() {
        if (shape != null)
            shape.dispose();
        if (batch != null)
            batch.dispose();
        if (font != null)
            font.dispose();
        Gdx.app.log("Scene", "SettingsScene disposed");
    }

    //
    private void addKey(String action, float x, float y, float w, float h) {
        inputBindElements.add(new InputBindElement(action, x, y, w, h));
    }

    /**
     * Implementing SRP and Encapsulation.
     * to hold references and separate by state
     */
    private class RebindHandler implements InputController.InputCallback {

        private final SettingsScene scene;
        private final String action;

        public RebindHandler(SettingsScene scene, String action) {
            this.scene = scene;
            this.action = action;
        }

        @Override
        public void onInputReceived(int keycode) {
            // Perform Binding
            scene.input.bindKey(this.action, keycode);

            Gdx.app.log("Settings", "Rebound " + this.action + " to " + keycode);

            // Reset UI State
            stopRebinding();
        }
    }
}
