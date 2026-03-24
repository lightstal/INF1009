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
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
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
	
	// Toggle this to true when you want to bypass the main game and load Part1Simulation scene
    private static final boolean IS_TESTING = false;

    /** <p>The game engine that manages all engine subsystems.</p> */
    private GameEngine engine;

    /**
     * <p>Called once when the application starts. Initialises the engine,
     * binds keys to actions, creates the scene factory, and sets the
     * initial scene to the main menu.</p>
     */
    @Override
    public void create() {
    	if (!IS_TESTING) {
    		Gdx.app.log("SIM", "Cyber Maze Escape app create(): start (engine init)");
    	} else {
    		 Gdx.app.log("SIM", "Part1SimulationApp create(): start (engine init)");
    	}
    		
    		
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
        
        if (!IS_TESTING) {
	        // Parameter format: bindInput(String action, int deviceID, int keycode)
	        // Note: Device ID 0 = Keyboard
	        input.bindInput("START_GAME",  0, Input.Keys.SPACE);
	        input.bindInput("SETTINGS",    0, Input.Keys.ESCAPE);
	        input.bindInput("LEFT",        0, Input.Keys.A);
	        input.bindInput("RIGHT",       0, Input.Keys.D);
	        input.bindInput("UP",          0, Input.Keys.W);
	        input.bindInput("DOWN",        0, Input.Keys.S);
	        input.bindInput("INTERACT",    0, Input.Keys.E);
	        input.bindInput("HELP",        0, Input.Keys.H);
	
	        // Menu navigation bindings kept separate from gameplay bindings
	        input.bindInput("MENU_LEFT",   0, Input.Keys.LEFT);
	        input.bindInput("MENU_RIGHT",  0, Input.Keys.RIGHT);
	        input.bindInput("MENU_CONFIRM",0, Input.Keys.ENTER);
	        input.bindInput("MENU_BACK",   0, Input.Keys.ESCAPE);
	        

	        // Create the Cyber Maze Escape scene factory (Part 2 game)
	        CyberSceneFactory factory = new CyberSceneFactory(
	            input, audio, nav, entityQuery,
	            entitySystem, collisionSystem, movementSystem
	        );
	        
	        // Launch with the Linux boot splash, then → main menu → level select → game
	        nav.setScene(factory.createBootScene());
	        
        } else {
        	// Bind simulation-specific keys to named actions
            input.bindInput("START_GAME",   0, Input.Keys.SPACE);
            input.bindInput("RESTART_GAME", 0, Input.Keys.R);
            input.bindInput("SETTINGS",     0, Input.Keys.ESCAPE);
            input.bindInput("BACK",         0, Input.Keys.BACKSPACE);
            input.bindInput("LEFT",         0, Input.Keys.A);
            input.bindInput("RIGHT",        0, Input.Keys.D);
            input.bindInput("UP",           0, Input.Keys.W);
            input.bindInput("DOWN",         0, Input.Keys.S);
            input.bindInput("SHOOT",        1, Input.Buttons.LEFT);
            

            // Create the Cyber Maze Escape scene factory (Part 2 game)
            SceneFactory factory = new Part1SceneFactory(
                input, audio, nav, entityQuery,
                entitySystem, collisionSystem, movementSystem
            );

            // Set the initial scene to the main menu
            nav.setScene(factory.createMainMenuScene());
        }
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
    	if (!IS_TESTING) {
    		Gdx.app.log("SIM", "Cyber Maze Escape app dispose(): clean shutdown");
    	} else {
    		Gdx.app.log("SIM", "Part1SimulationApp dispose(): clean shutdown");
    	}
        if (engine != null) engine.dispose();
    }
}
