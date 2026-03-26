package io.github.INF1009_P10_Team7.cyber.components.drone;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;

/**
 * Entity component that attaches a DroneAI instance to an engine entity.
 * Keeps the entity transform synced to the drone position for render/query use.
 */
public class DroneComponent implements IComponent {

    private final DroneAI drone;
    private Entity owner;

    public DroneComponent(DroneAI drone) {
        this.drone = drone;
    }

    public DroneAI getDrone() {
        return drone;
    }

    @Override
    public void onAdded(Entity owner) {
        this.owner = owner;
        syncTransform();
    }

    @Override
    public void onRemoved(Entity owner) {
        if (this.owner == owner) this.owner = null;
    }

    @Override
    public void update(float deltaTime) {
        syncTransform();
    }

    private void syncTransform() {
        if (owner == null || drone == null) return;
        TransformComponent tc = owner.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.getPosition().set(drone.getPosition().x, drone.getPosition().y);
        }
    }
}
