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
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;

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
        Gdx.app.log("SIM", "Cyber Maze Escape app create(): start (engine init)");

        // Print control instructions and scaling note to the console
        SimulationTestScript.printInstructions();
        SimulationTestScript.printScalingNote();

        // Create the engine (initialises all subsystems)
        engine = new GameEngine();

        // Retrieve engine interfaces (Dependency Inversion  -  depend on abstractions)
        IInputController input = engine.getInput();
        IAudioController audio = engine.getAudio();
        SceneNavigator nav = engine.getNavigator();
        IEntityQuery entityQuery = engine.getEntityQuery();
        IEntitySystem entitySystem = engine.getEntitySystem();
        ICollisionSystem collisionSystem = engine.getCollisionSystem();
        IMovementSystem movementSystem = engine.getMovementSystem();

        // Key bindings for Cyber Maze Escape
        input.bindKey("START_GAME",  Input.Keys.SPACE);
        input.bindKey("SETTINGS",    Input.Keys.ESCAPE);
        input.bindKey("BACK",        Input.Keys.Q);
        input.bindKey("LEFT",        Input.Keys.A);
        input.bindKey("RIGHT",       Input.Keys.D);
        input.bindKey("UP",          Input.Keys.W);
        input.bindKey("DOWN",        Input.Keys.S);
        input.bindKey("INTERACT",    Input.Keys.E);
        input.bindKey("HELP",        Input.Keys.H);

        // Menu navigation bindings kept separate from gameplay bindings
        input.bindKey("MENU_LEFT",   Input.Keys.LEFT);
        input.bindKey("MENU_RIGHT",  Input.Keys.RIGHT);
        input.bindKey("MENU_CONFIRM",Input.Keys.ENTER);
        input.bindKey("MENU_BACK",   Input.Keys.ESCAPE);

        // Create the Cyber Maze Escape scene factory (Part 2 game)
        CyberSceneFactory factory = new CyberSceneFactory(
            input, audio, nav, entityQuery,
            entitySystem, collisionSystem, movementSystem
        );

        // Launch with the Linux boot splash, then → main menu → level select → game
        nav.setScene(factory.createBootScene());
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
        Gdx.app.log("SIM", "Cyber Maze Escape app dispose(): clean shutdown");
        if (engine != null) engine.dispose();
    }
}
