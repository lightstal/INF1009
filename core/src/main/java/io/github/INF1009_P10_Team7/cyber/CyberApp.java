package io.github.INF1009_P10_Team7.cyber;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.INF1009_P10_Team7.cyber.scenes.SettingsScene;
import io.github.INF1009_P10_Team7.engine.core.GameEngine;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * CyberApp, standalone application entry for the Cyber game.
 *
 * <p>This is the composition root for the cyber package: it wires the engine,
 * binds input actions, constructs the {@link CyberSceneFactory}, and launches
 * the boot scene. It intentionally has no dependency on the simulation module
 * so the simulation package can be deleted safely.</p>
 */
public class CyberApp extends ApplicationAdapter {

    private GameEngine engine;

    @Override
    public void create() {
        Gdx.app.log("CyberMazeEscape", "CyberApp starting...");

        engine = new GameEngine();

        IInputController   input           = engine.getInput();
        IAudioController   audio           = engine.getAudio();
        SceneNavigator     nav             = engine.getNavigator();
        IEntityQuery       entityQuery     = engine.getEntityQuery();
        IEntitySystem      entitySystem    = engine.getEntitySystem();
        ICollisionSystem   collisionSystem = engine.getCollisionSystem();
        IMovementSystem    movementSystem  = engine.getMovementSystem();

        // Key bindings: deviceID 0 = Keyboard, deviceID 1 = Mouse
        input.bindInput("START_GAME",   0, Input.Keys.SPACE);
        input.bindInput("BOOT_SKIP",    1, Input.Buttons.LEFT);
        input.bindInput("MENU_CLICK",   1, Input.Buttons.LEFT);
        input.bindInput("SETTINGS",     0, Input.Keys.ESCAPE);
        input.bindInput("LEFT",         0, Input.Keys.A);
        input.bindInput("RIGHT",        0, Input.Keys.D);
        input.bindInput("UP",           0, Input.Keys.W);
        input.bindInput("DOWN",         0, Input.Keys.S);
        input.bindInput("INTERACT",     0, Input.Keys.E);
        input.bindInput("HELP",         0, Input.Keys.H);

        input.bindInput("MENU_LEFT",    0, Input.Keys.LEFT);
        input.bindInput("MENU_RIGHT",   0, Input.Keys.RIGHT);
        input.bindInput("MENU_CONFIRM", 0, Input.Keys.ENTER);
        input.bindInput("MENU_BACK",    0, Input.Keys.ESCAPE);

        // Scene factory wiring
        // Settings scene lives under cyber.scenes to avoid simulation coupling.
        final CyberSceneFactory[] factoryRef = new CyberSceneFactory[1];
        factoryRef[0] = new CyberSceneFactory(
            input, audio, nav, entityQuery,
            entitySystem, collisionSystem, movementSystem,
            () -> new SettingsScene(input, audio, nav, factoryRef[0])
        );
        CyberSceneFactory factory = factoryRef[0];

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

