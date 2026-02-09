package io.github.INF1009_P10_Team7.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * SettingsScene
 *
 * Purpose:
 * - Demonstrates a generic "return to previous scene" concept.
 *
 * Controls:
 * - BACKSPACE -> return to previous scene
 * - R -> restart previous scene
 *
 * Visual:
 * - Green background
 */
public class SettingsScene extends Scene {

    private final Scene previousScene;

    public SettingsScene(SceneManager sceneManager, Scene previousScene) {
        super(sceneManager);
        this.previousScene = previousScene;
    }

    @Override
    protected void onLoad() {
        Gdx.app.log("Scene", "SettingsScene loaded");
    }

    @Override
    protected void onUpdate(float delta) {
        // BACKSPACE -> return
        if (context.getInputController().isActionJustPressed("BACK")) {
            Gdx.app.log("Input", "Key binded to 'BACK' action was pressed");
        	sceneManager.requestScene(previousScene);
        }
        // R -> restart to new Game Scene
        if (context.getInputController().isActionJustPressed("RESTART_GAME")) {
            Gdx.app.log("Input", "Key binded to 'RESTART_GAME' action was pressed");
            previousScene.dispose();
        	sceneManager.requestScene(new GameScene(sceneManager));
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

    @Override
    protected void onDispose() {
        Gdx.app.log("Scene", "SettingsScene diposed");
    }
}
