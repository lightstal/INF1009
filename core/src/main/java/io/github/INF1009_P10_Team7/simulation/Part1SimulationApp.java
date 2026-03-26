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
import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;

/**
 * Composition root for Cyber Maze Escape.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates the {@link GameEngine}</li>
 *   <li>Binds all keyboard actions to named action strings</li>
 *   <li>Wires scenes using ONLY interfaces (Dependency Inversion Principle)</li>
 *   <li>Launches the boot scene to start the application flow</li>
 *   <li>Delegates the game loop (update/render/resize/dispose) to the engine</li>
 * </ul>
 */
public class Part1SimulationApp extends ApplicationAdapter {

    private GameEngine engine;

    @Override
    public void create() {
        Gdx.app.log("CyberMazeEscape", "Application starting...");

        engine = new GameEngine();

        // Retrieve engine interfaces — depend on abstractions, not concretions (DIP)
        IInputController  input          = engine.getInput();
        IAudioController  audio          = engine.getAudio();
        SceneNavigator    nav            = engine.getNavigator();
        IEntityQuery      entityQuery    = engine.getEntityQuery();
        IEntitySystem     entitySystem   = engine.getEntitySystem();
        ICollisionSystem  collisionSystem = engine.getCollisionSystem();
        IMovementSystem   movementSystem = engine.getMovementSystem();

        // ── Gameplay key bindings ────────────────────────────────────────────
        // bindInput(actionName, deviceID, keycode)  — deviceID 0 = Keyboard
        // ── Key bindings: deviceID 0 = Keyboard, deviceID 1 = Mouse ──────────────
        // NOTE: "BACK" (Q key) is checked in GameScene but not bound here.
        // Add: input.bindInput("BACK", 0, Input.Keys.Q); to enable that transition.
        input.bindInput("START_GAME",   0, Input.Keys.SPACE);
        input.bindInput("SETTINGS",     0, Input.Keys.ESCAPE);
        input.bindInput("LEFT",         0, Input.Keys.A);
        input.bindInput("RIGHT",        0, Input.Keys.D);
        input.bindInput("UP",           0, Input.Keys.W);
        input.bindInput("DOWN",         0, Input.Keys.S);
        input.bindInput("INTERACT",     0, Input.Keys.E);
        input.bindInput("HELP",         0, Input.Keys.H);

        // ── Menu navigation bindings ─────────────────────────────────────────
        input.bindInput("MENU_LEFT",    0, Input.Keys.LEFT);
        input.bindInput("MENU_RIGHT",   0, Input.Keys.RIGHT);
        input.bindInput("MENU_CONFIRM", 0, Input.Keys.ENTER);
        input.bindInput("MENU_BACK",    0, Input.Keys.ESCAPE);

        // ── Scene factory wiring ─────────────────────────────────────────────
        // SettingsScene is owned by simulation and injected into cyber via a
        // Supplier so that cyber never imports any simulation code directly.
        final CyberSceneFactory[] factoryRef = new CyberSceneFactory[1];
        factoryRef[0] = new CyberSceneFactory(
            input, audio, nav, entityQuery,
            entitySystem, collisionSystem, movementSystem,
            () -> new SettingsScene(input, audio, nav, factoryRef[0])
        );
        CyberSceneFactory factory = factoryRef[0];

        // Boot → Main Menu → Level Select → Cutscene → Game
        nav.setScene(factory.createBootScene());

        Gdx.app.log("CyberMazeEscape", "Engine initialised. Launching boot scene.");
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
        Gdx.app.log("CyberMazeEscape", "Disposing engine — clean shutdown.");
        if (engine != null) engine.dispose();
    }
}
