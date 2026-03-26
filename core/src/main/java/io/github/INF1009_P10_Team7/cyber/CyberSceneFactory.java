package io.github.INF1009_P10_Team7.cyber;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.map.ILevelMapRuntime;
import io.github.INF1009_P10_Team7.engine.map.tiled.TiledLevelMapRuntime;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.cyber.level.LevelConfig;
import io.github.INF1009_P10_Team7.cyber.level.Level1Config;
import io.github.INF1009_P10_Team7.cyber.level.Level2Config;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberEndScenesFactory;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberGameScene;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberMainMenuScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LevelCutsceneScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LevelSelectScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LinuxBootScene;

public class CyberSceneFactory implements SceneFactory {

    private final IInputController  input;
    private final IAudioController  audio;
    private final SceneNavigator    nav;
    private final IEntityQuery      entityQuery;
    private final IEntitySystem     entitySystem;
    private final ICollisionSystem  collisionSystem;
    private final IMovementSystem   movementSystem;
    private final Supplier<Scene>   settingsSceneSupplier;

    private final Map<Integer, Supplier<LevelConfig>> levelConfigs = new HashMap<>();

    public CyberSceneFactory(IInputController input, IAudioController audio,
                              SceneNavigator nav, IEntityQuery entityQuery,
                              IEntitySystem entitySystem,
                              ICollisionSystem collisionSystem,
                              IMovementSystem movementSystem,
                              Supplier<Scene> settingsSceneSupplier) {
        this.input                 = input;
        this.audio                 = audio;
        this.nav                   = nav;
        this.entityQuery           = entityQuery;
        this.entitySystem          = entitySystem;
        this.collisionSystem       = collisionSystem;
        this.movementSystem        = movementSystem;
        this.settingsSceneSupplier = settingsSceneSupplier;

        levelConfigs.put(1, Level1Config::new);
        levelConfigs.put(2, Level2Config::new);
    }

    public Scene createBootScene() {
        return new LinuxBootScene(input, audio, nav, this);
    }

    @Override
    public Scene createMainMenuScene() {
        return new CyberMainMenuScene(input, audio, nav, this);
    }

    public Scene createLevelSelectScene() {
        return new LevelSelectScene(input, audio, nav, this);
    }

    @Override
    public Scene createGameScene() {
        return createGameScene(1);
    }

    public Scene createGameScene(int level) {
        Supplier<LevelConfig> supplier = levelConfigs.getOrDefault(level, Level1Config::new);
        LevelConfig config = supplier.get();
        ILevelMapRuntime mapRuntime = new TiledLevelMapRuntime(
            config.getMapFile(),
            config.getCollisionLayer(),
            config.getWallLayer(),
            config.getDoorLayer(),
            "terminal",
            TileMap.COLS,
            TileMap.ROWS,
            TileMap.TILE_SIZE
        );
        return new CyberGameScene(input, audio, nav,
            entitySystem, collisionSystem, movementSystem, this, config, mapRuntime);
    }

    public Scene createCutsceneScene(int level) {
        return LevelCutsceneScene.create(input, audio, nav, this, level);
    }

    @Override
    public Scene createSettingsScene() {
        return settingsSceneSupplier.get();
    }

    public Scene createGameOverScene() {
        return createGameOverScene(1);
    }

    public Scene createGameOverScene(int level) {
        return CyberEndScenesFactory.gameOver(input, audio, nav, this, level);
    }

    public Scene createVictoryScene(int keys, int keysRequired, int timeLeft, int level) {
        return createVictoryScene(keys, keysRequired, timeLeft, level, 0, 0);
    }

    public Scene createVictoryScene(int keys, int keysRequired, int timeLeft, int level,
                                    int respawnsUsed, int hintsUsed) {
        return CyberEndScenesFactory.victory(input, audio, nav, this,
            keys, keysRequired, timeLeft, level, respawnsUsed, hintsUsed);
    }
}