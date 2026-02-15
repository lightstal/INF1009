package io.github.INF1009_P10_Team7.engine.entity;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EntityManager (engine layer)
 *
 * Responsibilities:
 * - Own entity collection + lifecycle (add/remove/update)
 * - Provide query access to entities
 *
 * Non-responsibilities:
 * - No entity creation logic (scenes create entities directly)
 * - No collision registration
 * - No movement execution
 * - No context-specific code (no EntityType enum, no switch statements)
 */
public class EntityManager implements IEntitySystem, EntityQuery {

    private final Map<UUID, Entity> entities = new HashMap<>();
    private final List<Entity> pendingAdd = new ArrayList<>();
    private final List<UUID> pendingRemove = new ArrayList<>();

    /** Convenience lookup for scenes/debugging. */
    private final Map<String, GameEntity> namedEntities = new HashMap<>();

    public EntityManager() {}

    @Override
    public void addEntity(Entity entity) {
        if (entity == null) return;
        pendingAdd.add(entity);

        // Auto-register named entities for query lookup
        if (entity instanceof GameEntity) {
            GameEntity ge = (GameEntity) entity;
            if (ge.getName() != null && !ge.getName().isEmpty()) {
                namedEntities.put(ge.getName(), ge);
            }
        }
    }

    @Override
    public void removeEntity(Entity entity) {
        if (entity == null) return;
        pendingRemove.add(entity.getId());

        if (entity instanceof GameEntity) {
            GameEntity ge = (GameEntity) entity;
            namedEntities.remove(ge.getName());
        }
    }

    @Override
    public void updateAll(float delta) {
        flushPending();
        for (Entity e : entities.values()) {
            if (!e.isActive()) continue;
            e.update(delta);
        }
        flushPending();
    }

    @Override
    public void clear() {
        entities.clear();
        pendingAdd.clear();
        pendingRemove.clear();
        namedEntities.clear();
    }

    @Override
    public void dispose() {
        clear();
        Gdx.app.log("EntityManager", "EntityManager disposed");
    }

    @Override
    public Iterable<Entity> getAllEntities() {
        return entities.values();
    }

    @Override
    public Map<String, GameEntity> getNamedEntities() {
        return namedEntities;
    }

    @Override
    public GameEntity getByName(String name) {
        return namedEntities.get(name);
    }

    private void flushPending() {
        if (!pendingRemove.isEmpty()) {
            for (UUID id : pendingRemove) {
                entities.remove(id);
            }
            pendingRemove.clear();
        }
        if (!pendingAdd.isEmpty()) {
            for (Entity e : pendingAdd) {
                entities.put(e.getId(), e);
            }
            pendingAdd.clear();
        }
    }
}
