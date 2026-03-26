package io.github.INF1009_P10_Team7.engine.render;

/**
 * Engine-owned immutable RGBA color value.
 */
public final class ColorValue {
    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public ColorValue(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
