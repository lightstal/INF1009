package io.github.INF1009_P10_Team7.engine.scene;

import io.github.INF1009_P10_Team7.engine.entity.EntityDefinition;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Scene (engine layer)
 *
 * Scenes receive ONLY the interfaces they need (InputController/AudioController/SceneNavigator),
 * and they only DECLARE entity blueprints via EntityDefinition (they do not instantiate entities).
 *
 * No GameContext. No EventBus.
 */
public abstract class Scene {

    protected final InputController input;
    protected final AudioController audio;
    protected final SceneNavigator nav;

    // Part 1 rubric: scene stores the list of what entities exist + initial data.
    protected final List<EntityDefinition> entityDefinitions = new ArrayList<>();

    private boolean loaded = false;

    protected Scene(InputController input, AudioController audio, SceneNavigator nav) {
        if (input == null || audio == null || nav == null) {
            throw new IllegalArgumentException("Scene dependencies cannot be null");
        }
        this.input = input;
        this.audio = audio;
        this.nav = nav;
    }

    /** Called by SceneManager exactly once when the scene becomes active. */
    public final void load() {
        if (!loaded) {
            loaded = true;
            onLoad();
        }
    }

    /** Called by SceneManager when the scene is removed/replaced. */
    public final void unload() {
        if (loaded) {
            loaded = false;
            onUnload();
        }
    }

    /** Called once per frame by SceneManager (before movement/collision). */
    public final void update(float delta) {
        onUpdate(delta);
    }

    /**
     * Called once per frame by SceneManager AFTER movement and collision.
     * Use this for boundary clamping so it runs after entities have been moved.
     */
    public final void lateUpdate(float delta) {
        onLateUpdate(delta);
    }

    /** Called once per frame by SceneManager. */
    public final void render() {
        onRender();
    }

    /** Optional resize hook. */
    public void resize(int width, int height) { }

    /** Called by SceneManager when the scene is permanently destroyed. */
    public final void dispose() {
        onDispose();
    }

    /** Stack-based navigation hooks (default no-op). */
    protected void onPause() { }
    protected void onResume() { }

    /**
     * If true, the engine should pause world updates (movement/collision/entities) while this scene is on top.
     * Useful for Settings/Pause menus.
     */
    public boolean blocksWorldUpdate() { return false; }

    public final List<EntityDefinition> getEntityDefinitions() {
        return entityDefinitions;
    }

    // ===== Hooks implemented by concrete scenes =====
    protected abstract void onLoad();
    protected abstract void onUpdate(float delta);
    protected abstract void onRender();
    protected abstract void onUnload();
    protected abstract void onDispose();

    /** Override this for post-movement logic like boundary clamping. Default: no-op. */
    protected void onLateUpdate(float delta) { }
}
