package io.github.INF1009_P10_Team7.engine.render;

/**
 * Engine-level primitive drawing contract.
 *
 * <p>This interface exists so game modules can depend on render abstractions
 * instead of framework-specific shape classes.</p>
 */
public interface IShapeDraw {
    void beginFilled();
    void beginLine();
    void end();
    void setColor(float r, float g, float b, float a);
    void line(float x1, float y1, float x2, float y2);
    void rectLine(float x1, float y1, float x2, float y2, float width);
    void rect(float x, float y, float width, float height);
    void circle(float x, float y, float radius, int segments);
    void triangle(float x1, float y1, float x2, float y2, float x3, float y3);
}
