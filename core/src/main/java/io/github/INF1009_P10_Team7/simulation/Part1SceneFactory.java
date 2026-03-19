package io.github.INF1009_P10_Team7.simulation;

import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * <p>Concrete implementation of {@link SceneFactory} for the simulation.
 * Creates scenes and wires them with the engine interfaces they need.</p>
 *
 * <p>This factory holds references to all engine interfaces so that
 * each scene receives only the dependencies it requires (DIP).
 * Adding a new scene type only requires adding a new create method
 * here — no changes to the engine are needed (OCP).</p>
 */
public class Part1SceneFactory implements SceneFactory {

    /** <p>Input controller for reading keyboard and mouse input.</p> */
    private final IInputController input;

    /** <p>Audio controller for playing music and sound effects.</p> */
    private final IAudioController audio;

    /** <p>Scene navigator for switching, pushing, and popping scenes.</p> */
    private final SceneNavigator nav;

    /** <p>Entity query interface for looking up entities by name.</p> */
    private final IEntityQuery entityQuery;

    /** <p>Entity system interface for adding and removing entities.</p> */
    private final IEntitySystem entitySystem;

    /** <p>Collision system interface for registering collidable entities.</p> */
    private final ICollisionSystem collisionSystem;

    /** <p>Movement system interface for managing entity movement behaviours.</p> */
    private final IMovementSystem movementSystem;

    /**
     * <p>Constructs the factory with all engine interfaces needed by the scenes.</p>
     *
     * @param input           the input controller for reading player input
     * @param audio           the audio controller for music and sound effects
     * @param nav             the scene navigator for scene transitions
     * @param entityQuery     the entity query interface for looking up entities
     * @param entitySystem    the entity system for adding/removing entities
     * @param collisionSystem the collision system for registering collidables
     * @param movementSystem  the movement system for managing movement behaviours
     */
    public Part1SceneFactory(
        IInputController input,
        IAudioController audio,
        SceneNavigator nav,
        IEntityQuery entityQuery,
        IEntitySystem entitySystem,
        ICollisionSystem collisionSystem,
        IMovementSystem movementSystem
    ) {
        this.input = input;
        this.audio = audio;
        this.nav = nav;
        this.entityQuery = entityQuery;
        this.entitySystem = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem = movementSystem;
    }

    /**
     * <p>Creates a new main menu scene. Only requires input, audio,
     * navigation, and this factory.</p>
     *
     * @return a new {@link MainMenuScene} instance
     */
    @Override
    public Scene createMainMenuScene() {
        return new MainMenuScene(input, audio, nav, this);
    }

    /**
     * <p>Creates a new game scene with full access to the entity,
     * collision, and movement systems for gameplay.</p>
     *
     * @return a new {@link GameScene} instance
     */
    @Override
    public Scene createGameScene() {
        return new GameScene(input, audio, nav, entityQuery, entitySystem, collisionSystem, movementSystem, this);
    }

    /**
     * <p>Creates a new settings scene. Only requires input, audio,
     * navigation, and this factory.</p>
     *
     * @return a new {@link SettingsScene} instance
     */
    @Override
    public Scene createSettingsScene() {
        return new SettingsScene(input, audio, nav, this);
    }
}
