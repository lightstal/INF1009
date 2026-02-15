package io.github.INF1009_P10_Team7.engine.entity;

/**
 * Interface for entity lifecycle management (SRP: manages entity lifecycle only).
 * Decouples scenes and engine from the concrete EntityManager (DIP).
 */
public interface IEntitySystem {

    void addEntity(Entity entity);

    void removeEntity(Entity entity);

    void updateAll(float delta);

    void clear();

    void dispose();

    Iterable<Entity> getAllEntities();
}
