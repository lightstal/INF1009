package io.github.INF1009_P10_Team7.Collision;

import java.util.Iterator;
import java.util.Map;

import io.github.INF1009_P10_Team7.Entity.Entity;

public class CollisionManager implements iCollisionSystem {
	
	@Override
    public void process(Map<Entity, CollisionType> registry, float worldWidth, float worldHeight) {
        // 1. Garbage collect dead entities from the Scene's map
        Iterator<Map.Entry<Entity, CollisionType>> iter = registry.entrySet().iterator();
        while (iter.hasNext()) {
            if (iter.next().getKey().isExpired()) {
                iter.remove();
            }
        }
        
        // 2. Resolve Collisions
        Object[] keys = registry.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            Entity a = (Entity) keys[i];
            if (a.isExpired()) continue;

            CollisionResolution.resolveBoundary(a, registry.get(a), worldWidth, worldHeight);

            for (int j = i + 1; j < keys.length; j++) {
                Entity b = (Entity) keys[j];
                if (b.isExpired()) continue;

                if (CollisionDetection.isOverlapping(a, b)) {
                    CollisionResolution.resolve(a, b, registry.get(a), registry.get(b));
                }
            }
        }
    }
}