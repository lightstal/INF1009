package io.github.INF1009_P10_Team7.simulation;

import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.GameScene;
import io.github.INF1009_P10_Team7.engine.scene.MainMenuScene;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.scene.SettingsScene;

/** Concrete factory implementation for Part 1 scenes. */
public class Part1SceneFactory implements SceneFactory {
    private final InputController input;
    private final AudioController audio;
    private final SceneNavigator nav;
    private final EntityQuery entityQuery;
    private final IEntitySystem entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem movementSystem;

    public Part1SceneFactory(
        InputController input,
        AudioController audio,
        SceneNavigator nav,
        EntityQuery entityQuery,
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

    @Override
    public Scene createMainMenuScene() {
        return new MainMenuScene(input, audio, nav, this);
    }

    @Override
    public Scene createGameScene() {
        return new GameScene(input, audio, nav, entityQuery, entitySystem, collisionSystem, movementSystem, this);
    }

    @Override
    public Scene createSettingsScene() {
        return new SettingsScene(input, audio, nav, this);
    }
}
