package io.github.INF1009_P10_Team7.simulation.cyber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Central font factory — generates crisp FreeType fonts from bundled TTF files.
 *
 * <p>Drop-in replacement for the old {@code new BitmapFont()} pattern.
 * Fonts are rendered at native resolution (density-aware) so they look
 * sharp on any display, including high-DPI/4K screens.</p>
 */
public final class FontManager {

    private FontManager() {}

    /**
     * Base pixel size at scale 1.0 in virtual-resolution units.
     * The old default BitmapFont was ~15 px; we target ~15 px at scale 1.0
     * then multiply by screen density so the texture is rendered at the
     * physical pixel count needed for crispness.
     */
    private static final int BASE_PX = 15;

    /** Cached screen-density multiplier (set on first use). */
    private static float density = -1f;

    private static FreeTypeFontGenerator genRegular;
    private static FreeTypeFontGenerator genBold;

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Creates a regular-weight font at {@code scale × BASE_PX × density}.
     * The returned font's internal scale is adjusted so it occupies the
     * same virtual-space size as the old {@code BitmapFont + setScale(scale)}.
     */
    public static BitmapFont create(float scale) {
        return build(getRegular(), scale);
    }

    /** Bold-weight variant. */
    public static BitmapFont createBold(float scale) {
        return build(getBold(), scale);
    }

    /**
     * Creates a font at an exact pixel size suitable for replacing the
     * skin's built-in bitmap font.  The skin's TextButtonStyle expects
     * a 72 px base (PressStart2P), so callers pass {@code 72}.
     * The font is generated at {@code basePx × density} for crispness.
     */
    public static BitmapFont createForSkin(int basePx) {
        float d = getDensity();
        int px = Math.max(12, Math.round(basePx * d));

        FreeTypeFontParameter p = makeParams(px);
        BitmapFont f = getBold().generateFont(p);

        // Scale the font back so it occupies the same virtual space as
        // a bitmap font with the given basePx.  The generated texture is
        // basePx*density pixels, but the viewport expects basePx virtual units.
        if (d > 1.01f) {
            f.getData().setScale(1f / d);
        }
        f.setUseIntegerPositions(false);
        return f;
    }

    /** Dispose shared generators (call at app exit). */
    public static void dispose() {
        if (genRegular != null) { genRegular.dispose(); genRegular = null; }
        if (genBold    != null) { genBold.dispose();    genBold    = null; }
        density = -1f;
    }

    // ── Internal ─────────────────────────────────────────────────────────

    /**
     * Computes (and caches) the display density multiplier.
     *
     * <p>Density = physical back-buffer width ÷ virtual world width (1280 px).
     * On a 1920-wide window the density is 1.5, so fonts generated at
     * {@code 15 px × 1.5 = 22.5 px} are then scaled back to virtual-space
     * size ({@code scale = 1/density}) so layout code remains correct.</p>
     *
     * @return density ratio (≥ 1.0)
     */
    private static float getDensity() {
        if (density < 0f) {
            // Ratio of physical pixels to virtual world pixels.
            // Virtual world is typically 1280 wide; actual window may be wider.
            float physW = Gdx.graphics.getBackBufferWidth();
            float virtW = 1280f; // matches TileMap.WORLD_W / SettingsScene VW
            density = Math.max(1f, physW / virtW);
        }
        return density;
    }

    private static FreeTypeFontGenerator getRegular() {
        if (genRegular == null)
            genRegular = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/DejaVuSansMono.ttf"));
        return genRegular;
    }

    private static FreeTypeFontGenerator getBold() {
        if (genBold == null)
            genBold = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/DejaVuSansMono-Bold.ttf"));
        return genBold;
    }

    private static BitmapFont build(FreeTypeFontGenerator gen, float scale) {
        float d = getDensity();
        // Generate at physical-pixel size for crispness
        int px = Math.max(8, Math.round(scale * BASE_PX * d));

        FreeTypeFontParameter p = makeParams(px);
        BitmapFont f = gen.generateFont(p);

        // Scale font data back to virtual-space size so existing layout
        // code (which works in 1280×704 virtual coords) stays correct.
        if (d > 1.01f) {
            f.getData().setScale(1f / d);
        }
        f.setUseIntegerPositions(false);
        return f;
    }

    private static FreeTypeFontParameter makeParams(int px) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size       = px;
        p.mono       = true;
        p.hinting    = FreeTypeFontGenerator.Hinting.Full;
        p.minFilter  = Texture.TextureFilter.Linear;
        p.magFilter  = Texture.TextureFilter.Linear;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                     + "↑↓←→·αβγ∅▶▸▪▫█░▒▓│┤┐└┘─┬├┼╔╗╚╝║═╠╣╩╦╬";
        return p;
    }
}
