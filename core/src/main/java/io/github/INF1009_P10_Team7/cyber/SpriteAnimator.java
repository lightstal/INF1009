package io.github.INF1009_P10_Team7.cyber;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * SpriteAnimator  -  animates a grid-based sprite sheet.
 *
 * niceguy.png layout  (576 x 256, 9 cols x 4 rows, each frame 64x64):
 *   Row 0   -  walk UP    (frames 0-8)
 *   Row 1   -  walk LEFT  (frames 0-8)
 *   Row 2   -  walk DOWN  (frames 0-8)
 *   Row 3   -  walk RIGHT (frames 0-8)
 *
 * Usage:
 *   // In your scene / onLoad():
 *   animator = new SpriteAnimator("niceguy.png", 9, 4, 64, 64, 0.1f);
 *
 *   // In your render / update loop:
 *   animator.update(delta, vx, vy);  // pass current velocity
 *   animator.drawCentered(batch, px, py, displaySize);
 *
 *   // In dispose():
 *   animator.dispose();
 */
public class SpriteAnimator {

    /** Facing directions used for animation row selection and setDirection(). */
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    /** The full sprite sheet texture loaded from disk. */
    private final Texture           sheet;
    /** Sliced frames indexed as [row][col], where row = direction. */
    private final TextureRegion[][] frames;

    private final int   cols;
    private final int   rows;
    private final float frameDuration;  // seconds per frame

    // Row indices per direction — set via constructor (OCP: no code change needed for new sheets)
    private final int rowUp, rowDown, rowLeft, rowRight;

    /** Accumulated time used to index into the walk-cycle frames. */
    private float     stateTime  = 0f;
    /** Current sprite sheet row (maps to the facing direction). */
    private int       currentRow;
    /** Whether the entity is currently moving (determines animation vs idle frame). */
    private boolean   moving     = false;

    // -------------------------------------------------------------------------

    /**
     * Convenience constructor using the default niceguy.png row layout:
     *   UP=0, LEFT=1, DOWN=2, RIGHT=3
     *
     * @param assetPath     path inside assets folder, e.g. "niceguy.png"
     * @param cols          number of columns in the sprite sheet
     * @param rows          number of rows  in the sprite sheet
     * @param frameW        width  of each frame in pixels
     * @param frameH        height of each frame in pixels
     * @param frameDuration seconds each frame is shown (0.1f = 10 fps)
     */
    public SpriteAnimator(String assetPath,
                          int cols, int rows,
                          int frameW, int frameH,
                          float frameDuration) {
        this(assetPath, cols, rows, frameW, frameH, frameDuration, 0, 2, 1, 3);
    }

    /**
     * Full constructor — use this when your sprite sheet has a different row order.
     * Satisfies OCP: new sheet layouts need no changes to this class.
     *
     * @param assetPath     path inside assets folder
     * @param cols          number of columns in the sprite sheet
     * @param rows          number of rows  in the sprite sheet
     * @param frameW        width  of each frame in pixels
     * @param frameH        height of each frame in pixels
     * @param frameDuration seconds each frame is shown (0.1f = 10 fps)
     * @param rowUp         sheet row index for the UP walk cycle
     * @param rowDown       sheet row index for the DOWN walk cycle
     * @param rowLeft       sheet row index for the LEFT walk cycle
     * @param rowRight      sheet row index for the RIGHT walk cycle
     */
    public SpriteAnimator(String assetPath,
                          int cols, int rows,
                          int frameW, int frameH,
                          float frameDuration,
                          int rowUp, int rowDown, int rowLeft, int rowRight) {
        this.cols          = cols;
        this.rows          = rows;
        this.frameDuration = frameDuration;
        this.rowUp         = rowUp;
        this.rowDown       = rowDown;
        this.rowLeft       = rowLeft;
        this.rowRight      = rowRight;
        this.currentRow    = rowDown;   // facing down by default

        sheet  = new Texture(com.badlogic.gdx.Gdx.files.internal(assetPath));
        frames = TextureRegion.split(sheet, frameW, frameH);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    /**
     * Call every frame before drawing.
     * Pass the current velocity so the animator picks the right direction row
     * and advances the walk cycle.
     *
     * @param delta seconds since last frame (Gdx.graphics.getDeltaTime())
     * @param vx    horizontal velocity (positive = right, negative = left)
     * @param vy    vertical   velocity (positive = up,    negative = down)
     */
    public void update(float delta, float vx, float vy) {
        moving = (Math.abs(vx) > 0.01f || Math.abs(vy) > 0.01f);

        if (moving) {
            stateTime += delta;

            // Prefer the axis with the larger magnitude for direction
            if (Math.abs(vx) >= Math.abs(vy)) {
                currentRow = (vx > 0) ? rowRight : rowLeft;
            } else {
                currentRow = (vy > 0) ? rowUp : rowDown;
            }
        } else {
            // Standing still  -  show first frame of current direction
            stateTime = 0f;
        }
    }

    // -------------------------------------------------------------------------
    // DRAW
    // -------------------------------------------------------------------------

    /**
     * Draw the current animation frame centred at world position (cx, cy).
     *
     * @param batch       the SpriteBatch (must be begun)
     * @param cx          centre x in world space
     * @param cy          centre y in world space
     * @param displaySize width and height to draw (in world units)
     */
    public void drawCentered(SpriteBatch batch, float cx, float cy, float displaySize) {
        TextureRegion frame = currentFrame();
        float half = displaySize * 0.5f;
        batch.draw(frame, cx - half, cy - half, displaySize, displaySize);
    }

    /**
     * Draw with explicit width/height (if your character is not square on screen).
     */
    public void drawCentered(SpriteBatch batch,
                             float cx, float cy,
                             float displayW, float displayH) {
        TextureRegion frame = currentFrame();
        batch.draw(frame, cx - displayW * 0.5f, cy - displayH * 0.5f, displayW, displayH);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private TextureRegion currentFrame() {
        int frameIndex = 0;
        if (moving) {
            frameIndex = (int)(stateTime / frameDuration) % cols;
        }
        return frames[currentRow][frameIndex];
    }

    /** Force a facing direction (useful for cutscenes / idle). */
    public void setDirection(Direction dir) {
        switch (dir) {
            case UP:    currentRow = rowUp;    break;
            case DOWN:  currentRow = rowDown;  break;
            case LEFT:  currentRow = rowLeft;  break;
            case RIGHT: currentRow = rowRight; break;
        }
    }

    public void dispose() {
        if (sheet != null) sheet.dispose();
    }
}
