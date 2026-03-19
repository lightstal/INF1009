package io.github.INF1009_P10_Team7.simulation;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.INF1009_P10_Team7.engine.core.GameEngine;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * <p>The composition root of the simulation. This is the entry point
 * that LibGDX calls to start the application.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates the {@link GameEngine}</li>
 *   <li>Configures simulation-specific keybinds</li>
 *   <li>Wires scenes using ONLY interfaces (Dependency Inversion Principle)</li>
 *   <li>Starts the initial scene (main menu)</li>
 *   <li>Delegates the game loop (update/render/resize/dispose) to the engine</li>
 * </ul>
 */
public class Part1SimulationApp extends ApplicationAdapter {

    /** <p>The game engine that manages all engine subsystems.</p> */
    private GameEngine engine;

    /**
     * <p>Called once when the application starts. Initialises the engine,
     * binds keys to actions, creates the scene factory, and sets the
     * initial scene to the main menu.</p>
     */
    @Override
    public void create() {
        Gdx.app.log("SIM", "Part1SimulationApp create(): start (engine init)");

        // Print control instructions and scaling note to the console
        SimulationTestScript.printInstructions();
        SimulationTestScript.printScalingNote();

        // Create the engine (initialises all subsystems)
        engine = new GameEngine();

        // Retrieve engine interfaces (Dependency Inversion — depend on abstractions)
        IInputController input = engine.getInput();
        IAudioController audio = engine.getAudio();
        SceneNavigator nav = engine.getNavigator();
        IEntityQuery entityQuery = engine.getEntityQuery();
        IEntitySystem entitySystem = engine.getEntitySystem();
        ICollisionSystem collisionSystem = engine.getCollisionSystem();
        IMovementSystem movementSystem = engine.getMovementSystem();

        // Bind simulation-specific keys to named actions
        input.bindKey("START_GAME", Input.Keys.SPACE);
        input.bindKey("RESTART_GAME", Input.Keys.R);
        input.bindKey("SETTINGS", Input.Keys.ESCAPE);
        input.bindKey("BACK", Input.Keys.BACKSPACE);
        input.bindKey("LEFT", Input.Keys.A);
        input.bindKey("RIGHT", Input.Keys.D);
        input.bindKey("UP", Input.Keys.W);
        input.bindKey("DOWN", Input.Keys.S);
        input.bindMouseButton("SHOOT", Input.Buttons.LEFT);

        // Create the scene factory, wired with all engine interfaces
        SceneFactory factory = new Part1SceneFactory(
            input, audio, nav, entityQuery,
            entitySystem, collisionSystem, movementSystem
        );

        // Set the initial scene to the main menu
        nav.setScene(factory.createMainMenuScene());
    }

    /**
     * <p>Called every frame by LibGDX. Computes delta time and delegates
     * the update and render passes to the engine.</p>
     */
    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        engine.update(dt);
        engine.render();
    }

    /**
     * <p>Called when the window is resized. Forwards the new dimensions
     * to the engine so viewports can be updated.</p>
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    /**
     * <p>Called when the application is closing. Disposes of the engine
     * and all its subsystems for a clean shutdown.</p>
     */
    @Override
    public void dispose() {
        Gdx.app.log("SIM", "Part1SimulationApp dispose(): clean shutdown");
        if (engine != null) engine.dispose();
    }
}
