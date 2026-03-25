package io.github.INF1009_P10_Team7.simulation.cyber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * CyberSprites - central registry for all game sprite textures.
 * <p>
 * Uses a Registry Pattern (Map) to store and manage textures dynamically.
 * Sprites are loaded from the assets folder at scene load time and disposed 
 * automatically when the scene unloads.
 */
public class CyberSprites implements Disposable {

    /** The dynamic registry storing all active textures by their String key. */
    private final Map<String, Texture> textureRegistry;
    /** Whether the textures have been loaded from disk. Guards against double-loading. */
    private boolean loaded = false;

    public CyberSprites() {
        this.textureRegistry = new HashMap<>();
    }

    // =========================================================================
    // LOAD / DISPOSE
    // =========================================================================

    /**
     * Load all sprite textures from disk into the registry.
     * Call once inside Scene.onLoad().
     */
    public void load() {
        if (loaded) return;
        
        try {
            // 1. Read the JSON file from the assets folder
            JsonReader reader = new JsonReader();
            JsonValue spriteData = reader.parse(Gdx.files.internal("sprites.json"));

            // 2. Loop through every entry in the JSON file
            for (JsonValue entry : spriteData) {
                String key = entry.name;           // e.g., "player"
                String path = entry.asString();    // e.g., "player.png"
                
                // 3. Register it using your existing helper
                registerTexture(key, path);
            }
            
            loaded = true;
            Gdx.app.log("CyberSprites", "Successfully loaded " + textureRegistry.size() + " sprites from JSON.");
            
        } catch (Exception e) {
            Gdx.app.error("CyberSprites", "Failed to parse sprites.json: " + e.getMessage());
        }
    }

    /**
     * Safely clears and disposes of every texture in the registry.
     */
    @Override
    public void dispose() {
        for (Texture tex : textureRegistry.values()) {
            disposeTexture(tex);
        }
        textureRegistry.clear();
        loaded = false;
        Gdx.app.log("CyberSprites", "All sprite textures disposed.");
    }

    public boolean isLoaded() { 
        return loaded; 
    }

    // =========================================================================
    // REGISTRY ACCESS
    // =========================================================================

    /**
     * Retrieves a texture from the registry by its key.
     * @param key The string identifier (e.g., "player").
     * @return The Texture, or null if it was not found/loaded.
     */
    public Texture get(String key) {
        Texture tex = textureRegistry.get(key);
        if (tex == null) {
            Gdx.app.error("CyberSprites", "WARNING: Texture not found in registry: " + key);
        }
        return tex;
    }

    /**
     * Internal helper to load and store a texture in the registry.
     */
    private void registerTexture(String key, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textureRegistry.put(key, t);
            } else {
                Gdx.app.error("CyberSprites", "Missing file: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("CyberSprites", "Failed to load: " + path + " - " + e.getMessage());
        }
    }

    // =========================================================================
    // DRAW HELPERS
    // =========================================================================

    /**
     * Draw a texture retrieved via its Registry Key, centred at world position (cx, cy).
     */
    public void drawCentered(SpriteBatch batch, String textureKey,
                             float cx, float cy, float size, float alpha) {
        Texture tex = get(textureKey);
        if (tex == null) return;
        
        batch.setColor(1f, 1f, 1f, alpha);
        float half = size * 0.5f;
        batch.draw(tex, cx - half, cy - half, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Draw a texture retrieved via its Registry Key, centred at (cx, cy), rotated by angleDeg.
     */
    public void drawCenteredRotated(SpriteBatch batch, String textureKey,
                                    float cx, float cy, float size,
                                    float angleDeg, float alpha) {
        Texture tex = get(textureKey);
        if (tex == null) return;
        
        batch.setColor(1f, 1f, 1f, alpha);
        float half = size * 0.5f;
        batch.draw(tex,
            cx - half, cy - half,      // position
            half, half,                // origin
            size, size,                // size
            1f, 1f,                    // scale
            angleDeg,                  // rotation
            0, 0,                      // src x,y
            tex.getWidth(), tex.getHeight(),
            false, false);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void disposeTexture(Texture t) {
        if (t != null) try { t.dispose(); } catch (Exception ignored) {}
    }
}