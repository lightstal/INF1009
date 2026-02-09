package io.github.INF1009_P10_Team7.engine.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.events.EventListener;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;
import io.github.INF1009_P10_Team7.engine.events.EventType;

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
}