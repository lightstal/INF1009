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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.UIManagement.KeyBindingButton;
import io.github.INF1009_P10_Team7.engine.UIManagement.UIElement;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * <p>Settings screen with a volume slider and key rebinding buttons.
 * Pushed on top of the current scene as an overlay.</p>
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

    // create for button ui
    private Skin skin;
    private TextButton backButton;
    private Stage stage;
    private UIElement uiElement;;
    private List<KeyBindingButton> keyBindingButtons;

    // Back button layout
    private static final float BACK_BUTTON_WIDTH = 260f;
    private static final float BACK_BUTTON_HEIGHT = 56f;
    private static final float BACK_BUTTON_Y = 120f;

    // Key binding buttons layout
    private static final float KEY_BUTTON_WIDTH = 140f;
    private static final float KEY_BUTTON_HEIGHT = 35f;
    private static final float KEY_HORIZONTAL_GAP = 60f;
    private static final float KEY_VERTICAL_GAP = 55f;
    private static final float KEY_START_Y_OFFSET = 20f;

    public SettingsScene(IInputController input, IAudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;

    }

    /** <p>Sets up camera, rendering resources, volume, UI layout, and buttons.</p> */
    @Override
    protected void onLoad() {
        // Initialization
        initializeCamera();
        initializeRendering();
        initializeVolume();
        recalcUI();
        initializeStage();
        initializeUI();

        Gdx.app.log("Scene", "SettingsScene loaded");
    }

    /** <p>Sets up the camera and viewport.</p> */
    private void initializeCamera() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VW, VH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        camera.position.set(VW / 2f, VH / 2f, 0);
        camera.update();
    }

    /** <p>Creates the shape renderer, sprite batch, and font if not already created.</p> */
    private void initializeRendering() {
        if (shape == null) {
            shape = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont();
            layout = new GlyphLayout();
        }
    }

    /** <p>Reads the current music volume from the audio controller.</p> */
    private void initializeVolume() {
        volume01 = audio.getMusicVolume();
        Gdx.app.log("AudioController", "SettingsScene current music volume: " + (int) (volume01 * 100) + "%");
    }

    /** <p>Calculates positions and sizes for the panel, slider, and back button.</p> */
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

        // Back button
        btnW = BACK_BUTTON_WIDTH;
        btnH = BACK_BUTTON_HEIGHT;
        btnX = (VW - btnW) / 2f;
        btnY = BACK_BUTTON_Y;
    }

    /** <p>Creates the Stage for UI components.</p> */
    private void initializeStage() {
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
    }

    /** <p>Loads the skin and creates the back button and key binding buttons.</p> */
    private void initializeUI() {
        try {
            Skin skin = new Skin(Gdx.files.internal("buttons/name2d.json"));
            uiElement = new UIElement(skin, true);

            createBackButton();
            createKeyBindings();

            Gdx.app.log("SettingsScene", "UI initialized");
        } catch (Exception e) {
            Gdx.app.error("SettingsScene", "Failed to load UI", e);
        }
    }

    /** <p>Creates the back button that pops this scene.</p> */
    private void createBackButton() {
        backButton = uiElement.createButton(
            "BACK",
            BACK_BUTTON_WIDTH,
            BACK_BUTTON_HEIGHT,
            () -> nav.popScene());

        backButton.setPosition(btnX, btnY);
        stage.addActor(backButton);
    }

    /** <p>Creates key binding buttons for LEFT, RIGHT, UP, DOWN, and SHOOT.</p> */
    private void createKeyBindings() {
        keyBindingButtons = new java.util.ArrayList<>();
        KeyBindingPositions positions = calculateKeyBindingPositions();

        // Row 1: LEFT and RIGHT
        addKeyBinding("LEFT", positions.leftX, positions.row1Y);
        addKeyBinding("RIGHT", positions.rightX, positions.row1Y);

        // Row 2: UP and DOWN
        addKeyBinding("UP", positions.leftX, positions.row2Y);
        addKeyBinding("DOWN", positions.rightX, positions.row2Y);
        // Row 3: SHOOT (centered)
        addKeyBinding("SHOOT", positions.centerX - (KEY_BUTTON_WIDTH / 2f), positions.row3Y);
    }

    /** <p>Calculates the x/y positions for each row and column of key binding buttons.</p> */
    private KeyBindingPositions calculateKeyBindingPositions() {
        float centerX = VW / 2f;
        float centerY = VH / 2f;

        // Calculate column positions
        float leftX = centerX - KEY_HORIZONTAL_GAP - KEY_BUTTON_WIDTH;
        float rightX = centerX + KEY_HORIZONTAL_GAP;

        // Calculate row positions
        float row1Y = centerY + KEY_START_Y_OFFSET;
        float row2Y = row1Y - KEY_VERTICAL_GAP;
        float row3Y = row2Y - KEY_VERTICAL_GAP;

        return new KeyBindingPositions(centerX, leftX, rightX, row1Y, row2Y, row3Y);
    }

    /**
     * <p>Holds the calculated positions for key binding button layout.</p>
     */
    private static class KeyBindingPositions {
        final float centerX;
        final float leftX;
        final float rightX;
        final float row1Y;
        final float row2Y;
        final float row3Y;

        /**
         * @param centerX center X position
         * @param leftX   left column X
         * @param rightX  right column X
         * @param row1Y   first row Y
         * @param row2Y   second row Y
         * @param row3Y   third row Y
         */
        KeyBindingPositions(float centerX, float leftX, float rightX,
                            float row1Y, float row2Y, float row3Y) {
            this.centerX = centerX;
            this.leftX = leftX;
            this.rightX = rightX;
            this.row1Y = row1Y;
            this.row2Y = row2Y;
            this.row3Y = row3Y;
        }
    }

    /**
     * <p>Creates a single key binding button and adds it to the stage.</p>
     *
     * @param action the action name to bind
     * @param x      x position
     * @param y      y position
     */
    private void addKeyBinding(String action, float x, float y) {
        KeyBindingButton button = uiElement.createKeyBindingButton(
            action,
            KEY_BUTTON_WIDTH,
            KEY_BUTTON_HEIGHT,
            input);

        button.setPosition(x, y);
        stage.addActor(button);
        keyBindingButtons.add(button);
    }

    /**
     * <p>Handles volume slider input (arrow keys and click) and
     * keyboard shortcuts each frame.</p>
     *
     * @param delta time since last frame in seconds
     */
    @Override
    protected void onUpdate(float delta) {
        handleKeyboardShortcuts();

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

    }

    /** <p>Pops the scene when ENTER is pressed.</p> */
    private void handleKeyboardShortcuts() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            nav.popScene();
        }
    }

    /** <p>Checks if a point is inside a rectangle.</p> */
    private boolean contains(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    /** <p>Draws the settings panel, slider, text, and UI buttons.</p> */
    @Override
    protected void onRender() {
        if (camera == null || viewport == null)
            return;

        prepareRender();
        renderShapes();
        renderText();
        renderStage();
    }

    /** <p>Applies the viewport, clears the screen, and sets projection matrices.</p> */
    private void prepareRender() {
        // CRITICAL ORDER: Apply viewport FIRST, then clear, then draw!
        viewport.apply();
        camera.update();

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    /** <p>Draws the panel background, header, slider track, fill, and knob.</p> */
    private void renderShapes() {
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
        shape.setColor(0.30f, 0.75f, 0.45f, 1f);
        shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);

        // Slider knob
        float knobX = sliderX + sliderW * volume01;
        shape.setColor(0.90f, 0.90f, 0.95f, 1f);
        shape.circle(knobX, sliderY + sliderH / 2f, 12f);

        shape.end();
    }

    /** <p>Draws the title, volume label, and percentage text.</p> */
    private void renderText() {
        batch.begin();

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

        batch.end();
    }

    /** <p>Updates and draws the Stage (buttons).</p> */
    private void renderStage() {
        if (stage != null) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
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

    /** <p>Returns true so the engine pauses world updates while settings is open.</p> */
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
        if (skin != null)
            skin.dispose();
        if (stage != null)
            stage.dispose();
        Gdx.app.log("Scene", "SettingsScene disposed");
    }

}
