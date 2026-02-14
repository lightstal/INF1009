package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.UIElement;

/**
 * MainMenuScene
 *
 * Uses StretchViewport so the menu UI scales with the window.
 */
public class MainMenuScene extends Scene {

    private final SceneFactory factory;

    private Stage stage;
    private Skin skin;
    private TextButton startButton;
    private TextButton settingButton;
    private UIElement uiElement;

    // FIXED WORLD SIZE - matches GameScene
    private static final float WORLD_W = 800f;
    private static final float WORLD_H = 480f;

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

        // for button ui
        try {
            skin = new Skin(Gdx.files.internal("buttons/name2d.json"));

            uiElement = new UIElement(skin, true); // Create instance

            startButton = uiElement.createButton("START GAME", 200, 60,
                    () -> nav.requestScene(factory.createGameScene()));

            settingButton = uiElement.createButton("SETTING", 200, 60,
                    () -> nav.pushScene(factory.createSettingsScene()));

            updateButtonPosition();

            stage.addActor(startButton);
            stage.addActor(settingButton);

        } catch (Exception e) {
            Gdx.app.error("UI", "Failed to load skin: buttons/name2d.json", e);
        }

        Gdx.input.setInputProcessor(stage);

    }

    private void updateButtonPosition() {
        if (stage != null) {
            // Center of the screen horizontally
            float centerX = (WORLD_W - 200) / 2f;

            // Position Start Button slightly above the center
            if (startButton != null) {
                startButton.setPosition(centerX, (WORLD_H / 2f) + 40);
            }

            // Position Setting Button slightly below the Start Button
            if (settingButton != null) {
                settingButton.setPosition(centerX, (WORLD_H / 2f) - 40);
            }
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
            updateButtonPosition();
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
