package io.github.INF1009_P10_Team7.Movement;

import java.util.Iterator;
import java.util.Map;

import io.github.INF1009_P10_Team7.Entity.Entity;

public class MovementManager implements iMovementSystem {
	
    @Override
    public void process(Map<Entity, iMovementBehaviour> registry, float deltaTime) {
        Iterator<Map.Entry<Entity, iMovementBehaviour>> iter = registry.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<Entity, iMovementBehaviour> entry = iter.next();
            Entity entity = entry.getKey();

            // Auto-remove dead entities directly from the Scene's map
            if (entity.isExpired()) {
                iter.remove();
                continue;
            }

            // Execute Movement
            entry.getValue().move(entity, deltaTime);
        }
    }
}