package io.github.INF1009_P10_Team7.simulation.cyber;

import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.scenes.CyberGameScene;
import io.github.INF1009_P10_Team7.simulation.cyber.scenes.CyberMainMenuScene;
import io.github.INF1009_P10_Team7.simulation.cyber.scenes.CyberEndScenesFactory;
import io.github.INF1009_P10_Team7.simulation.cyber.scenes.LinuxBootScene;
import io.github.INF1009_P10_Team7.simulation.cyber.scenes.LevelSelectScene;
import io.github.INF1009_P10_Team7.simulation.SettingsScene;

/**
 * Factory for all Cyber Maze Escape scenes.
 * Supports levels 1–5; adding a level only requires updating TileMap and
 * CyberGameScene.initLevelConfig – this factory needs no changes (OCP).
 */
public class CyberSceneFactory implements SceneFactory {

    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator   nav;
    private final IEntityQuery     entityQuery;
    private final IEntitySystem    entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem  movementSystem;

    public CyberSceneFactory(IInputController input, IAudioController audio,
                              SceneNavigator nav, IEntityQuery entityQuery,
                              IEntitySystem entitySystem,
                              ICollisionSystem collisionSystem,
                              IMovementSystem movementSystem) {
        this.input           = input;
        this.audio           = audio;
        this.nav             = nav;
        this.entityQuery     = entityQuery;
        this.entitySystem    = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem  = movementSystem;
    }

    /** Boot splash shown on first launch. */
    public Scene createBootScene() {
        return new LinuxBootScene(input, audio, nav, this);
    }

    @Override
    public Scene createMainMenuScene() {
        return new CyberMainMenuScene(input, audio, nav, this);
    }

    /** Full 5-level selection screen. */
    public Scene createLevelSelectScene() {
        return new LevelSelectScene(input, audio, nav, this);
    }

    /** Default game scene – Level 1. */
    @Override
    public Scene createGameScene() {
        return createGameScene(1);
    }

    /**
     * Level-specific game scene.
     * @param level 1–5 (clamped to valid range inside TileMap)
     */
    public Scene createGameScene(int level) {
        return new CyberGameScene(input, audio, nav,
            entitySystem, collisionSystem, movementSystem, this, level);
    }

    @Override
    public Scene createSettingsScene() {
        return new SettingsScene(input, audio, nav, this);
    }

    public Scene createGameOverScene() {
        return CyberEndScenesFactory.gameOver(input, audio, nav, this);
    }

    public Scene createVictoryScene(int keys, int timeLeft, int level) {
        return CyberEndScenesFactory.victory(input, audio, nav, this, keys, timeLeft, level);
    }
}
