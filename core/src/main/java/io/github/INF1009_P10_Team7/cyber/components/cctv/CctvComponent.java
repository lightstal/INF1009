package io.github.INF1009_P10_Team7.cyber.components.cctv;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;

/**
 * CCTV camera metadata component.
 */
public class CctvComponent implements IComponent {
    private final int cameraIndex;
    private final int tileCol;
    private final int tileRow;
    private final float baseAngle;

    public CctvComponent(int cameraIndex, int tileCol, int tileRow, float baseAngle) {
        this.cameraIndex = cameraIndex;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.baseAngle = baseAngle;
    }

    public int getCameraIndex() { return cameraIndex; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public float getBaseAngle() { return baseAngle; }

    @Override public void onAdded(Entity owner) { }
    @Override public void onRemoved(Entity owner) { }
    @Override public void update(float deltaTime) { }
}
