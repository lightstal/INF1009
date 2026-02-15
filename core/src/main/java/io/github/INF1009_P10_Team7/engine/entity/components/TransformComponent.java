package io.github.INF1009_P10_Team7.engine.entity.components;

import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Component that stores position and rotation data for an entity.
 * Represents the spatial transform of an entity in 2D space.
 */
public class TransformComponent implements IComponent {
    private Vector2 position;
    private float rotation;
    private Entity owner;

    // Creates a TransformComponent at the origin with no rotation.
    public TransformComponent() {
        this.position = new Vector2();
        this.rotation = 0f;
    }

    // Creates a TransformComponent with the specified x and y position and no rotation.
    public TransformComponent(float x, float y) {
        this.position = new Vector2(x, y);
        this.rotation = 0f;
    }

    // Creates a TransformComponent with the specified position and rotation.
    public TransformComponent(Vector2 position, float rotation) {
        this.position = new Vector2(position);
        this.rotation = rotation;
    }

    @Override
    public void onAdded(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void onRemoved(Entity owner) {
        this.owner = null;
    }

    @Override
    public void update(float deltaTime) {
        // TransformComponent typically doesn't update on its own
        // Position and rotation are modified by other components (e.g., PhysicComponent)
    }

    // Gets the position.
    public Vector2 getPosition() {
        return position;
    }

    // Sets the position.
    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    // Sets the position using x and y coordinates.
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    // Gets the rotation in degrees.
    public float getRotation() {
        return rotation;
    }

    // Sets the rotation in degrees.
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    // Gets the owner entity.
    public Entity getOwner() {
        return owner;
    }
}
