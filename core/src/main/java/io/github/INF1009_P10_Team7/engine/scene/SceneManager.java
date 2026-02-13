package io.github.INF1009_P10_Team7.engine.scene;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SceneManager (engine layer)
 *
 * Stack-based scene system:
 * - requestScene(): REPLACES the whole stack (disposes previous scenes).
 * - pushScene(): overlays a new scene (keeps previous scene instance alive).
 * - popScene(): returns to the previous scene instance.
 *
 * Only REPLACE operations trigger a world rebuild in GameEngine.
 */
public class SceneManager implements SceneNavigator {

    private final Deque<Scene> stack = new ArrayDeque<>();

    // Full replace requested at safe frame boundary
    private Scene pendingReplace;

    // Set true only when a REPLACE happens. Consumed by GameEngine.
    private boolean sceneReplaced = false;

    @Override
    public void setScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        // treat as immediate replace
        replaceNow(scene);
    }

    @Override
    public void requestScene(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("scene cannot be null");
        pendingReplace = scene;
    }

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

    public void update(float delta) {
        applyPendingReplace();
        Scene current = getCurrentScene();
        if (current != null) current.update(delta);
    }

    public void render() {
        Scene current = getCurrentScene();
        if (current != null) current.render();
    }

    public void resize(int width, int height) {
        Scene current = getCurrentScene();
        if (current != null) current.resize(width, height);
    }

    public void dispose() {
        clearAll();
        pendingReplace = null;
    }

    public Scene getCurrentScene() {
        return stack.peek();
    }

    /** Consumed by GameEngine to know when to rebuild entities for the NEW base scene. */
    public boolean consumeSceneReplacedFlag() {
        boolean v = sceneReplaced;
        sceneReplaced = false;
        return v;
    }

    private void applyPendingReplace() {
        if (pendingReplace == null) return;
        replaceNow(pendingReplace);
        pendingReplace = null;
    }

    private void replaceNow(Scene newScene) {
        clearAll();
        stack.push(newScene);
        newScene.load();
        sceneReplaced = true;
    }

    private void clearAll() {
        while (!stack.isEmpty()) {
            Scene s = stack.pop();
            s.unload();
            s.dispose();
        }
    }
}
