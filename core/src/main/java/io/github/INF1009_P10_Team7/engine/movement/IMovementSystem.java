package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * <p>
 * Interface for movement system.
 * It manages entities and their movement behaviours.
 * This helps separate the engine logic from the actual movement implementation.
 * </p>
 */
public interface IMovementSystem {

    /**
     * <p>
     * Add an entity with its movement behaviour into the system.
     * </p>
     *
     * @param entity the entity to be added
     * @param behavior the movement behaviour for the entity
     */
    void addEntity(Entity entity, MovementBehaviour behavior);

    /**
     * <p>
     * Remove an entity from the movement system.
     * </p>
     *
     * @param entity the entity to remove
     */
    void removeEntity(Entity entity);

    /**
     * <p>
     * Update movement for all entities.
     * Called every frame using deltaTime.
     * </p>
     *
     * @param deltaTime time passed since last frame
     */
    void updateAll(float deltaTime);

    /**
     * <p>
     * Clear all entities from the movement system.
     * </p>
     */
    void clear();

    /**
     * <p>
     * Get the number of entities currently managed.
     * </p>
     *
     * @return total entity count
     */
    int getEntityCount();

    /**
     * <p>
     * Check if the system already contains the entity.
     * </p>
     *
     * @param entity the entity to check
     * @return true if entity exists, false otherwise
     */
    boolean hasEntity(Entity entity);

    /**
     * <p>
     * Get the movement behaviour assigned to an entity.
     * </p>
     *
     * @param entity the entity to check
     * @return movement behaviour of the entity
     */
    MovementBehaviour getBehavior(Entity entity);

    /**
     * <p>
     * Set or change the movement behaviour for an entity.
     * </p>
     *
     * @param entity the entity to update
     * @param behavior new movement behaviour
     */
    void setBehavior(Entity entity, MovementBehaviour behavior);
}
