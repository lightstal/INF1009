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
 * Pushed on top of the current scene as an overlay, so the game
 * world is paused underneath.</p>
 *
 * <p>The player can adjust volume with the slider or arrow keys,
 * rebind movement and shoot keys, and return to the previous
 * scene with the back button or ENTER key.</p>
 */
public class SettingsScene extends Scene {

    /** <p>Factory for creating scenes during navigation.</p> */
    private final SceneFactory factory;

    /** <p>Orthographic camera for 2D rendering.</p> */
    private OrthographicCamera camera;

    /** <p>Viewport that stretches the virtual resolution to fill the window.</p> */
    private Viewport viewport;

    /** <p>Shape renderer for drawing the panel, slider, and other shapes.</p> */
    private ShapeRenderer shape;

    /** <p>Sprite batch for rendering text.</p> */
    private SpriteBatch batch;

    /** <p>Font used for the title, volume label, and percentage text.</p> */
    private BitmapFont font;

    /** <p>Glyph layout used for measuring text width for centring.</p> */
    private GlyphLayout layout;

    /** <p>Current volume level as a float between 0 and 1.</p> */
    private float volume01;

    // --- UI rectangle positions and sizes (in world coordinates) ---

    /** <p>X position of the settings panel.</p> */
    private float panelX;
    /** <p>Y position of the settings panel.</p> */
    private float panelY;
    /** <p>Width of the settings panel.</p> */
    private float panelW;
    /** <p>Height of the settings panel.</p> */
    private float panelH;

    /** <p>X position of the volume slider track.</p> */
    private float sliderX;
    /** <p>Y position of the volume slider track.</p> */
    private float sliderY;
    /** <p>Width of the volume slider track.</p> */
    private float sliderW;
    /** <p>Height of the volume slider track.</p> */
    private float sliderH;

    /** <p>X position of the back button.</p> */
    private float btnX;
    /** <p>Y position of the back button.</p> */
    private float btnY;
    /** <p>Width of the back button.</p> */
    private float btnW;
    /** <p>Height of the back button.</p> */
    private float btnH;

    /** <p>Fixed virtual width for the settings screen.</p> */
    private static final float VW = 800f;

    /** <p>Fixed virtual height for the settings screen.</p> */
    private static final float VH = 600f;

    /** <p>Skin that provides styles for UI buttons.</p> */
    private Skin skin;

    /** <p>Back button that pops this scene.</p> */
    private TextButton backButton;

    /** <p>Scene2D stage that holds and manages UI actors.</p> */
    private Stage stage;

    /** <p>Helper class for creating styled UI buttons.</p> */
    private UIElement uiElement;;

    /** <p>List of key binding buttons for rebinding controls.</p> */
    private List<KeyBindingButton> keyBindingButtons;

    /** <p>Width of the back button in world units.</p> */
    private static final float BACK_BUTTON_WIDTH = 260f;

    /** <p>Height of the back button in world units.</p> */
    private static final float BACK_BUTTON_HEIGHT = 56f;

    /** <p>Y position of the back button from the bottom of the screen.</p> */
    private static final float BACK_BUTTON_Y = 120f;

    /** <p>Width of each key binding button.</p> */
    private static final float KEY_BUTTON_WIDTH = 140f;

    /** <p>Height of each key binding button.</p> */
    private static final float KEY_BUTTON_HEIGHT = 35f;

    /** <p>Horizontal gap between left and right columns of key binding buttons.</p> */
    private static final float KEY_HORIZONTAL_GAP = 60f;

    /** <p>Vertical gap between rows of key binding buttons.</p> */
    private static final float KEY_VERTICAL_GAP = 55f;

    /** <p>Vertical offset from centre for the first row of key bindings.</p> */
    private static final float KEY_START_Y_OFFSET = 20f;

