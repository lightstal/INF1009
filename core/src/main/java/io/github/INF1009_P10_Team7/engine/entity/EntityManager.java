package io.github.INF1009_P10_Team7.engine.entity;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.entity.components.MovementComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.SpriteComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.movement.AImovement;
import io.github.INF1009_P10_Team7.engine.movement.FollowMovement;
import io.github.INF1009_P10_Team7.engine.movement.LinearMovement;

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
 * - Create entities from EntityDefinitions (blueprints)
 *
 * Non-responsibilities:
 * - No collision registration
 * - No movement execution
 * - No references to other managers
 */
public class EntityManager implements EntityQuery {

    private final Map<UUID, Entity> entities = new HashMap<>();
    private final List<Entity> pendingAdd = new ArrayList<>();
    private final List<UUID> pendingRemove = new ArrayList<>();

    /** Convenience lookup for scenes/debugging. */
    private final Map<String, GameEntity> namedEntities = new HashMap<>();

    public EntityManager() {}

    public Entity createEntity() {
        Entity entity = new Entity() { };
        addEntity(entity);
        return entity;
    }

    public void addEntity(Entity entity) {
        if (entity == null) return;
        pendingAdd.add(entity);
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        pendingRemove.add(entity.getId());
    }

    /** Update all entities (components) once per frame. */
    public void updateAll(float delta) {
        flushPending();
        for (Entity e : entities.values()) {
            if (!e.isActive()) continue;
            e.update(delta);
        }
        flushPending();
    }

    /** Clears all entities (engine-owned lifecycle). */
    public void clear() {
        entities.clear();
        pendingAdd.clear();
        pendingRemove.clear();
        namedEntities.clear();
    }

    public void dispose() {
        clear();
        Gdx.app.log("EntityManager", "EntityManager disposed");
    }

    @Override
    public Iterable<Entity> getAllEntities() {
        return entities.values();
    }

    public Collection<Entity> getAllEntitiesCollection() {
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

    /**
     * Creates entities from a list of EntityDefinitions.
     * This is the ONLY place where entities are instantiated.
     *
     * @param definitions list of entity blueprints
     * @return map name -> created entity (useful for follow behaviour)
     */
    public Map<String, GameEntity> createEntitiesFromDefinitions(List<EntityDefinition> definitions) {

        Map<String, GameEntity> createdEntities = new HashMap<>();

        // First pass: create entities that do not require target references
        for (EntityDefinition def : definitions) {
            if (def.type == EntityDefinition.EntityType.PLAYER ||
                def.type == EntityDefinition.EntityType.STATIC_OBJECT ||
                def.type == EntityDefinition.EntityType.LINEAR_ENTITY ||
                def.type == EntityDefinition.EntityType.AI_WANDERER ||
                def.type == EntityDefinition.EntityType.INACTIVE_ENTITY ||
                def.type == EntityDefinition.EntityType.BOUNCING_CIRCLE) {

                GameEntity entity = createEntityFromDefinition(def, null);
                createdEntities.put(def.name, entity);
            }
        }

        // Second pass: create follow-type entities after targets exist
        for (EntityDefinition def : definitions) {
            if (def.type == EntityDefinition.EntityType.ENEMY) {
                GameEntity entity = createEntityFromDefinition(def, createdEntities);
                createdEntities.put(def.name, entity);
            }
        }

        namedEntities.clear();
        namedEntities.putAll(createdEntities);

        Gdx.app.log("EntityManager", "Created entities from definitions: " + createdEntities.size());

        return createdEntities;
    }

    private GameEntity createEntityFromDefinition(EntityDefinition def, Map<String, GameEntity> existingEntities) {
        GameEntity entity = new GameEntity(def.name);

        entity.addComponent(new TransformComponent(def.position, def.rotation));

        switch (def.type) {
            case PLAYER:
                if (def.initialVelocity != null) {
                    entity.addComponent(new PhysicComponent(def.initialVelocity, def.mass));
                }
                entity.addComponent(new SpriteComponent("player_sprite"));
                break;

            case ENEMY:
                if (def.targetEntity != null) {
                    entity.addComponent(new MovementComponent(new FollowMovement(def.targetEntity, def.movementSpeed)));
                } else if (existingEntities != null && existingEntities.containsKey("Player")) {
                    entity.addComponent(new MovementComponent(new FollowMovement(existingEntities.get("Player"), def.movementSpeed)));
                }
                break;

            case LINEAR_ENTITY:
                if (def.linearDirection != null) {
                    entity.addComponent(new MovementComponent(new LinearMovement(def.linearDirection, def.movementSpeed)));
                }
                break;

            case AI_WANDERER:
                entity.addComponent(new MovementComponent(new AImovement(def.movementSpeed)));
                break;

            case INACTIVE_ENTITY:
                if (def.initialVelocity != null) {
                    entity.addComponent(new PhysicComponent(def.initialVelocity, def.mass));
                }
                entity.setActive(def.isActive);
                break;

            case BOUNCING_CIRCLE:
                // Bouncing circle uses PhysicComponent for velocity-based movement
                // and collision bouncing (CollisionResolution treats entities with
                // PhysicComponent as movable and reflects their velocity on collision)
                if (def.initialVelocity != null) {
                    entity.addComponent(new PhysicComponent(def.initialVelocity, def.mass));
                } else {
                    // Default velocity so the circle always moves
                    entity.addComponent(new PhysicComponent(
                        new io.github.INF1009_P10_Team7.engine.utils.Vector2(120f, 80f), def.mass));
                }
                break;

            case STATIC_OBJECT:
                break;
        }

        entity.setCollisionRadius(def.collisionRadius);
        addEntity(entity);
        return entity;
    }
}
