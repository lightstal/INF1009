package io.github.INF1009_P10_Team7.engine.entity;

import java.util.Map;

/**
 * IEntityQuery, read-only view of the entity collection.
 *
 * <p>Scenes and systems that only need to look up entities should depend on
 * this narrow interface rather than the full {@link EntityManager} (ISP / DIP).
 * This prevents callers from accidentally adding or removing entities when
 * they only need to read state.</p>
 */
public interface IEntityQuery {

    /**
     * Returns an iterable snapshot of every entity currently registered
     * with the manager. Includes both active and inactive entities.
     *
     * @return all registered entities
     */
    Iterable<Entity> getAllEntities();

    /**
     * Returns the map of named entities for quick name-based lookup.
     * Only {@link GameEntity} instances with a non-null, non-empty name
     * are present in this map.
     *
     * @return map from name to {@link GameEntity}
     */
    Map<String, GameEntity> getNamedEntities();

    /**
     * Finds a named {@link GameEntity} by its registered name.
     *
     * @param name the entity name to look up (case-sensitive)
     * @return the matching {@link GameEntity}, or {@code null} if not found
     */
    GameEntity getByName(String name);
}
