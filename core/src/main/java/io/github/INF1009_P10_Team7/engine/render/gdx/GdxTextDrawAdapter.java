package io.github.INF1009_P10_Team7.engine.render.gdx;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.INF1009_P10_Team7.engine.render.ITextDraw;

/**
 * LibGDX-backed text renderer implementation.
 */
public class GdxTextDrawAdapter implements ITextDraw {
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public GdxTextDrawAdapter(SpriteBatch spriteBatch, BitmapFont font) {
        this.spriteBatch = spriteBatch;
        this.font = font;
    }

    @Override
    public void begin() {
        spriteBatch.begin();
    }

    @Override
    public void end() {
        spriteBatch.end();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
    }

    @Override
    public void draw(String text, float x, float y) {
        font.draw(spriteBatch, text, x, y);
    }

    @Override
    public float measureWidth(String text) {
        layout.setText(font, text);
        return layout.width;
    }

    @Override
    public float measureHeight(String text) {
        layout.setText(font, text);
        return layout.height;
    }
}
