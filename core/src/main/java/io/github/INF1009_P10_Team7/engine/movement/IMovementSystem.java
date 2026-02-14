package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * Interface for movement management.
 * Decouples the engine from the concrete MovementManager.
 */
public interface IMovementSystem {

    void addEntity(Entity entity, MovementBehaviour behavior);

    void removeEntity(Entity entity);

    void updateAll(float deltaTime);

    void clear();

    int getEntityCount();

    boolean hasEntity(Entity entity);

    MovementBehaviour getBehavior(Entity entity);

    void setBehavior(Entity entity, MovementBehaviour behavior);
}
