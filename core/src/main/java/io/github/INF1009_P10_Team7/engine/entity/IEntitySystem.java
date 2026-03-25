package io.github.INF1009_P10_Team7.engine.entity;

/**
 * IEntitySystem — write interface for managing the entity lifecycle.
 *
 * <p>Scenes that create or destroy entities should depend on this interface
 * rather than the full {@link EntityManager} (ISP / DIP). Separating read
 * access ({@link IEntityQuery}) from write access keeps scene code from
 * unintentionally querying entities while the collection is being mutated.</p>
 */
public interface IEntitySystem {

    /**
     * Schedules an entity to be added to the manager on the next frame
     * boundary (deferred add prevents ConcurrentModificationException).
     *
     * @param entity the entity to register; ignored if {@code null}
     */
    void addEntity(Entity entity);

    /**
     * Schedules an entity to be removed from the manager on the next frame
     * boundary.
     *
     * @param entity the entity to unregister; ignored if {@code null}
     */
    void removeEntity(Entity entity);

    /**
     * Flushes pending adds/removes then calls {@link Entity#update} on every
     * active entity. Called by {@link io.github.INF1009_P10_Team7.engine.core.GameEngine}
     * once per frame.
     *
     * @param delta seconds since the last frame
     */
    void updateAll(float delta);

    /** Removes all entities immediately and clears the pending queues. */
    void clear();

    /**
     * Disposes all entities and releases manager resources.
     * Called during engine shutdown.
     */
    void dispose();
}
