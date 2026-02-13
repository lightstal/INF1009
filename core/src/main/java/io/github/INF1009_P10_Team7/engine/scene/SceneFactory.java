package io.github.INF1009_P10_Team7.engine.scene;

/**
 * Factory interface for creating scenes without exposing how dependencies are wired.
 * Implemented in the simulation layer.
 */
public interface SceneFactory {
    Scene createMainMenuScene();
    Scene createGameScene();
    /** Create the settings scene. It should return to GameScene via the factory (fresh instance). */
    Scene createSettingsScene();
}
