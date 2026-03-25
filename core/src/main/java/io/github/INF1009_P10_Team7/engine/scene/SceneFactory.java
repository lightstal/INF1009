package io.github.INF1009_P10_Team7.engine.scene;

/**
 * SceneFactory — abstract factory interface for creating game scenes.
 *
 * <p>Concrete factories (e.g. {@code Part1SceneFactory}, {@code CyberSceneFactory})
 * implement this interface and hold references to all engine interfaces needed
 * by the scenes they create. Scenes call factory methods rather than using
 * {@code new} directly, keeping them decoupled from concrete scene types (OCP, DIP).</p>
 *
 * <p>To add a new scene type, create a new {@code createXxx()} method in the
 * concrete factory — no changes to this interface or to existing scenes are
 * required.</p>
 */
public interface SceneFactory {

    /**
     * Creates and returns a fresh main-menu scene.
     *
     * @return a new main-menu {@link Scene} instance
     */
    Scene createMainMenuScene();

    /**
     * Creates and returns a fresh gameplay scene.
     *
     * @return a new game {@link Scene} instance
     */
    Scene createGameScene();

    /**
     * Creates and returns a fresh settings scene.
     * The settings scene is typically pushed on top of the game scene
     * so the game is paused but not destroyed.
     *
     * @return a new settings {@link Scene} instance
     */
    Scene createSettingsScene();
}
