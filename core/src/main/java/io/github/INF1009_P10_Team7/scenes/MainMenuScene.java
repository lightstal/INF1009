package io.github.INF1009_P10_Team7.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;

import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;

/**
 * MainMenuScene
 *
 * Controls:
 * - SPACE -> switch to GameScene
 *
 * Visual:
 * - Blue background
 */
public class MainMenuScene extends Scene {

    private final SceneManager sceneManager;

    public MainMenuScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    protected void onLoad() {
        // Log for testing (marker can see lifecycle)
        Gdx.app.log("Scene", "MainMenuScene loaded");
        
        InputOutputManager io = sceneManager.getInputOutputManager();
        io.playMusic("Music_Menu.mp3");
    }

    @Override
    protected void onUpdate(float delta) {
//        // Press SPACE to go to GameScene
//        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
//            sceneManager.requestScene(new GameScene(sceneManager));
//        }
        
        InputOutputManager io = sceneManager.getInputOutputManager();
        if (io.isActionJustPressed("START_GAME")) {
            sceneManager.requestScene(new GameScene(sceneManager));
        }
        
    }

    @Override
    protected void onRender() {
        // Blue screen
        ScreenUtils.clear(0.2f, 0.2f, 0.8f, 1f);
    }

    @Override
    public void resize(int width, int height) {
        // Resize log to show resize forwarding works
        Gdx.app.log("Scene", "MainMenuScene resize: " + width + "x" + height);
    }

    @Override
    protected void onUnload() {
        // Log for testing
        Gdx.app.log("Scene", "MainMenuScene unloaded");
        
        InputOutputManager io = sceneManager.getInputOutputManager();
        io.setMusicState("stopped");
    }
}
