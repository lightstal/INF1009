package io.github.INF1009_P10_Team7.engine.render.gdx;

import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.INF1009_P10_Team7.engine.render.ISpriteDraw;

/**
 * LibGDX-backed implementation of engine sprite drawing contract.
 */
public class GdxSpriteDrawAdapter implements ISpriteDraw {
    private final SpriteBatch batch;
    private final Function<String, Texture> textureLookup;

    public GdxSpriteDrawAdapter(SpriteBatch batch, Function<String, Texture> textureLookup) {
        this.batch = batch;
        this.textureLookup = textureLookup;
    }

    @Override
    public void begin() {
        batch.begin();
    }

    @Override
    public void end() {
        batch.end();
    }

    @Override
    public void setTint(float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
    }

    @Override
    public void resetTint() {
        batch.setColor(Color.WHITE);
    }

    @Override
    public void draw(String spriteId, float x, float y, float width, float height) {
        Texture texture = textureLookup.apply(spriteId);
        if (texture == null) return;
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public void drawRotated(String spriteId, float x, float y, float width, float height, float degrees) {
        drawRotated(spriteId, x, y, width, height, width / 2f, height / 2f, degrees);
    }

    @Override
    public void drawRotated(String spriteId, float x, float y, float width, float height,
                            float originX, float originY, float degrees) {
        Texture texture = textureLookup.apply(spriteId);
        if (texture == null) return;
        batch.draw(texture, x, y, originX, originY, width, height, 1f, 1f, degrees,
            0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }
}
