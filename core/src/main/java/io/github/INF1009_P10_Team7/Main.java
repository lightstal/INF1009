package io.github.INF1009_P10_Team7;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.scenes.MainMenuScene;

import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;

/**
 * Main (future real game entry point).
 *
 * For Part 1, you can run Part1SimulationApp instead.
 * For later parts, you can build your actual game flow here.
 */
public class Main extends ApplicationAdapter {

    private SceneManager sceneManager;
    private InputOutputManager inputOutputManager;

    @Override
    public void create() {
    	inputOutputManager = new InputOutputManager();
        sceneManager = new SceneManager(inputOutputManager);
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        float dt = Gdx.graphics.getDeltaTime();
        
        inputOutputManager.update();
        
        sceneManager.update(dt);
        sceneManager.render();
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.resize(width, height);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        if (inputOutputManager != null) {
            inputOutputManager.dispose();
        }
    }
}
