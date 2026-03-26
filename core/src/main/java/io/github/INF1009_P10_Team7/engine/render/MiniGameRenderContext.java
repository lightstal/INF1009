package io.github.INF1009_P10_Team7.engine.render;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.render.gdx.GdxShapeDrawAdapter;
import io.github.INF1009_P10_Team7.engine.render.gdx.GdxTextDrawAdapter;

/**
 * Engine-owned render context passed to mini-games.
 *
 * <p>This is a transitional abstraction that keeps LibGDX render objects out
 * of feature contracts while refactoring toward full engine render interfaces.</p>
 */
public class MiniGameRenderContext {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final IShapeDraw shapeDraw;
    private final ITextDraw textDraw;
    private final ITextDraw titleTextDraw;
    private final ITextDraw smallTextDraw;
    private final ITextDraw monoTextDraw;

    /**
     * Convenience constructor: uses the same font for all text roles.
     */
    public MiniGameRenderContext(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch, BitmapFont font) {
        this(shapeRenderer, spriteBatch, font, font, font, font);
    }

    /**
     * Full constructor: allows distinct fonts for different roles without
     * exposing LibGDX types to mini-game implementations (they use {@link ITextDraw} getters).
     */
    public MiniGameRenderContext(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch,
                                 BitmapFont bodyFont, BitmapFont titleFont,
                                 BitmapFont smallFont, BitmapFont monoFont) {
        this.shapeRenderer = shapeRenderer;
        this.spriteBatch = spriteBatch;
        this.shapeDraw = new GdxShapeDrawAdapter(shapeRenderer);
        this.textDraw = new GdxTextDrawAdapter(spriteBatch, bodyFont);
        this.titleTextDraw = new GdxTextDrawAdapter(spriteBatch, titleFont);
        this.smallTextDraw = new GdxTextDrawAdapter(spriteBatch, smallFont);
        this.monoTextDraw = new GdxTextDrawAdapter(spriteBatch, monoFont);
    }

    public ShapeRenderer shapeRenderer() {
        return shapeRenderer;
    }

    public SpriteBatch spriteBatch() {
        return spriteBatch;
    }

    public IShapeDraw shape() {
        return shapeDraw;
    }

    public ITextDraw text() {
        return textDraw;
    }

    public ITextDraw titleText() {
        return titleTextDraw;
    }

    public ITextDraw smallText() {
        return smallTextDraw;
    }

    public ITextDraw monoText() {
        return monoTextDraw;
    }
}
