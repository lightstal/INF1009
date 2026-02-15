package io.github.INF1009_P10_Team7.engine.scene;

/**
 * <p>Interface for scene navigation. Scenes depend on this
 * instead of the concrete SceneManager.</p>
 */
public interface SceneNavigator {

    /**
     * <p>Immediately replaces the current scene.</p>
     *
     * @param scene the new scene
     */
    void setScene(Scene scene);

    /**
     * <p>Requests a scene replace at a safe frame boundary.</p>
     *
     * @param scene the new scene
     */
    void requestScene(Scene scene);

    /**
     * <p>Pushes a scene on top. The previous scene is kept alive.</p>
     *
     * @param scene the scene to push
     */
    void pushScene(Scene scene);

    /** <p>Pops the top scene and returns to the previous one.</p> */
    void popScene();
}
