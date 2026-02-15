package io.github.INF1009_P10_Team7.engine.entity;

import java.util.Map;

/** Read-only interface for scenes to query entities without managing them. */
public interface IEntityQuery {
    Iterable<Entity> getAllEntities();
    Map<String, GameEntity> getNamedEntities();
    GameEntity getByName(String name);
}