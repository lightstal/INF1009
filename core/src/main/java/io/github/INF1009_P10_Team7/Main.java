package io.github.INF1009_P10_Team7;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.engine.scene.MainMenuScene;
import io.github.INF1009_P10_Team7.engine.core.ContextImplementation;
import io.github.INF1009_P10_Team7.engine.core.GameContext;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioOutput;
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
	private EventBus eventBus;
    @Override
    public void create() {
        eventBus = new EventBus();
        inputOutputManager = new InputOutputManager();

        AudioOutput audio = inputOutputManager.getAudioOutput();

        eventBus.subscribe(EventType.PLAY_MUSIC, audio);
        eventBus.subscribe(EventType.PLAY_SOUND, audio);
        eventBus.subscribe(EventType.STOP_MUSIC, audio);

        // Listen for Logic Events (Pause/Resume)
        eventBus.subscribe(EventType.GAME_PAUSED, audio);
        eventBus.subscribe(EventType.GAME_RESUMED, audio);

        GameContext context = new ContextImplementation(
            eventBus,
            inputOutputManager
        );

        sceneManager = new SceneManager(context);

//        sceneManager = new SceneManager(inputOutputManager, eventBus);

        // Start with MainMenu scene
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
