package io.github.INF1009_P10_Team7.cyber.components;

import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.Entity;

public class ExitDoorComponent implements IComponent {
    private Entity owner;
    private boolean unlocked = false;

    @Override public void onAdded(Entity entity) { this.owner = entity; }
    @Override public void onRemoved(Entity entity) { this.owner = null; }
    @Override public void update(float deltaTime) {}

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}