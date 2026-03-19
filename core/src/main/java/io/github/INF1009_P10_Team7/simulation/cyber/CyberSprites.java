package io.github.INF1009_P10_Team7.simulation.cyber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * CyberSprites  -  central loader for all game sprite textures.
 *
 * Sprites are loaded from the assets folder at scene load time
 * and disposed when the scene unloads. Use drawCentered() helpers
 * to render sprites aligned to world-space positions.
 *
 * Sprite catalogue:
 *   terminal      -  hacking terminal station (shown on terminal tiles)
 *   player        -  top-down player character
 *   hunter        -  the hunter / security guard enemy
 *   osint         -  OSINT challenge icon
 *   phoneWifi     -  wifi/network device icon
 *   camera        -  surveillance camera icon
 *   mapPin        -  map / location icon
 *   secCamera     -  security camera on tripod (used for room decoration)
 *   lightBulb     -  hanging light bulb (room prop)
 *   ceilingLight  -  ceiling light strip (room prop)
 *   barrierLg     -  large barrier / crate
 *   barrierSm     -  small barrier / crate
 */
public class CyberSprites implements Disposable {

    // ---- Public texture references (null until load() is called) ----
    public Texture terminal;
    public Texture player;
    public Texture hunter;
    public Texture osint;
    public Texture phoneWifi;
    public Texture camera;
    public Texture mapPin;
    public Texture secCamera;
    public Texture lightBulb;
    public Texture ceilingLight;
    public Texture barrierLg;
    public Texture barrierSm;

    private boolean loaded = false;

    // =========================================================================
    // LOAD / DISPOSE
    // =========================================================================

    /**
     * Load all sprite textures from disk.
     * Call once inside Scene.onLoad().
     */
    public void load() {
        terminal     = loadSafe("terminal.png");
        player       = loadSafe("player.png");
        hunter       = loadSafe("hunter.png");
        osint        = loadSafe("osint.png");
        phoneWifi    = loadSafe("phone_wifi.png");
        camera       = loadSafe("camera.png");
        mapPin       = loadSafe("map_pin.png");
        secCamera    = loadSafe("sec_camera.png");
        lightBulb    = loadSafe("lightbulb.png");
        ceilingLight = loadSafe("ceiling_light.png");
        barrierLg    = loadSafe("barrier_large.png");
        barrierSm    = loadSafe("barrier_small.png");
        loaded = true;
    }

    @Override
    public void dispose() {
        disposeTexture(terminal);
        disposeTexture(player);
        disposeTexture(hunter);
        disposeTexture(osint);
        disposeTexture(phoneWifi);
        disposeTexture(camera);
        disposeTexture(mapPin);
        disposeTexture(secCamera);
        disposeTexture(lightBulb);
        disposeTexture(ceilingLight);
        disposeTexture(barrierLg);
        disposeTexture(barrierSm);
        loaded = false;
    }

    public boolean isLoaded() { return loaded; }

    // =========================================================================
    // DRAW HELPERS
    // =========================================================================

    /**
     * Draw a texture centred at world position (cx, cy) with the given size.
     * Alpha controls transparency (1f = fully opaque).
     */
    public void drawCentered(SpriteBatch batch, Texture tex,
                             float cx, float cy, float size, float alpha) {
        if (tex == null) return;
        batch.setColor(1f, 1f, 1f, alpha);
        float half = size * 0.5f;
        batch.draw(tex, cx - half, cy - half, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Draw a texture centred at (cx, cy), rotated by angleDeg, with given size.
     */
    public void drawCenteredRotated(SpriteBatch batch, Texture tex,
                                    float cx, float cy, float size,
                                    float angleDeg, float alpha) {
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

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Texture loadSafe(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return t;
            }
        } catch (Exception e) {
            Gdx.app.log("CyberSprites", "Could not load: " + path + "  -  " + e.getMessage());
        }
        return null;
    }

    private void disposeTexture(Texture t) {
        if (t != null) try { t.dispose(); } catch (Exception ignored) {}
    }
}
