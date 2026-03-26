package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class CyberSprites implements Disposable {
    private final Map<String, Texture> textureRegistry = new HashMap<>();
    private boolean loaded = false;

    public void load() {
        if (loaded) return;
        try {
            JsonValue spriteData = new JsonReader().parse(Gdx.files.internal("sprites.json"));
            for (JsonValue entry : spriteData) registerTexture(entry.name, entry.asString());

            // The TMX door layer uses a two-tile sheet (closed/open) but sprites.json
            // may not define per-state keys. If missing, generate them by slicing
            // `Textures/doors.png` so `CyberWorldRenderer` can draw the correct state.
            ensureExitDoorSprites();

            loaded = true;
            Gdx.app.log("CyberSprites", "Successfully loaded " + textureRegistry.size() + " sprites from JSON.");
        } catch (Exception e) {
            Gdx.app.error("CyberSprites", "Failed to parse sprites.json: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        for (Texture tex : textureRegistry.values()) disposeTexture(tex);
        textureRegistry.clear();
        loaded = false;
        Gdx.app.log("CyberSprites", "All sprite textures disposed.");
    }

    public boolean isLoaded() { return loaded; }

    public Texture get(String key) {
        Texture tex = textureRegistry.get(key);
        if (tex == null) Gdx.app.error("CyberSprites", "WARNING: Texture not found in registry: " + key);
        return tex;
    }

    public boolean has(String key) {
        return textureRegistry.containsKey(key);
    }

    public float getAspectRatio(String key) {
        Texture tex = get(key);
        if (tex == null || tex.getHeight() == 0) return 1f;
        return tex.getWidth() / (float) tex.getHeight();
    }

    private void ensureExitDoorSprites() {
        boolean hasClosed = textureRegistry.containsKey("exitDoorClosed");
        boolean hasOpen = textureRegistry.containsKey("exitDoorOpen");
        if (hasClosed && hasOpen) return;

        try {
            // doors.png is expected to be a horizontal 2-tile sheet (closed | open).
            Pixmap sheet = new Pixmap(Gdx.files.internal("Textures/doors.png"));
            int sheetW = sheet.getWidth();
            int sheetH = sheet.getHeight();
            int tileW = Math.max(1, sheetW / 2);
            int tileH = Math.max(1, sheetH);

            // TMX door layer uses two tiles ("closed door" vs "open door").
            // Slice order in `Textures/doors.png` may not match our initial
            // assumption, so we slice as:
            //   - right half -> closed
            //   - left half  -> open
            if (!hasClosed) {
                Pixmap closedPm = new Pixmap(tileW, tileH, sheet.getFormat());
                closedPm.drawPixmap(sheet, 0, 0, tileW, tileH, tileW, 0);
                Texture closedTex = new Texture(closedPm);
                closedTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textureRegistry.put("exitDoorClosed", closedTex);
                closedPm.dispose();
            }

            if (!hasOpen) {
                Pixmap openPm = new Pixmap(tileW, tileH, sheet.getFormat());
                openPm.drawPixmap(sheet, 0, 0, tileW, tileH, 0, 0);
                Texture openTex = new Texture(openPm);
                openTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textureRegistry.put("exitDoorOpen", openTex);
                openPm.dispose();
            }

            sheet.dispose();
            Gdx.app.log("CyberSprites", "Generated exit door sprites: closed/open");
        } catch (Exception e) {
            // If slicing fails, at least don’t crash the game.
            Gdx.app.error("CyberSprites", "Failed to generate exit door sprites: " + e.getMessage());
        }
    }

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

    public void drawCentered(SpriteBatch batch, String textureKey, float cx, float cy, float size, float alpha) {
        Texture tex = get(textureKey);
        if (tex == null) return;
        batch.setColor(1f, 1f, 1f, alpha);
        float half = size * 0.5f;
        batch.draw(tex, cx - half, cy - half, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawCenteredRotated(SpriteBatch batch, String textureKey, float cx, float cy, float size, float angleDeg, float alpha) {
        Texture tex = get(textureKey);
        if (tex == null) return;
        batch.setColor(1f, 1f, 1f, alpha);
        float half = size * 0.5f;
        batch.draw(tex, cx - half, cy - half, half, half, size, size, 1f, 1f, angleDeg,
            0, 0, tex.getWidth(), tex.getHeight(), false, false);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void disposeTexture(Texture t) { if (t != null) try { t.dispose(); } catch (Exception ignored) {} }
}
