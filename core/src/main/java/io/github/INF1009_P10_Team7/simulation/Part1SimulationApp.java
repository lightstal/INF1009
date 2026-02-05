package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.scenes.MainMenuScene;

/**
 * Part1SimulationApp
 *
 * Runs your engine + scenes specifically to satisfy the Part 1 rubric:
 * - Initializes without error (create)
 * - Shows scene switching with logs (load/unload)
 * - Shows resize forwarding
 * - Ends without error (dispose)
 *
 * IMPORTANT:
 * - This is only for Part 1 demo.
 * - Your actual game can later run Main.java instead.
 */
public class Part1SimulationApp extends ApplicationAdapter {

    private SceneManager sceneManager;

    @Override
    public void create() {
        // Rubric: start without errors
        Gdx.app.log("SIM", "Part1SimulationApp create(): start (engine init)");

        sceneManager = new SceneManager();

        // Print instructions for marker/video
        SimulationTestScript.printInstructions();
        SimulationTestScript.printScalingNote();

        // Start with MainMenu scene
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    @Override
    public void render() {
        // Clear frame (scenes also clear; this is safe)
        ScreenUtils.clear(0, 0, 0, 1);

        // Standard game loop
        float dt = Gdx.graphics.getDeltaTime();
        sceneManager.update(dt);
        sceneManager.render();
    }

    @Override
    public void resize(int width, int height) {
        // Rubric: show resize forwarding
        sceneManager.resize(width, height);
    }

    @Override
    public void dispose() {
        // Rubric: end without errors
        Gdx.app.log("SIM", "Part1SimulationApp dispose(): end (clean shutdown)");
        sceneManager.dispose();
    }
}
