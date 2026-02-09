package io.github.INF1009_P10_Team7.engine.scene;

import io.github.INF1009_P10_Team7.engine.core.GameContext;

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
	
	protected final SceneManager sceneManager;
    protected final GameContext context;
    
    // Tracks whether this scene is currently active/loaded
    private boolean loaded = false;
    
    public Scene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    	this.context = sceneManager.getContext();
    }

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
    
    /**
     * Forces resource cleanup.
     * Can be called manually (e.g., when restarting) even if scene is not loaded.
     */
    public void dispose() {
        onDispose();
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
    protected abstract void onDispose();
}
