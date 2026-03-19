package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.INF1009_P10_Team7.engine.UIManagement.UIElement;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * <p>Main menu screen with start game and settings buttons.
 * Uses StretchViewport so the UI scales with the window.</p>
 *
 * <p>This is the first scene displayed when the simulation starts.
 * The player can press SPACE or click the button to enter the game,
 * or open settings from here.</p>
 */
public class MainMenuScene extends Scene {

    /** <p>Factory for creating scenes during navigation.</p> */
    private final SceneFactory factory;

    /** <p>Scene2D stage that holds and manages UI actors (buttons).</p> */
    private Stage stage;

    /** <p>Skin that provides styles for the UI buttons.</p> */
    private Skin skin;

    /** <p>Button that starts the game when clicked.</p> */
    private TextButton startButton;

    /** <p>Button that opens the settings screen when clicked.</p> */
    private TextButton settingButton;

    /** <p>Helper class for creating styled UI buttons.</p> */
    private UIElement uiElement;

    /** <p>Fixed virtual world width — matches GameScene.</p> */
    private static final float WORLD_W = 800f;

    /** <p>Fixed virtual world height — matches GameScene.</p> */
    private static final float WORLD_H = 480f;

    /** <p>Width of the menu buttons in world units.</p> */
    private static final float BUTTON_WIDTH = 200f;

    /** <p>Height of the menu buttons in world units.</p> */
    private static final float BUTTON_HEIGHT = 60f;

    /** <p>Vertical offset from centre for spacing the two buttons.</p> */
    private static final float START_BUTTON_Y_OFFSET = 40f;

    /**
     * <p>Constructs the MainMenuScene with the required dependencies.</p>
     *
     * @param input   the input controller for detecting key presses
     * @param audio   the audio controller for playing menu music
     * @param nav     the scene navigator for switching between scenes
     * @param factory the scene factory for creating game and settings scenes
     */
    public MainMenuScene(IInputController input, IAudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    /**
     * <p>Sets up the stage, loads menu music, and creates the UI buttons.</p>
     */
    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "MainMenuScene loaded");

        // Set the background music for the menu
        audio.setMusic("Music_Menu.mp3");
        Gdx.app.log("AudioController", "MainMenu music loaded");

        // Create a stage with a StretchViewport to handle window resizing
        stage = new Stage(new StretchViewport(WORLD_W, WORLD_H));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage); // Let the stage handle click events

        // Build the UI buttons
        initializeUI();
    }

    /**
     * <p>Loads the skin from file and creates all UI buttons.
     * Logs an error if the skin file cannot be found.</p>
     */
    private void initializeUI() {
        try {
            skin = new Skin(Gdx.files.internal("buttons/name2d.json"));
            uiElement = new UIElement(skin, true);

            createButtons();
            positionButtons();
            addButtonsToStage();

            Gdx.app.log("MainMenuScene", "UI initialized");
        } catch (Exception e) {
            Gdx.app.error("MainMenuScene", "Failed to load UI skin", e);
        }
    }

    /**
     * <p>Creates the start game and settings buttons with click handlers.
     * Start button navigates to the game scene; settings button pushes
     * the settings scene as an overlay.</p>
     */
    private void createButtons() {
        // Start game button — replaces current scene with GameScene
        startButton = uiElement.createButton(
            "START GAME",
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            () -> nav.requestScene(factory.createGameScene()));

        // Settings button — pushes SettingsScene on top of current scene
        settingButton = uiElement.createButton(
            "SETTING",
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            () -> nav.pushScene(factory.createSettingsScene()));
    }

    /**
     * <p>Positions the buttons horizontally centred, with the start button
     * above the centre and the settings button below.</p>
     */
    private void positionButtons() {
        float centerX = (WORLD_W - BUTTON_WIDTH) / 2f;
        float centerY = WORLD_H / 2f;

        // Start button sits above centre
        if (startButton != null) {
            startButton.setPosition(centerX, centerY + START_BUTTON_Y_OFFSET);
        }

        // Settings button sits below start button
        if (settingButton != null) {
            settingButton.setPosition(centerX, centerY - START_BUTTON_Y_OFFSET);
        }
    }

    /**
     * <p>Adds the buttons to the stage so they can be rendered
     * and receive click events.</p>
     */
    private void addButtonsToStage() {
        if (startButton != null) {
            stage.addActor(startButton);
        }
        if (settingButton != null) {
            stage.addActor(settingButton);
        }
    }

    /**
     * <p>Updates the stage each frame and checks for the SPACE key
     * shortcut to start the game.</p>
     *
     * @param delta time since last frame in seconds
     */
    @Override
    protected void onUpdate(float delta) {
        // Update stage actors (animations, etc.)
        if (stage != null)
            stage.act(delta);

        // Re-attach the stage as input processor if another scene changed it
        if (Gdx.input.getInputProcessor() != stage) {
            Gdx.input.setInputProcessor(stage);
        }

        // Keyboard shortcut: SPACE starts the game
        if (input.isActionJustPressed("START_GAME")) {
            Gdx.app.log("InputController", "Action 'START_GAME' was pressed");
            nav.requestScene(factory.createGameScene());
        }
    }

    /**
     * <p>Clears the screen with a blue background and draws the stage
     * (buttons and any other UI actors).</p>
     */
    @Override
    protected void onRender() {
        // Apply viewport first, then clear, then draw
        if (stage != null) {
            stage.getViewport().apply();
        }

        // Clear with a blue background
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the stage (buttons)
        if (stage != null)
            stage.draw();
    }

    /**
     * <p>Called when the window is resized. Updates the stage viewport
     * and repositions buttons to stay centred.</p>
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "MainMenuScene resize: " + width + "x" + height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
            positionButtons(); // Recentre buttons after resize
        }
    }

    /** <p>Called when the scene is unloaded (before disposal).</p> */
    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "MainMenuScene unloading...");
    }

    /**
     * <p>Disposes of the stage, skin, and clears the input processor
     * to free resources.</p>
     */
    @Override
    protected void onDispose() {
        Gdx.app.log("Scene", "MainMenuScene disposed");
        if (stage != null)
            stage.dispose();
        if (skin != null)
            skin.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
