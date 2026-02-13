package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.INF1009_P10_Team7.engine.core.GameEngine;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.scene.MainMenuScene;
import io.github.INF1009_P10_Team7.engine.scene.GameScene;
import io.github.INF1009_P10_Team7.engine.scene.SettingsScene;

/**
 * Part1SimulationApp (composition root)
 *
 * - Creates GameEngine
 * - Configures keybinds / demo options
 * - Wires scenes using ONLY interfaces
 * - Starts the initial scene
 */
public class Part1SimulationApp extends ApplicationAdapter {

    private GameEngine engine;

    @Override
    public void create() {
        Gdx.app.log("SIM", "Part1SimulationApp create(): start (engine init)");

        SimulationTestScript.printInstructions();
        SimulationTestScript.printScalingNote();

        engine = new GameEngine();

        // ===== Simulation-only configuration =====
        engine.setCollisionSound("bell.mp3");

        InputController input = engine.getInput();
        AudioController audio = engine.getAudio();
        SceneNavigator nav = engine.getNavigator();
        EntityQuery entities = engine.getEntities();

        // Keybinds
        input.bindKey("START_GAME", Input.Keys.SPACE);
        input.bindKey("RESTART_GAME", Input.Keys.R);
        input.bindKey("SETTINGS", Input.Keys.ESCAPE);
        input.bindKey("BACK", Input.Keys.BACKSPACE);
        input.bindKey("LEFT", Input.Keys.A);
        input.bindKey("RIGHT", Input.Keys.D);
        input.bindKey("UP", Input.Keys.W);
        input.bindKey("DOWN", Input.Keys.S);
        input.bindMouseButton("SHOOT", Input.Buttons.LEFT);

        // Scene factory to keep scene constructors minimal
        SceneFactory factory = new Part1SceneFactory(input, audio, nav, entities);

        // Start with MainMenu scene
        nav.setScene(factory.createMainMenuScene());
    }

    @Override
    public void render() {
        // DO NOT call ScreenUtils.clear() here!
        // Each scene handles its own clearing AFTER viewport.apply()
        // This is critical for Option 3 (Viewport/Camera) scaling to work.
        float dt = Gdx.graphics.getDeltaTime();
        engine.update(dt);
        engine.render();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("SIM", "Part1SimulationApp resize: " + width + "x" + height);
        engine.resize(width, height);
    }

    @Override
    public void dispose() {
        Gdx.app.log("SIM", "Part1SimulationApp dispose(): end (clean shutdown)");
        if (engine != null) engine.dispose();
    }

    /** Concrete factory implementation for Part 1 scenes. */
    private static final class Part1SceneFactory implements SceneFactory {
        private final InputController input;
        private final AudioController audio;
        private final SceneNavigator nav;
        private final EntityQuery entities;

        private Part1SceneFactory(InputController input, AudioController audio, SceneNavigator nav, EntityQuery entities) {
            this.input = input;
            this.audio = audio;
            this.nav = nav;
            this.entities = entities;
        }

        @Override
        public Scene createMainMenuScene() {
            return new MainMenuScene(input, audio, nav, this);
        }

        @Override
        public Scene createGameScene() {
            return new GameScene(input, audio, nav, entities, this);
        }

        @Override
        public Scene createSettingsScene() {
            return new SettingsScene(input, audio, nav, this);
        }
    }
}
