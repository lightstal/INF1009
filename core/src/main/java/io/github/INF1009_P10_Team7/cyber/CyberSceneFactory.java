package io.github.INF1009_P10_Team7.cyber;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.IEntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneFactory;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberEndScenesFactory;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberGameScene;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberMainMenuScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LevelCutsceneScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LevelSelectScene;
import io.github.INF1009_P10_Team7.cyber.scenes.LinuxBootScene;

/**
 * Factory for all Cyber Maze Escape scenes.
 *
 * <p>Level configs are registered in a {@code Map<Integer, Supplier<LevelConfig>>}
 * so adding a new level requires only one line here — the engine and
 * {@link CyberGameScene} never change (OCP).</p>
 *
 * <p>Example: to add Level 3, just add:
 * {@code levelConfigs.put(3, Level3Config::new);}</p>
 */
/**
 * CyberSceneFactory — concrete {@link io.github.INF1009_P10_Team7.engine.scene.SceneFactory}
 * for the Cyber Maze Escape game.
 *
 * <p>Creates all scenes for the cyber-game flow and holds references to all
 * engine interfaces needed by those scenes. Adding a new scene type requires
 * only a new {@code createXxx()} method here — no changes to the engine or
 * to existing scenes (OCP).</p>
 *
 * <p>Scene flow managed by this factory:</p>
 * <pre>
 * LinuxBootScene → CyberMainMenuScene → LevelSelectScene
 *    → LevelCutsceneScene → CyberGameScene
 *    → CyberEndScenes (victory / game-over)
 * </pre>
 */
public class CyberSceneFactory implements SceneFactory {

    private final IInputController  input;
    private final IAudioController  audio;
    private final SceneNavigator    nav;
    private final IEntityQuery      entityQuery;
    private final IEntitySystem     entitySystem;
    private final ICollisionSystem  collisionSystem;
    private final IMovementSystem   movementSystem;
    private final Supplier<Scene>   settingsSceneSupplier;

    /**
     * Registry of level number → LevelConfig supplier (OCP).
     * Adding a new level = one line. No if/else chains needed.
     */
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

        // Register all available levels — add new levels here only (OCP)
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
    /**
     * Creates a Level 1 game scene (SceneFactory contract implementation).
     * Delegates to {@link #createGameScene(int)} with level index 1.
     */
    public Scene createGameScene() {
        return createGameScene(1);
    }

    /**
     * Creates a game scene for the given level number.
     * Falls back to Level 1 if the level number is not registered.
     */
    public Scene createGameScene(int level) {
        Supplier<LevelConfig> supplier = levelConfigs.getOrDefault(level, Level1Config::new);
        LevelConfig config = supplier.get();
        return new CyberGameScene(input, audio, nav,
            entitySystem, collisionSystem, movementSystem, this, config);
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

    /**
     * Passes all scoring parameters through to CyberVictoryScene.
     */
    public Scene createVictoryScene(int keys, int keysRequired, int timeLeft, int level) {
        return createVictoryScene(keys, keysRequired, timeLeft, level, 0, 0);
    }

    public Scene createVictoryScene(int keys, int keysRequired, int timeLeft, int level,
                                    int respawnsUsed, int hintsUsed) {
        return CyberEndScenesFactory.victory(input, audio, nav, this,
            keys, keysRequired, timeLeft, level, respawnsUsed, hintsUsed);
    }
}
