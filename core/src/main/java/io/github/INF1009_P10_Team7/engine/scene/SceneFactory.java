package io.github.INF1009_P10_Team7.engine.scene;

/**
 * <p>Factory interface for creating scenes without exposing
 * how dependencies are wired. Implemented in the simulation layer.</p>
 */
public interface SceneFactory {

    /** @return a new main menu scene */
    Scene createMainMenuScene();

    /** @return a new game scene */
    Scene createGameScene();

    /**
     * <p>Creates the settings scene. It should return to
     * GameScene via the factory (fresh instance).</p>
     *
     * @return a new settings scene
     */
    Scene createSettingsScene();
}
