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
 * MainMenuScene
 *
 * Uses StretchViewport so the menu UI scales with the window.
 */
public class MainMenuScene extends Scene {

    private final SceneFactory factory;

    // create for buttion ui
    private Stage stage;
    private Skin skin;
    private TextButton startButton;
    private TextButton settingButton;
    private UIElement uiElement;

    // FIXED WORLD SIZE - matches GameScene
    private static final float WORLD_W = 800f;
    private static final float WORLD_H = 480f;

    // Button dimensions
    private static final float BUTTON_WIDTH = 200f;
    private static final float BUTTON_HEIGHT = 60f;
    
    // Button spacing
    private static final float START_BUTTON_Y_OFFSET = 40f;

    public MainMenuScene(IInputController input, IAudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "MainMenuScene loaded");

        audio.setMusic("Music_Menu.mp3");
        Gdx.app.log("AudioController", "MainMenu music loaded");

        stage = new Stage(new StretchViewport(WORLD_W, WORLD_H));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage);

        // creation on ui button
        initializeUI();

    }

    // initialize ui by SRP
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

    // list of button created. using OCP by uiElement
    private void createButtons() {
        // Start game button
        startButton = uiElement.createButton(
                "START GAME",
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                () -> nav.requestScene(factory.createGameScene()));

        // Settings button
        settingButton = uiElement.createButton(
                "SETTING",
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                () -> nav.pushScene(factory.createSettingsScene()));
    }

    // button positioning
    private void positionButtons() {
        float centerX = (WORLD_W - BUTTON_WIDTH) / 2f;
        float centerY = WORLD_H / 2f;

        // Position start button above center
        if (startButton != null) {
            startButton.setPosition(centerX, centerY + START_BUTTON_Y_OFFSET);
        }

        // Position settings button below start button
        if (settingButton != null) {
            settingButton.setPosition(centerX, centerY - START_BUTTON_Y_OFFSET);
        }
    }

    // to put stage and actors for button.
    private void addButtonsToStage() {
        if (startButton != null) {
            stage.addActor(startButton);
        }
        if (settingButton != null) {
            stage.addActor(settingButton);
        }
    }


    @Override
    protected void onUpdate(float delta) {
        if (stage != null)
            stage.act(delta);

        // for returning another scene to check if stage is null
        if (Gdx.input.getInputProcessor() != stage) {
            Gdx.input.setInputProcessor(stage);
        }

        if (input.isActionJustPressed("START_GAME")) {
            Gdx.app.log("InputController", "Action 'START_GAME' was pressed");
            nav.requestScene(factory.createGameScene());
        }
    }

    @Override
    protected void onRender() {
        // CRITICAL ORDER: Apply viewport FIRST, then clear, then draw!
        if (stage != null) {
            stage.getViewport().apply();
        }

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (stage != null)
            stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "MainMenuScene resize: " + width + "x" + height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
           positionButtons();
        }
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "MainMenuScene unloading...");
    }

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
