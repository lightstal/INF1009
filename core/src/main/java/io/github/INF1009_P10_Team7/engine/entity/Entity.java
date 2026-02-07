package io.github.INF1009_P10_Team7.engine.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Abstract base class for all entities in the game.
public abstract class Entity {
    private final UUID id;
    private boolean active;
    private final Map<Class<? extends Component>, Component> components;

    // Creates a new entity with a unique ID and initializes its component map.
    public Entity() {
        this.id = UUID.randomUUID();
        this.active = true;
        this.components = new HashMap<>();
    }

    // Gets the unique ID of this entity.
    public UUID getId() {
        return id;
    }

    // Adds a component to this entity.
    // If a component of the same type already exists, it is replaced.
    // @param component The component to add
    public void addComponent(Component component) {
        Class<? extends Component> type = component.getClass();

        // Remove existing component of the same type if present
        if (components.containsKey(type)) {
            Component existing = components.get(type);
            existing.onRemoved(this);
        }

        components.put(type, component);
        component.onAdded(this);
    }

    // Removes a component of the specified type from this entity.
    public void removeComponent(Class<? extends Component> type) {
        Component component = components.remove(type);
        if (component != null) {
            component.onRemoved(this);
        }
    }

    // Gets the component of the specified type from this entity.
    // @param type The class type of the component to retrieve
    // @return The component instance, or null if not found
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }

    // Checks if this entity has a component of the specified type.
    // @param type The class type of the component to check
    // @return true if the component exists, false otherwise
    public boolean hasComponent(Class<? extends Component> type) {
        return components.containsKey(type);
    }

    // Sets whether this entity is active.
    // @param active true to activate, false to deactivate
    public void setActive(boolean active) {
        this.active = active;
    }

    // Checks if this entity is active.
    // @return true if active, false otherwise
    public boolean isActive() {
        return active;
    }

    // Updates this entity and all its components.
    // @param deltaTime The time elapsed since the last update in seconds
    public void update(float deltaTime) {
        if (!active) {
            return;
        }

        for (Component component : components.values()) {
            component.update(deltaTime);
        }
    }
}