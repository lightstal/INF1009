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
 * <p>Creates scenes for the simulation and wires them with
 * the engine interfaces they need.</p>
 */
public class Part1SceneFactory implements SceneFactory {
    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator nav;
    private final IEntityQuery entityQuery;
    private final IEntitySystem entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem movementSystem;

    /**
     * @param input           input controller
     * @param audio           audio controller
     * @param nav             scene navigator
     * @param entityQuery     for looking up entities
     * @param entitySystem    for adding/removing entities
     * @param collisionSystem for registering collidables
     * @param movementSystem  for registering movement behaviours
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

    /** @return a new main menu scene */
    @Override
    public Scene createMainMenuScene() {
        return new MainMenuScene(input, audio, nav, this);
    }

    /** @return a new game scene */
    @Override
    public Scene createGameScene() {
        return new GameScene(input, audio, nav, entityQuery, entitySystem, collisionSystem, movementSystem, this);
    }

    /** @return a new settings scene */
    @Override
    public Scene createSettingsScene() {
        return new SettingsScene(input, audio, nav, this);
    }
}
