package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import com.badlogic.gdx.Gdx;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * MovementManager manages all entities and their movement behaviours.
 * It updates physics movement first, then applies custom movement behaviour.
 * </p>
 */
public class MovementManager implements IMovementSystem {
    /** Stores each entity with its movement behaviour */
    private final Map<Entity, MovementBehaviour> entityBehaviors = new HashMap<>();

    /**
     * <p>
     * Constructor to initialize the movement manager.
     * </p>
     */
    public MovementManager() {
        Gdx.app.log("MovementManager", "MovementManager initialized");
    }

    /**
     * <p>
     * Add an entity with its movement behaviour.
     * If behaviour is null, only physics movement will be applied.
     * </p>
     *
     * @param entity the entity to add
     * @param behavior the movement behaviour assigned to the entity
     */
    @Override
    public void addEntity(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);

        String name = (behavior == null)
            ? "PhysicsOnly"
            : behavior.getClass().getSimpleName();

        Gdx.app.log("MovementManager", "Added entity with " + name);
    }

    /**
     * <p>
     * Remove an entity from the movement manager.
     * </p>
     *
     * @param entity the entity to remove
     */
    @Override
    public void removeEntity(Entity entity) {
        entityBehaviors.remove(entity);
        Gdx.app.log("MovementManager", "Removed entity from movement management");
    }

    /**
     * <p>
     * Update all entities every frame.
     * First applies physics velocity, then applies movement behaviour.
     * </p>
     *
     * @param deltaTime time passed since last frame
     */
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

    /**
     * <p>
     * Remove all entities and behaviours from the manager.
     * </p>
     */
    @Override
    public void clear() {
        entityBehaviors.clear();
        Gdx.app.log("MovementManager", "MovementManager cleared");
    }

    /**
     * <p>
     * Get the total number of entities managed.
     * </p>
     *
     * @return number of entities
     */
    @Override
    public int getEntityCount() {
        return entityBehaviors.size();
    }

    /**
     * <p>
     * Check if an entity exists in the manager.
     * </p>
     *
     * @param entity the entity to check
     * @return true if entity is managed, false otherwise
     */
    @Override
    public boolean hasEntity(Entity entity) {
        return entityBehaviors.containsKey(entity);
    }

    /**
     * <p>
     * Get the movement behaviour assigned to an entity.
     * </p>
     *
     * @param entity the entity to check
     * @return movement behaviour of the entity
     */
    @Override
    public MovementBehaviour getBehavior(Entity entity) {
        return entityBehaviors.get(entity);
    }

    /**
     * <p>
     * Update or change the movement behaviour of an entity.
     * </p>
     *
     * @param entity the entity to update
     * @param behavior new movement behaviour
     */
    @Override
    public void setBehavior(Entity entity, MovementBehaviour behavior) {
        entityBehaviors.put(entity, behavior);
        Gdx.app.log("MovementManager", "Updated behavior for entity to " + behavior.getClass().getSimpleName());
    }
}
