package io.github.INF1009_P10_Team7.engine.render;

/**
 * Engine-level sprite drawing contract.
 */
public interface ISpriteDraw {
    void begin();
    void end();
    void setTint(float r, float g, float b, float a);
    void resetTint();
    void draw(String spriteId, float x, float y, float width, float height);
    void drawRotated(String spriteId, float x, float y, float width, float height, float degrees);
    void drawRotated(String spriteId, float x, float y, float width, float height,
                     float originX, float originY, float degrees);
}
