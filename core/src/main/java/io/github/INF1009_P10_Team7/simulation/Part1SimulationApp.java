package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.INF1009_P10_Team7.engine.core.GameEngine;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * Part1SimulationApp (composition root)
 *
 * - Creates GameEngine
 * - Configures keybinds
 * - Wires scenes using ONLY interfaces (Dependency Inversion)
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

        // Get interfaces from engine (Dependency Inversion)
        InputController input = engine.getInput();
        AudioController audio = engine.getAudio();
        SceneNavigator nav = engine.getNavigator();
        EntityQuery entityQuery = engine.getEntityQuery();
        IEntitySystem entitySystem = engine.getEntitySystem();
        ICollisionSystem collisionSystem = engine.getCollisionSystem();
        IMovementSystem movementSystem = engine.getMovementSystem();

        // Keybinds (simulation-specific configuration)
        input.bindKey("START_GAME", Input.Keys.SPACE);
        input.bindKey("RESTART_GAME", Input.Keys.R);
        input.bindKey("SETTINGS", Input.Keys.ESCAPE);
        input.bindKey("BACK", Input.Keys.BACKSPACE);
        input.bindKey("LEFT", Input.Keys.A);
        input.bindKey("RIGHT", Input.Keys.D);
        input.bindKey("UP", Input.Keys.W);
        input.bindKey("DOWN", Input.Keys.S);
        input.bindMouseButton("SHOOT", Input.Buttons.LEFT);

        // Scene factory wired with interfaces
        SceneFactory factory = new Part1SceneFactory(
            input, audio, nav, entityQuery,
            entitySystem, collisionSystem, movementSystem
        );

        // Start with MainMenu scene
        nav.setScene(factory.createMainMenuScene());
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        engine.update(dt);
        engine.render();
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void dispose() {
        Gdx.app.log("SIM", "Part1SimulationApp dispose(): clean shutdown");
        if (engine != null) engine.dispose();
    }
}
