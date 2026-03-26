package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteAnimator {
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    private final Texture sheet;
    private final TextureRegion[][] frames;
    private final int cols;
    private final float frameDuration;
    private final int rowUp, rowDown, rowLeft, rowRight;
    private float stateTime = 0f;
    private int currentRow;
    private boolean moving = false;

    public SpriteAnimator(String assetPath, int cols, int rows, int frameW, int frameH, float frameDuration) {
        this(assetPath, cols, rows, frameW, frameH, frameDuration, 0, 2, 1, 3);
    }
    public SpriteAnimator(String assetPath, int cols, int rows, int frameW, int frameH, float frameDuration,
                          int rowUp, int rowDown, int rowLeft, int rowRight) {
        this.cols = cols; this.frameDuration = frameDuration;
        this.rowUp = rowUp; this.rowDown = rowDown; this.rowLeft = rowLeft; this.rowRight = rowRight;
        this.currentRow = rowDown;
        sheet = new Texture(com.badlogic.gdx.Gdx.files.internal(assetPath));
        frames = TextureRegion.split(sheet, frameW, frameH);
    }
    public void update(float delta, float vx, float vy) {
        moving = Math.abs(vx) > 0.01f || Math.abs(vy) > 0.01f;
        if (!moving) { stateTime = 0f; return; }
        stateTime += delta;
        if (Math.abs(vx) >= Math.abs(vy)) currentRow = (vx > 0) ? rowRight : rowLeft;
        else currentRow = (vy > 0) ? rowUp : rowDown;
    }
    public void drawCentered(SpriteBatch batch, float cx, float cy, float displaySize) {
        TextureRegion frame = currentFrame(); float half = displaySize * 0.5f;
        batch.draw(frame, cx - half, cy - half, displaySize, displaySize);
    }
    public void drawCentered(SpriteBatch batch, float cx, float cy, float displayW, float displayH) {
        TextureRegion frame = currentFrame();
        batch.draw(frame, cx - displayW * 0.5f, cy - displayH * 0.5f, displayW, displayH);
    }
    private TextureRegion currentFrame() {
        int frameIndex = moving ? (int) (stateTime / frameDuration) % cols : 0;
        return frames[currentRow][frameIndex];
    }
    public void setDirection(Direction dir) {
        switch (dir) {
            case UP: currentRow = rowUp; break;
            case DOWN: currentRow = rowDown; break;
            case LEFT: currentRow = rowLeft; break;
            case RIGHT: currentRow = rowRight; break;
        }
    }
    public void dispose() { if (sheet != null) sheet.dispose(); }
}
