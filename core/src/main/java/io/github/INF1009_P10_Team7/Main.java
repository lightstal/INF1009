package io.github.INF1009_P10_Team7;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.engine.scene.MainMenuScene;
import io.github.INF1009_P10_Team7.engine.core.ContextImplementation;
import io.github.INF1009_P10_Team7.engine.core.GameContext;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;
import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;
import io.github.INF1009_P10_Team7.engine.movement.MovementManager;  // ← ADD THIS IMPORT

/**
 * Main (future real game entry point).
 *
 * For Part 1, you can run Part1SimulationApp instead.
 * For later parts, you can build your actual game flow here.
 */
public class Main extends ApplicationAdapter {

    private SceneManager sceneManager;
    private InputOutputManager inputOutputManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;  // ← ADD THIS FIELD
    private EventBus eventBus;
    
    @Override
    public void create() {
        eventBus = new EventBus();
        inputOutputManager = new InputOutputManager(eventBus);

        // Initialize CollisionManager
        collisionManager = new CollisionManager(inputOutputManager);
        collisionManager.setCollisionSound("bell.mp3");
        Gdx.app.log("Main", "CollisionManager initialized");

        // Initialize MovementManager - ADD THESE 2 LINES
        movementManager = new MovementManager();
        Gdx.app.log("Main", "MovementManager initialized");

        GameContext context = new ContextImplementation(
            eventBus,
            inputOutputManager,
            collisionManager,
            movementManager
        );

        sceneManager = new SceneManager(context);

        // Start with MainMenu scene
        sceneManager.setScene(new MainMenuScene(sceneManager));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        float dt = Gdx.graphics.getDeltaTime();

        inputOutputManager.update();
        
        // ADD THIS LINE
        movementManager.updateAll(dt);

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
        if (collisionManager != null) {
            collisionManager.clear();
            Gdx.app.log("Main", "CollisionManager cleared");
        }
        // ADD THESE 4 LINES
        if (movementManager != null) {
            movementManager.clear();
            Gdx.app.log("Main", "MovementManager cleared");
        }
        if (inputOutputManager != null) {
            inputOutputManager.dispose();
        }
    }
}