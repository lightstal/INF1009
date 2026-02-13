package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import com.badlogic.gdx.Gdx;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * MovementManager - Manages movement behaviors for entities
 */
public class MovementManager {
    private final Map<Entity, MovementBehaviour> entityBehaviors = new HashMap<>();

    public MovementManager() {
        Gdx.app.log("MovementManager", "MovementManager initialized");
    }

    /**
     * Add an entity with its movement behavior
     */
    public void addEntity(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);

        String name = (behavior == null)
            ? "PhysicsOnly"
            : behavior.getClass().getSimpleName();

        Gdx.app.log("MovementManager", "Added entity with " + name);
    }

    /**
     * Remove an entity from movement management
     */
    public void removeEntity(Entity entity) {
        entityBehaviors.remove(entity);
        Gdx.app.log("MovementManager", "Removed entity from movement management");
    }

    /**
     * Update all entities with their movement behaviors
     */
    public void updateAll(float deltaTime) {
        for (Map.Entry<Entity, MovementBehaviour> entry : entityBehaviors.entrySet()) {
            Entity entity = entry.getKey();
            MovementBehaviour behavior = entry.getValue();

            if (!entity.isActive()) continue;

            // ---- A) Physics Integration ----
            PhysicComponent pc = entity.getComponent(PhysicComponent.class);
            TransformComponent tc = entity.getComponent(TransformComponent.class);

            if (pc != null && tc != null) {
                Vector2 pos = tc.getPosition();
                Vector2 vel = pc.getVelocity();

                pos.x += vel.x * deltaTime;
                pos.y += vel.y * deltaTime;
            }

            // ---- B) Behaviour Movement ----
            if (behavior != null) {
                behavior.move(entity, deltaTime);
            }
        }
    }

    /**
     * Clear all entities from movement management
     */
    public void clear() {
        entityBehaviors.clear();
        Gdx.app.log("MovementManager", "MovementManager cleared");
    }

    /**
     * Get the number of entities being managed
     */
    public int getEntityCount() {
        return entityBehaviors.size();
    }

    /**
     * Check if an entity is registered
     */
    public boolean hasEntity(Entity entity) {
        return entityBehaviors.containsKey(entity);
    }

    /**
     * Get the movement behavior for an entity
     */
    public MovementBehaviour getBehavior(Entity entity) {
        return entityBehaviors.get(entity);
    }

    /**
     * Set or change the movement behavior for an entity
     */
    public void setBehavior(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);
        Gdx.app.log("MovementManager", "Updated behavior for entity to " + behavior.getClass().getSimpleName());
    }
}
