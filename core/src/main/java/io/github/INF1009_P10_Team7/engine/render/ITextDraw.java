package io.github.INF1009_P10_Team7.engine.render;

/**
 * Engine-level text drawing and measuring contract.
 */
public interface ITextDraw {
    void begin();
    void end();
    void setColor(float r, float g, float b, float a);
    void draw(String text, float x, float y);
    float measureWidth(String text);
    float measureHeight(String text);
}
