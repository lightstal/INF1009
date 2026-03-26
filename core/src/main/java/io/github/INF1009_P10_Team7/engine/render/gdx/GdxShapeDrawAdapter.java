package io.github.INF1009_P10_Team7.engine.render.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;

/**
 * LibGDX-backed implementation of the engine shape drawing contract.
 */
public class GdxShapeDrawAdapter implements IShapeDraw {
    private final ShapeRenderer shapeRenderer;

    public GdxShapeDrawAdapter(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void beginFilled() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void beginLine() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    }

    @Override
    public void end() {
        shapeRenderer.end();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        shapeRenderer.setColor(r, g, b, a);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        shapeRenderer.line(x1, y1, x2, y2);
    }

    @Override
    public void rectLine(float x1, float y1, float x2, float y2, float width) {
        shapeRenderer.rectLine(x1, y1, x2, y2, width);
    }

    @Override
    public void rect(float x, float y, float width, float height) {
        shapeRenderer.rect(x, y, width, height);
    }

    @Override
    public void circle(float x, float y, float radius, int segments) {
        shapeRenderer.circle(x, y, radius, segments);
    }

    @Override
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        shapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
    }
}
