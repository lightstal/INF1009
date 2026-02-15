package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * <p>
 * MovementBehaviour is an interface for all movement types.
 * Any movement class must implement this method.
 * It defines how an entity should move every frame.
 * </p>
 */
public interface MovementBehaviour {

    /**
     * <p>
     * Move the given entity.
     * This method is called every frame with deltaTime.
     * </p>
     *
     * @param entity the entity to move
     * @param deltaTime time passed since last frame
     */
    void move(Entity entity, float deltaTime);
}
