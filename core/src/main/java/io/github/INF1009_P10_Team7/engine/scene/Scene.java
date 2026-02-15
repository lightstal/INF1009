package io.github.INF1009_P10_Team7.engine.scene;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * <p>Abstract base class for all scenes. Scenes only receive the
 * interfaces they need (input, audio, navigation).</p>
 *
 * <p>Concrete scenes implement the hooks like onLoad(), onUpdate(),
 * onRender() etc. to define their own behaviour.</p>
 */
public abstract class Scene {

    protected final IInputController input;
    protected final IAudioController audio;
    protected final SceneNavigator nav;

    private boolean loaded = false;

    /**
     * <p>Creates a new Scene with the given dependencies.
     * Throws an exception if any dependency is null.</p>
     *
     * @param input  the input controller
     * @param audio  the audio controller
     * @param nav    the scene navigator for switching scenes
     */
    protected Scene(IInputController input, IAudioController audio, SceneNavigator nav) {
        if (input == null || audio == null || nav == null) {
            throw new IllegalArgumentException("Scene dependencies cannot be null");
        }
        this.input = input;
        this.audio = audio;
        this.nav = nav;
    }

    /**
     * <p>Called by SceneManager once when the scene becomes active.
     * Calls onLoad() only if the scene has not been loaded yet.</p>
     */
    public final void load() {
        if (!loaded) {
            loaded = true;
            onLoad();
        }
    }

    /**
     * <p>Called by SceneManager when the scene is removed or replaced.
     * Calls onUnload() only if the scene is currently loaded.</p>
     */
    public final void unload() {
        if (loaded) {
            loaded = false;
            onUnload();
        }
    }

    /**
     * <p>Called once per frame before movement and collision.</p>
     *
     * @param delta time since last frame in seconds
     */
    public final void update(float delta) {
        onUpdate(delta);
    }

    /**
     * <p>Called once per frame after movement and collision.
     * Useful for things like boundary clamping.</p>
     *
     * @param delta time since last frame in seconds
     */
    public final void lateUpdate(float delta) {
        onLateUpdate(delta);
    }

    /** <p>Called once per frame to draw the scene.</p> */
    public final void render() {
        onRender();
    }

    /**
     * <p>Called when the window is resized. Does nothing by default.</p>
     *
     * @param width  new width
     * @param height new height
     */
    public void resize(int width, int height) { }

    /** <p>Called when the scene is permanently destroyed.</p> */
    public final void dispose() {
        onDispose();
    }

    /** <p>Called when another scene is pushed on top. Does nothing by default.</p> */
    protected void onPause() { }

    /** <p>Called when this scene returns to the top after a pop. Does nothing by default.</p> */
    protected void onResume() { }

    /**
     * <p>If true, the engine pauses world updates (movement, collision)
     * while this scene is on top. Useful for pause or settings menus.</p>
     *
     * @return {@code false} by default
     */
    public boolean blocksWorldUpdate() { return false; }

    // Hooks implemented by concrete scenes

    /** <p>Set up the scene, create entities, etc.</p> */
    protected abstract void onLoad();

    /**
     * <p>Per-frame update logic before movement and collision.</p>
     *
     * @param delta time since last frame in seconds
     */
    protected abstract void onUpdate(float delta);

    /** <p>Draw the scene.</p> */
    protected abstract void onRender();

    /** <p>Clean up when the scene is removed.</p> */
    protected abstract void onUnload();

    /** <p>Clean up when the scene is permanently destroyed.</p> */
    protected abstract void onDispose();

    /**
     * <p>Per-frame update logic after movement and collision.
     * Does nothing by default.</p>
     *
     * @param delta time since last frame in seconds
     */
    protected void onLateUpdate(float delta) { }
}
