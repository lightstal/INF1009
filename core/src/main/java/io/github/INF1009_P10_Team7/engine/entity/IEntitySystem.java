package io.github.INF1009_P10_Team7.engine.entity;

import java.util.Collection;

/**
 * Interface for entity lifecycle management.
 * Decouples scenes and engine from the concrete EntityManager.
 */
public interface IEntitySystem {

    void addEntity(Entity entity);

    void removeEntity(Entity entity);

    void updateAll(float delta);

    void clear();

    void dispose();

    Iterable<Entity> getAllEntities();

    Collection<Entity> getAllEntitiesCollection();
}
