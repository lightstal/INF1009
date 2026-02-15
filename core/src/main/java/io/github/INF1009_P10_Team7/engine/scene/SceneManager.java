package io.github.INF1009_P10_Team7.engine.scene;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <p>Stack-based scene manager. Handles switching between scenes
 * and implements {@link SceneNavigator}.</p>
 *
 * <p>requestScene() replaces the whole stack, pushScene() overlays
 * a new scene on top, and popScene() returns to the previous one.</p>
 */
public class SceneManager implements SceneNavigator {

    private final Deque<Scene> stack = new ArrayDeque<>();

    // Full replace requested at safe frame boundary
    private Scene pendingReplace;

    // Set true only when a REPLACE happens. Consumed by GameEngine.
    private boolean sceneReplaced = false;

    /**
     * <p>Immediately replaces the current scene.</p>
     *
     * @param scene the new scene to set
     */
    @Override
    public void setScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        // treat as immediate replace
        replaceNow(scene);
    }

    /**
     * <p>Requests a scene replace that will be applied at a safe
     * frame boundary.</p>
     *
     * @param scene the new scene to switch to
     */
    @Override
    public void requestScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        pendingReplace = scene;
    }

    /**
     * <p>Pushes a scene on top of the stack. The previous scene is
     * paused but kept alive.</p>
     *
     * @param scene the scene to push
     */
    @Override
    public void pushScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");

        Scene current = getCurrentScene();
        if (current != null) {
            current.onPause();
        }

        stack.push(scene);
        scene.load();
        // DO NOT set sceneReplaced; pushing should NOT rebuild the world.
    }

    /**
     * <p>Pops the top scene and returns to the previous one.
     * The popped scene is unloaded and disposed.</p>
     */
    @Override
    public void popScene() {
        if (stack.isEmpty()) return;

        Scene top = stack.pop();
        top.unload();
        top.dispose();

        Scene nowTop = getCurrentScene();
        if (nowTop != null) {
            nowTop.onResume();
        }
        // DO NOT set sceneReplaced; popping should NOT rebuild the world.
    }

    /**
     * <p>Checks if a replace is queued so GameEngine can prepare
     * before it happens.</p>
     *
     * @return {@code true} if a replace is pending
     */
    public boolean hasPendingReplace() {
        return pendingReplace != null;
    }

    /**
     * <p>Updates the current scene. Applies any pending replace first.</p>
     *
     * @param delta time since last frame in seconds
     */
    public void update(float delta) {
        applyPendingReplace();
        Scene current = getCurrentScene();
        if (current != null) current.update(delta);
    }

    /**
     * <p>Called after movement and collision in GameEngine.
     * Lets scenes do post-movement work like boundary clamping.</p>
     *
     * @param delta time since last frame in seconds
     */
    public void lateUpdate(float delta) {
        Scene current = getCurrentScene();
        if (current != null) current.lateUpdate(delta);
    }

    /** <p>Renders the current scene.</p> */
    public void render() {
        Scene current = getCurrentScene();
        if (current != null) current.render();
    }

    /**
     * <p>Handles window resize for the current scene.</p>
     *
     * @param width  new width
     * @param height new height
     */
    public void resize(int width, int height) {
        Scene current = getCurrentScene();
        if (current != null) current.resize(width, height);
    }

    /** <p>Disposes all scenes and clears the stack.</p> */
    public void dispose() {
        clearAll();
        pendingReplace = null;
    }

    /**
     * <p>Returns the scene currently on top of the stack.</p>
     *
     * @return the current scene, or {@code null} if the stack is empty
     */
    public Scene getCurrentScene() {
        return stack.peek();
    }

    /**
     * <p>Returns true if a scene replace just happened, then resets
     * the flag. Used by GameEngine to know when to rebuild entities.</p>
     *
     * @return {@code true} if a replace occurred
     */
    public boolean consumeSceneReplacedFlag() {
        boolean v = sceneReplaced;
        sceneReplaced = false;
        return v;
    }

    /** <p>Applies the pending replace if there is one.</p> */
    private void applyPendingReplace() {
        if (pendingReplace == null) return;
        replaceNow(pendingReplace);
        pendingReplace = null;
    }

    /**
     * <p>Clears the stack, loads the new scene, and sets the
     * replace flag.</p>
     *
     * @param newScene the scene to replace with
     */
    private void replaceNow(Scene newScene) {
        clearAll();
        stack.push(newScene);
        newScene.load();
        sceneReplaced = true;
    }

    /** <p>Unloads and disposes all scenes in the stack.</p> */
    private void clearAll() {
        while (!stack.isEmpty()) {
            Scene s = stack.pop();
            s.unload();
            s.dispose();
        }
    }
}
