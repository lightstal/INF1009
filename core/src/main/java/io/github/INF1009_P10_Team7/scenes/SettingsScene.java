package io.github.INF1009_P10_Team7.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutput;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;

/**
 * SettingsScene
 *
 * Purpose:
 * - Demonstrates a generic "return to previous scene" concept.
 *
 * Controls:
 * - BACKSPACE -> return to previous scene
 *
 * Visual:
 * - Green background
 */
public class SettingsScene extends Scene {

    private final SceneManager sceneManager;
    private final Scene previousScene;

    public SettingsScene(SceneManager sceneManager, Scene previousScene) {
        this.sceneManager = sceneManager;
        this.previousScene = previousScene;
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "SettingsScene loaded");

        InputOutput io = sceneManager.getInputOutput();
        io.setMusicState("paused");
        Gdx.app.log("Audio Output", "Game Music State set to 'paused'");
    }

    @Override
    protected void onUpdate(float delta) {
//        // BACKSPACE -> return
//        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
//            sceneManager.requestScene(previousScene);
//        }

        InputOutput io = sceneManager.getInputOutput();
        if (io.isActionJustPressed("BACK")) {
            Gdx.app.log("Input", "Key binded to 'BACK' action was pressed");
        	sceneManager.requestScene(previousScene);
        }
    }

    @Override
    protected void onRender() {
        // Green screen
        ScreenUtils.clear(0.2f, 0.8f, 0.2f, 1f);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Scene", "SettingsScene resize: " + width + "x" + height);
    }

    @Override
    protected void onUnload() {
        Gdx.app.log("Scene", "SettingsScene unloaded");
    }
}
