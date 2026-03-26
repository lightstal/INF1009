package io.github.INF1009_P10_Team7.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Engine-level font factory.
 */
public final class FontManager {
    private FontManager() { }

    private static final int BASE_PX = 15;
    private static float density = -1f;
    private static FreeTypeFontGenerator genRegular;
    private static FreeTypeFontGenerator genBold;

    public static BitmapFont create(float scale) {
        return build(getBold(), scale);
    }

    public static BitmapFont createBold(float scale) {
        return build(getBold(), scale);
    }

    public static BitmapFont createForSkin(int basePx) {
        float d = getDensity();
        int px = Math.max(12, Math.round(basePx * d));
        FreeTypeFontParameter p = makeParams(px);
        BitmapFont f = getBold().generateFont(p);
        if (d > 1.01f) {
            f.getData().setScale(1f / d);
        }
        f.setUseIntegerPositions(true);
        return f;
    }

    public static void dispose() {
        if (genRegular != null) { genRegular.dispose(); genRegular = null; }
        if (genBold != null) { genBold.dispose(); genBold = null; }
        density = -1f;
    }

    private static float getDensity() {
        if (density < 0f) {
            float physW = Gdx.graphics.getBackBufferWidth();
            float virtW = 1280f;
            density = Math.max(1f, physW / virtW);
        }
        return density;
    }

    private static FreeTypeFontGenerator getRegular() {
        if (genRegular == null) {
            genRegular = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/DejaVuSansMono.ttf"));
        }
        return genRegular;
    }

    private static FreeTypeFontGenerator getBold() {
        if (genBold == null) {
            genBold = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/DejaVuSansMono-Bold.ttf"));
        }
        return genBold;
    }

    private static BitmapFont build(FreeTypeFontGenerator gen, float scale) {
        float d = getDensity();
        int px = Math.max(8, Math.round(scale * BASE_PX * d));
        FreeTypeFontParameter p = makeParams(px);
        BitmapFont f = gen.generateFont(p);
        if (d > 1.01f) {
            f.getData().setScale(1f / d);
        }
        f.setUseIntegerPositions(true);
        return f;
    }

    private static FreeTypeFontParameter makeParams(int px) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = px;
        p.mono = true;
        p.hinting = FreeTypeFontGenerator.Hinting.Full;
        p.minFilter = Texture.TextureFilter.Nearest;
        p.magFilter = Texture.TextureFilter.Nearest;
        p.renderCount = 2;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "↑↓←→·αβγ∅▶▸▪▫█░▒▓│┤┐└┘─┬├┼╔╗╚╝║═╠╣╩╦╬";
        return p;
    }
}
