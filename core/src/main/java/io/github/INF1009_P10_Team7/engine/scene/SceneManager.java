package io.github.INF1009_P10_Team7.engine.scene;

import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutput;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;

/**
 * SceneManager (UML requirement)
 *
 * Purpose:
 * - Holds ONE current scene (active).
 * - Holds ONE pending scene (requested next).
 * - Switches scenes safely at the start of update().
 *
 * Why pendingScene?
 * - Avoid switching scenes in the middle of update/render (can be unstable).
 */
public class SceneManager {

    // Current running scene
    private Scene currentScene;

    // Next requested scene (applied at start of update)
    private Scene pendingScene;

    private final InputOutput io;
    private final EntityManager entityManager;

    public SceneManager(InputOutput io, EntityManager entityManager) {
        this.io = io;
        this.entityManager = entityManager;
    }

    public InputOutput getInputOutput() {
        return io;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Immediately set a scene (good for starting scene).
     */
    public void setScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        pendingScene = scene;
        applyPendingScene(); // apply immediately
    }

    /**
     * Request a scene switch (applied on next update()).
     */
    public void requestScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        pendingScene = scene;
    }

    /**
     * Called every frame:
     * 1) apply pending scene if any
     * 2) update current scene
     */
    public void update(float delta) {
        applyPendingScene();
        if (currentScene != null) {
            currentScene.update(delta);
        }
    }

    /**
     * Called every frame to render current scene.
     */
    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }

    /**
     * Forward window resize to current scene.
     */
    public void resize(int width, int height) {
        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }

    /**
     * Clean shutdown:
     * unload current scene and clear references.
     */
    public void dispose() {
        if (currentScene != null) {
            currentScene.unload();
            currentScene = null;
        }
        pendingScene = null;
    }

    /**
     * Internal helper:
     * unload current -> switch -> load new
     */
    private void applyPendingScene() {
        if (pendingScene == null) return;

        // Unload old scene
        if (currentScene != null) {
            currentScene.unload();
        }

        // Switch reference
        currentScene = pendingScene;
        pendingScene = null;

        // Load new scene
        currentScene.load();
    }
}
