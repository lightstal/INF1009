package io.github.INF1009_P10_Team7.engine.scene;

/**
 * Minimal scene navigation interface.
 * Scenes depend on this abstraction (NOT SceneManager).
 */
public interface SceneNavigator {

    /** Immediately replace the current scene (useful for initial scene). */
    void setScene(Scene scene);

    /** Request a full scene replace (applied at a safe frame boundary). */
    void requestScene(Scene scene);

    /** Push a scene on top (overlay). The previous scene instance is kept alive. */
    void pushScene(Scene scene);

    /** Pop the top scene and return to the previous scene instance. */
    void popScene();
}
