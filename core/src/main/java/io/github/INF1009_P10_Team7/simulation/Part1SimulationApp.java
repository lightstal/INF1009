package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.scenes.MainMenuScene;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioOutput;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;

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
    private InputOutputManager inputOutputManager;
	private EventBus eventBus;

    @Override
    public void create() {
        // Rubric: start without errors
        Gdx.app.log("SIM", "Part1SimulationApp create(): start (engine init)");

        // Print instructions for marker/video
        SimulationTestScript.printInstructions();
        SimulationTestScript.printScalingNote();
        
        eventBus = new EventBus();
        
        inputOutputManager = new InputOutputManager();
        
        AudioOutput audio = inputOutputManager.getAudioOutput();
        
        eventBus.subscribe(EventType.PLAY_MUSIC, audio);
        eventBus.subscribe(EventType.PLAY_SOUND, audio);
        eventBus.subscribe(EventType.STOP_MUSIC, audio);
        
        // Listen for Logic Events (Pause/Resume)
        eventBus.subscribe(EventType.GAME_PAUSED, audio);
        eventBus.subscribe(EventType.GAME_RESUMED, audio);

        inputOutputManager.bindKey("START_GAME", Input.Keys.SPACE);
        inputOutputManager.bindKey("RESTART_GAME", Input.Keys.R);
        inputOutputManager.bindKey("SETTINGS", Input.Keys.ESCAPE);
        inputOutputManager.bindKey("BACK", Input.Keys.BACKSPACE);
        inputOutputManager.bindKey("LEFT", Input.Keys.A);
        inputOutputManager.bindKey("RIGHT", Input.Keys.D);
        inputOutputManager.bindMouseButton("SHOOT", Input.Buttons.LEFT);

        
        sceneManager = new SceneManager(inputOutputManager, eventBus);

        // Start with MainMenu scene
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    @Override
    public void render() {
        // Clear frame (scenes also clear; this is safe)
        ScreenUtils.clear(0, 0, 0, 1);

        // Standard game loop
        float dt = Gdx.graphics.getDeltaTime();
        inputOutputManager.update();
        
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
        inputOutputManager.dispose();
    }
}
