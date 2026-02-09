package io.github.INF1009_P10_Team7.engine.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;
import io.github.INF1009_P10_Team7.engine.entity.components.MovementComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.SpriteComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.events.EventListener;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;
import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.movement.AImovement;
import io.github.INF1009_P10_Team7.engine.movement.FollowMovement;
import io.github.INF1009_P10_Team7.engine.movement.LinearMovement;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

// Manages a collection of entities, allowing for creation, addition, removal, and updating.
public class EntityManager implements EventListener{
    private final Map<UUID, Entity> entities;
    private final List<Entity> pendingAdd;
    private final List<UUID> pendingRemove;

    private final EventBus eventBus;

    private boolean isPaused = false;

    // Creates a new EntityManager.
    public EntityManager(EventBus eventBus) {
        this.entities = new HashMap<>();
        this.pendingAdd = new ArrayList<>();
        this.pendingRemove = new ArrayList<>();
        this.eventBus = eventBus;

        eventBus.subscribe(EventType.GAME_PAUSED, this);
        eventBus.subscribe(EventType.GAME_RESUMED, this);
        eventBus.subscribe(EventType.GAME_START, this);
    }

    @Override
    public void onNotify(GameEvent event) {
        Gdx.app.log("EntityManager - EventBus", "Event received: " + event.type);
        if (event.type == EventType.GAME_PAUSED) {
            isPaused = true;
        } else if (event.type == EventType.GAME_RESUMED) {
            isPaused = false;
        } else if (event.type == EventType.GAME_START) {
            clear();
        }
    }

    // Creates a new entity and schedules it for addition.
    public Entity createEntity() {
        // Default implementation creates a basic entity
        // Subclasses can override to create specific entity types
        Entity entity = new Entity() {
            // Anonymous concrete implementation
        };
        addEntity(entity);
        return entity;
    }

    // Schedules an entity for addition.
    public void addEntity(Entity entity) {
        pendingAdd.add(entity);
    }

    // Schedules an entity for removal by its ID.
    public void removeEntity(UUID id) {
        pendingRemove.add(id);
    }

    // Gets an entity by its ID.
    public Entity getEntity(UUID id) {
        return entities.get(id);
    }

    // Gets a collection of all active entities.
    public Collection<Entity> getAllEntities() {
        return entities.values();
    }

    // Updates all active entities. Processes pending additions and removals first.
    public void updateAll(float deltaTime) {

        if(isPaused) return;

        // Process pending removals first
        for (UUID id : pendingRemove) {
            entities.remove(id);
        }
        pendingRemove.clear();

        // Process pending additions
        for (Entity entity : pendingAdd) {
            entities.put(entity.getId(), entity);
        }
        pendingAdd.clear();

        // Update all active entities
        for (Entity entity : entities.values()) {
            if (entity.isActive()) {
                entity.update(deltaTime);
            }
        }
    }

    // Clears all entities from the manager.
    public void clear() {
        entities.clear();
        pendingAdd.clear();
        pendingRemove.clear();
        isPaused = false;
    }

    // Clears all entities from the manager.
    public void dispose() {
        clear();
        if (eventBus != null) {
            eventBus.unsubscribe(this);
        }
    }

    /**
     * Creates entities from a list of EntityDefinitions.
     * This is the ONLY place where entities should be instantiated.
     *
     * @param definitions List of entity blueprints
     * @param collisionManager CollisionManager to register collidables with
     * @return Map of entity names to created entities (for reference purposes like follow behavior)
     */
    public Map<String, GameEntity> createEntitiesFromDefinitions(
        List<EntityDefinition> definitions,
        CollisionManager collisionManager) {

        Map<String, GameEntity> createdEntities = new HashMap<>();

        // First pass: Create all entities without follow movement
        for (EntityDefinition def : definitions) {
            if (def.type == EntityDefinition.EntityType.PLAYER ||
                def.type == EntityDefinition.EntityType.STATIC_OBJECT ||
                def.type == EntityDefinition.EntityType.LINEAR_ENTITY ||
                def.type == EntityDefinition.EntityType.AI_WANDERER ||
                def.type == EntityDefinition.EntityType.INACTIVE_ENTITY) {

                GameEntity entity = createEntityFromDefinition(def, null);
                createdEntities.put(def.name, entity);

                if (collisionManager != null) {
                    collisionManager.registerCollidable(entity, def.resolutionType);
                }

                Gdx.app.log("EntityManager", "Created " + def.name + " at " + def.position);
            }
        }

        // Second pass: Create entities with follow movement (need reference to target)
        for (EntityDefinition def : definitions) {
            if (def.type == EntityDefinition.EntityType.ENEMY) {
                GameEntity entity = createEntityFromDefinition(def, createdEntities);
                createdEntities.put(def.name, entity);

                if (collisionManager != null) {
                    collisionManager.registerCollidable(entity, def.resolutionType);
                }

                Gdx.app.log("EntityManager", "Created " + def.name + " at " + def.position);
            }
        }

        return createdEntities;
    }

    /**
     * Internal method to create a single entity from its definition.
     */
    private GameEntity createEntityFromDefinition(
        EntityDefinition def,
        Map<String, GameEntity> existingEntities) {

        GameEntity entity = new GameEntity(def.name);

        // Add Transform component
        entity.addComponent(new TransformComponent(def.position, def.rotation));

        // Add type-specific components based on EntityType
        switch (def.type) {
            case PLAYER:
                // Player has physics and sprite
                if (def.initialVelocity != null) {
                    entity.addComponent(new PhysicComponent(def.initialVelocity, def.mass));
                }
                entity.addComponent(new SpriteComponent("player_sprite"));
                break;

            case ENEMY:
                // Enemy has follow movement
                if (def.targetEntity != null) {
                    entity.addComponent(new MovementComponent(
                        new FollowMovement(def.targetEntity, def.movementSpeed)));
                } else if (existingEntities != null && existingEntities.containsKey("Player")) {
                    // Fallback: try to find Player from existing entities
                    entity.addComponent(new MovementComponent(
                        new FollowMovement(existingEntities.get("Player"), def.movementSpeed)));
                }
                break;

            case LINEAR_ENTITY:
                // Linear movement entity
                if (def.linearDirection != null) {
                    entity.addComponent(new MovementComponent(
                        new LinearMovement(def.linearDirection, def.movementSpeed)));
                }
                break;

            case AI_WANDERER:
                // AI random movement
                entity.addComponent(new MovementComponent(
                    new AImovement(def.movementSpeed)));
                break;

            case INACTIVE_ENTITY:
                // Inactive entity with physics
                if (def.initialVelocity != null) {
                    entity.addComponent(new PhysicComponent(def.initialVelocity, def.mass));
                }
                entity.setActive(def.isActive);
                break;

            case STATIC_OBJECT:
                // Static object - no additional components needed
                break;
        }

        // Set collision radius
        entity.setCollisionRadius(def.collisionRadius);

        // Add to entity manager
        addEntity(entity);

        return entity;
    }
}
