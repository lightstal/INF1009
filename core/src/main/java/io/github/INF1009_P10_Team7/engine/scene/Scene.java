package io.github.INF1009_P10_Team7.engine.scene;

/**
 * Abstract Scene class (UML requirement).
 *
 * Purpose:
 * - Provide a consistent lifecycle that ALL scenes must follow.
 * - Keep engine generic (no game-specific logic here).
 *
 * Lifecycle:
 * load()   -> called once when activated
 * update() -> called every frame
 * render() -> called every frame
 * unload() -> called once when deactivated
 */
public abstract class Scene {

    // Tracks whether this scene is currently active/loaded
    private boolean loaded = false;

    // Called once when the scene becomes active
    public final void load() {
        if (loaded) return;     // prevent double-load
        loaded = true;
        onLoad();               // run scene-specific setup
    }

    // Called every frame
    public final void update(float delta) {
        if (!loaded) return;    // don't update if not active
        onUpdate(delta);
    }

    // Called every frame
    public final void render() {
        if (!loaded) return;    // don't render if not active
        onRender();
    }

    // Called once when the scene is replaced / deactivated
    public final void unload() {
        if (!loaded) return;    // prevent double-unload
        loaded = false;
        onUnload();             // run scene-specific cleanup
    }

    // Optional: called when window size changes
    public void resize(int width, int height) {}

    // Debug helper
    public final boolean isLoaded() {
        return loaded;
    }

    // ---- Hooks (child scenes MUST implement these) ----
    protected abstract void onLoad();
    protected abstract void onUpdate(float delta);
    protected abstract void onRender();
    protected abstract void onUnload();
}
