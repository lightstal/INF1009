package io.github.INF1009_P10_Team7.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;

/**
 * GameScene
 *
 * Controls:
 * - ESC -> go to SettingsScene (pass this as previous scene)
 * - BACKSPACE -> go back to MainMenuScene
 *
 * Visual:
 * - Red background
 */
public class GameScene extends Scene {

    private final SceneManager sceneManager;

    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "GameScene loaded");
    }

    @Override
    protected void onUpdate(float delta) {
        // ESC -> open Settings, return back to this GameScene
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            sceneManager.requestScene(new SettingsScene(sceneManager, this));
        }

        // BACKSPACE -> back to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            sceneManager.requestScene(new MainMenuScene(sceneManager));
        }
    }

    @Override
    protected void onRender() {
        // Red screen
        ScreenUtils.clear(0.8f, 0.2f, 0.2f, 1f);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "GameScene resize: " + width + "x" + height);
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "GameScene unloaded");
    }
}
