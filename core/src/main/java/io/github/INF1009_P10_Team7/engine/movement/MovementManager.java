package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import com.badlogic.gdx.Gdx;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * MovementManager - Manages movement behaviors for entities.
 * Implements IMovementSystem to allow dependency inversion.
 */
public class MovementManager implements IMovementSystem {
    private final Map<Entity, MovementBehaviour> entityBehaviors = new HashMap<>();

    public MovementManager() {
        Gdx.app.log("MovementManager", "MovementManager initialized");
    }

    @Override
    public void addEntity(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);

        String name = (behavior == null)
            ? "PhysicsOnly"
            : behavior.getClass().getSimpleName();

        Gdx.app.log("MovementManager", "Added entity with " + name);
    }

    @Override
    public void removeEntity(Entity entity) {
        entityBehaviors.remove(entity);
        Gdx.app.log("MovementManager", "Removed entity from movement management");
    }

    @Override
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

    @Override
    public void clear() {
        entityBehaviors.clear();
        Gdx.app.log("MovementManager", "MovementManager cleared");
    }

    @Override
    public int getEntityCount() {
        return entityBehaviors.size();
    }

    @Override
    public boolean hasEntity(Entity entity) {
        return entityBehaviors.containsKey(entity);
    }

    @Override
    public MovementBehaviour getBehavior(Entity entity) {
        return entityBehaviors.get(entity);
    }

    @Override
    public void setBehavior(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);
        Gdx.app.log("MovementManager", "Updated behavior for entity to " + behavior.getClass().getSimpleName());
    }
}