    /**
     * <p>Constructs the SettingsScene with the required dependencies.</p>
     *
     * @param input   the input controller for detecting key presses and rebinding
     * @param audio   the audio controller for reading/setting volume
     * @param nav     the scene navigator for popping back to the previous scene
     * @param factory the scene factory (not used directly but kept for consistency)
     */
    public SettingsScene(IInputController input, IAudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;

    }

    /**
     * <p>Sets up camera, rendering resources, volume, UI layout, and buttons.
     * Called when the scene is first loaded.</p>
     */
    @Override
    protected void onLoad() {
        initializeCamera();
        initializeRendering();
        initializeVolume();
        recalcUI();
        initializeStage();
        initializeUI();

        Gdx.app.log("Scene", "SettingsScene loaded");
    }

    /**
     * <p>Sets up the orthographic camera and stretch viewport
     * with the fixed virtual resolution.</p>
     */
    private void initializeCamera() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VW, VH, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Centre the camera on the virtual world
        camera.position.set(VW / 2f, VH / 2f, 0);
        camera.update();
    }

    /**
     * <p>Creates the shape renderer, sprite batch, font, and glyph layout
     * if they haven't been created yet.</p>
     */
    private void initializeRendering() {
        if (shape == null) {
            shape = new ShapeRenderer();
            batch = new SpriteBatch();
            font = new BitmapFont();
            layout = new GlyphLayout();
        }
    }

    /**
     * <p>Reads the current music volume from the audio controller
     * so the slider starts at the correct position.</p>
     */
    private void initializeVolume() {
        volume01 = audio.getMusicVolume();
        Gdx.app.log("AudioController", "SettingsScene current music volume: " + (int) (volume01 * 100) + "%");
    }

    /**
     * <p>Calculates positions and sizes for the panel, slider, and
     * back button based on the fixed virtual resolution.</p>
     */
    private void recalcUI() {
        // Panel — centred on screen
        panelW = 620f;
        panelH = 420f;
        panelX = (VW - panelW) / 2f;
        panelY = (VH - panelH) / 2f;

        // Slider — inside the panel, below the header
        sliderW = 460f;
        sliderH = 16f;
        sliderX = panelX + 80f;
        sliderY = panelY + panelH - 120f;

        // Back button — centred horizontally near the bottom
        btnW = BACK_BUTTON_WIDTH;
        btnH = BACK_BUTTON_HEIGHT;
        btnX = (VW - btnW) / 2f;
        btnY = BACK_BUTTON_Y;
    }

    /**
     * <p>Creates the Scene2D Stage that manages UI actors and sets it
     * as the input processor so buttons receive click events.</p>
     */
    private void initializeStage() {
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * <p>Loads the UI skin from file and creates the back button and
     * key binding buttons. Logs an error if the skin cannot be loaded.</p>
     */
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

    /**
     * <p>Creates the back button that pops this settings scene,
     * returning to the previous scene underneath.</p>
     */
    private void createBackButton() {
        backButton = uiElement.createButton(
            "BACK",
            BACK_BUTTON_WIDTH,
            BACK_BUTTON_HEIGHT,
            () -> nav.popScene());

        backButton.setPosition(btnX, btnY);
        stage.addActor(backButton);
    }

    /**
     * <p>Creates key binding buttons for LEFT, RIGHT, UP, DOWN, and SHOOT
     * arranged in a grid layout. Each button allows the player to rebind
     * the corresponding action to a different key.</p>
     */
    private void createKeyBindings() {
        keyBindingButtons = new java.util.ArrayList<>();
        KeyBindingPositions positions = calculateKeyBindingPositions();

        // Row 1: LEFT and RIGHT
        addKeyBinding("LEFT", positions.leftX, positions.row1Y);
        addKeyBinding("RIGHT", positions.rightX, positions.row1Y);

        // Row 2: UP and DOWN
        addKeyBinding("UP", positions.leftX, positions.row2Y);
        addKeyBinding("DOWN", positions.rightX, positions.row2Y);
        // Row 3: SHOOT (centred)
        addKeyBinding("SHOOT", positions.centerX - (KEY_BUTTON_WIDTH / 2f), positions.row3Y);
    }

    /**
     * <p>Calculates the x/y positions for each row and column of
     * key binding buttons based on the virtual resolution.</p>
     *
     * @return a {@link KeyBindingPositions} object with all calculated positions
     */
    private KeyBindingPositions calculateKeyBindingPositions() {
        float centerX = VW / 2f;
        float centerY = VH / 2f;

        // Two-column layout: left and right of centre
        float leftX = centerX - KEY_HORIZONTAL_GAP - KEY_BUTTON_WIDTH;
        float rightX = centerX + KEY_HORIZONTAL_GAP;

        // Three rows starting from above centre, going downward
        float row1Y = centerY + KEY_START_Y_OFFSET;
        float row2Y = row1Y - KEY_VERTICAL_GAP;
        float row3Y = row2Y - KEY_VERTICAL_GAP;

        return new KeyBindingPositions(centerX, leftX, rightX, row1Y, row2Y, row3Y);
    }

    /**
     * <p>Holds the calculated positions for key binding button layout.
     * Used internally to pass multiple position values between methods.</p>
     */
    private static class KeyBindingPositions {
        /** <p>Centre X of the screen.</p> */
        final float centerX;
        /** <p>Left column X position.</p> */
        final float leftX;
        /** <p>Right column X position.</p> */
        final float rightX;
        /** <p>First row Y position.</p> */
        final float row1Y;
        /** <p>Second row Y position.</p> */
        final float row2Y;
        /** <p>Third row Y position.</p> */
        final float row3Y;

        /**
         * <p>Constructs position data for key binding button layout.</p>
         *
         * @param centerX centre X position
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
     * <p>Creates a single key binding button for the given action and adds
     * it to the stage at the specified position.</p>
     *
     * @param action the action name to bind (e.g. "LEFT", "SHOOT")
     * @param x      x position in world coordinates
     * @param y      y position in world coordinates
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
     * <p>Handles input each frame: volume slider control via arrow keys
     * and mouse click, and keyboard shortcuts for navigation.</p>
     *
     * @param delta time since last frame in seconds
     */
    @Override
    protected void onUpdate(float delta) {
        // Check for ENTER key to go back
        handleKeyboardShortcuts();

        float previousVolume = volume01;

        // Adjust volume with LEFT/RIGHT arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            volume01 -= delta * 0.6f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            volume01 += delta * 0.6f;
        volume01 = MathUtils.clamp(volume01, 0f, 1f);

        // Handle mouse click on the slider or back button
        if (Gdx.input.justTouched()) {
            // Unproject screen coordinates to world coordinates
            Vector2 world = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            // Check if click is on the back button area
            if (contains(world.x, world.y, btnX, btnY, btnW, btnH)) {
                nav.popScene();
                return;
            }

            // Check if click is on the slider area (with some vertical padding)
            if (contains(world.x, world.y, sliderX, sliderY - 12f, sliderW, sliderH + 24f)) {
                float t = (world.x - sliderX) / sliderW;
                volume01 = MathUtils.clamp(t, 0f, 1f);
            }
        }

        // Apply volume change if it was modified
        if (Math.abs(volume01 - previousVolume) > 0.001f) {
            audio.setMusicVolume(volume01);
            audio.setSoundVolume(volume01);
            Gdx.app.log("SettingsScene", "Volume updated to: " + (int) (volume01 * 100) + "%");
        }

    }

    /**
     * <p>Checks for ENTER key press to pop back to the previous scene.</p>
     */
    private void handleKeyboardShortcuts() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            nav.popScene();
        }
    }

    /**
     * <p>Checks if a point (px, py) is inside a rectangle defined by
     * position (x, y) and size (w, h).</p>
     *
     * @param px point x coordinate
     * @param py point y coordinate
     * @param x  rectangle left edge
     * @param y  rectangle bottom edge
     * @param w  rectangle width
     * @param h  rectangle height
     * @return true if the point is inside the rectangle
     */
    private boolean contains(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    /**
     * <p>Draws the settings panel, slider, text labels, and UI buttons.
     * Called every frame after onUpdate.</p>
     */
    @Override
    protected void onRender() {
        if (camera == null || viewport == null)
            return;

        prepareRender();
        renderShapes();
        renderText();
        renderStage();
    }

    /**
     * <p>Applies the viewport, clears the screen with a dark background,
     * and sets projection matrices for shape and text rendering.</p>
     */
    private void prepareRender() {
        // Apply viewport first, then clear, then draw
        viewport.apply();
        camera.update();

        // Clear with a dark background
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set projection matrices for rendering
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    /**
     * <p>Draws the panel background, header strip, slider track,
     * slider fill bar, and slider knob using the shape renderer.</p>
     */
    private void renderShapes() {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Dark panel background
        shape.setColor(0.13f, 0.14f, 0.17f, 1f);
        shape.rect(panelX, panelY, panelW, panelH);

        // Slightly lighter header strip at the top of the panel
        shape.setColor(0.18f, 0.19f, 0.23f, 1f);
        shape.rect(panelX, panelY + panelH - 80f, panelW, 80f);

        // Slider track (dark background)
        shape.setColor(0.10f, 0.11f, 0.14f, 1f);
        shape.rect(sliderX, sliderY, sliderW, sliderH);

        // Slider fill (green bar showing current volume)
        shape.setColor(0.30f, 0.75f, 0.45f, 1f);
        shape.rect(sliderX, sliderY, sliderW * volume01, sliderH);

        // Slider knob (white circle at current volume position)
        float knobX = sliderX + sliderW * volume01;
        shape.setColor(0.90f, 0.90f, 0.95f, 1f);
        shape.circle(knobX, sliderY + sliderH / 2f, 12f);

        shape.end();
    }

    /**
     * <p>Draws the "SETTINGS" title, "VOLUME" label, and current
     * volume percentage text using the sprite batch and font.</p>
     */
    private void renderText() {
        batch.begin();

        // Title — centred in the header
        font.getData().setScale(2.0f);
        layout.setText(font, "SETTINGS");
        font.draw(batch, layout,
            panelX + (panelW - layout.width) / 2f,
            panelY + panelH - 28f);

        // Volume label — above the slider on the left
        font.getData().setScale(1.3f);
        layout.setText(font, "VOLUME");
        font.draw(batch, layout, sliderX, sliderY + 60f);

        // Percentage — above the slider on the right
        font.getData().setScale(1.1f);
        String percent = (int) (volume01 * 100) + "%";
        layout.setText(font, percent);
        font.draw(batch, layout, sliderX + sliderW - layout.width, sliderY + 60f);

        batch.end();
    }

    /**
     * <p>Updates and draws the Scene2D Stage, which contains the
     * back button and key binding buttons.</p>
     */
    private void renderStage() {
        if (stage != null) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    /**
     * <p>Called when the window is resized. Updates the viewport and
     * recalculates UI positions to stay centred.</p>
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "SettingsScene resize: " + width + "x" + height);

        if (viewport != null) {
            viewport.update(width, height, true);
        }

        // Recalculate panel, slider, and button positions
        recalcUI();
        Gdx.app.log("Scene", "World size locked at: " + VW + "x" + VH);
    }

    /**
     * <p>Returns true so the engine pauses world updates (entity movement,
     * collisions, etc.) while the settings overlay is open.</p>
     *
     * @return true to block world updates underneath this scene
     */
    @Override
    public boolean blocksWorldUpdate() {
        return true;
    }

    /** <p>Called when the scene is unloaded (before disposal).</p> */
    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "SettingsScene unloading...");
    }

    /**
     * <p>Disposes of all rendering resources (shape renderer, sprite batch,
     * font, skin, stage) to free GPU memory.</p>
     */
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
