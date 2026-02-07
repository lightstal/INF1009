package io.github.INF1009_P10_Team7.engine.managers;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;
import java.util.HashMap;
import java.util.Map;

public class MovementManager {
    private final Map<Entity, MovementBehaviour> entityBehaviors = new HashMap<>();

    public void addEntity(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);
    }

    public void updateAll(float deltaTime) {
        for (Map.Entry<Entity, MovementBehaviour> entry : entityBehaviors.entrySet()) {
            if (entry.getKey().isActive()) {
                entry.getValue().move(entry.getKey(), deltaTime);
            }
        }
    }
}