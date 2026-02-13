package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

/**
 * MainMenuScene
 *
 * Dependencies (minimal):
 * - InputController: read keybind actions
 * - AudioController: play menu music / click SFX
 * - SceneNavigator: request scene switches
 * - SceneFactory: create target scenes without leaking extra dependencies
 */
public class MainMenuScene extends Scene {

    private final SceneFactory factory;

    private Stage stage;
    private Skin skin;
    private TextButton startButton;

    public MainMenuScene(InputController input, AudioController audio, SceneNavigator nav, SceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "MainMenuScene loaded");

        audio.setMusic("Music_Menu.mp3");
        Gdx.app.log("AudioController", "MainMenu music loaded");

        stage = new Stage(new ScreenViewport());
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        try {
            skin = new Skin(Gdx.files.internal("buttons/name2d.json"));

            startButton = new TextButton("START GAME", skin, "default");
            startButton.setSize(200, 60);
            updateButtonPosition();

            startButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("UI", "Start Button Clicked");
                    nav.requestScene(factory.createGameScene());
                }
            });

            stage.addActor(startButton);
        } catch (Exception e) {
            Gdx.app.error("UI", "Failed to load skin: buttons/name2d.json", e);
        }

        Gdx.input.setInputProcessor(stage);
    }

    private void updateButtonPosition() {
        if (startButton != null && stage != null) {
            startButton.setPosition(
                (stage.getViewport().getWorldWidth() - startButton.getWidth()) / 2f,
                stage.getViewport().getWorldHeight() / 2f
            );
        }
    }

    @Override
    protected void onUpdate(float delta) {
        if (stage != null) stage.act(delta);

        if (input.isActionJustPressed("START_GAME")) {
            Gdx.app.log("InputController", "Action 'START_GAME' was pressed");
            nav.requestScene(factory.createGameScene());
        }
    }

    @Override
    protected void onRender() {
        ScreenUtils.clear(0.2f, 0.2f, 0.8f, 1f);
        if (stage != null) stage.draw();
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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
