package io.github.INF1009_P10_Team7.engine.scene;

/**
 * SceneNavigator — the scene-transition interface exposed to scenes.
 *
 * <p>Scenes receive only this narrow interface (ISP / DIP) rather than
 * the full {@link SceneManager}. This prevents scenes from accidentally
 * calling internal lifecycle methods and keeps the coupling minimal.</p>
 *
 * <p>Three transition modes:</p>
 * <ul>
 *   <li>{@link #setScene}     — immediate full-stack replace (use sparingly;
 *       prefer {@link #requestScene} to avoid mid-frame issues)</li>
 *   <li>{@link #requestScene} — deferred full-stack replace at a safe frame
 *       boundary; engine clears all managers before the new scene loads</li>
 *   <li>{@link #pushScene}    — overlay a scene on top without destroying
 *       the one underneath (e.g. settings menu over the game)</li>
 *   <li>{@link #popScene}     — remove the top overlay and resume the scene
 *       beneath it</li>
 * </ul>
 */
public interface SceneNavigator {

    /**
     * Immediately replaces the entire scene stack with {@code scene}.
     * Prefer {@link #requestScene} for most transitions to avoid
     * operating on the stack mid-frame.
     *
     * @param scene the new scene; must not be {@code null}
     */
    void setScene(Scene scene);

    /**
     * Requests a full-stack scene replace that is applied safely at the
     * start of the next frame. The engine will clear all sub-systems
     * (entities, movement, collision) before loading the new scene so
     * that its {@code onLoad()} sees a clean state.
     *
     * @param scene the scene to switch to; must not be {@code null}
     */
    void requestScene(Scene scene);

    /**
     * Pushes {@code scene} on top of the current stack. The current scene
     * is paused (its {@code onPause()} is called) but kept alive.
     * World updates are still running unless the new scene overrides
     * {@link Scene#blocksWorldUpdate()}.
     *
     * @param scene the scene to push; must not be {@code null}
     */
    void pushScene(Scene scene);

    /**
     * Pops the top scene from the stack and resumes the one beneath it.
     * The popped scene is unloaded and disposed.
     */
    void popScene();
}
