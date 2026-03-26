package io.github.INF1009_P10_Team7.cyber.components;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;

public class ClueComponent implements IComponent {
    private Entity owner;
    private String clueId;
    private String title;
    private String description;
    private String objectName;
    private boolean collected = false;
    private int tileCol;
    private int tileRow;

    public ClueComponent(String clueId, String title, String description, String objectName, int tileCol, int tileRow) {
        this.clueId = clueId;
        this.title = title;
        this.description = description;
        this.objectName = objectName;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
    }

    @Override public void onAdded(Entity entity) { this.owner = entity; }
    @Override public void onRemoved(Entity entity) { this.owner = null; }
    @Override public void update(float deltaTime) {}

    public String getClueId() { return clueId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getObjectName() { return objectName; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
}