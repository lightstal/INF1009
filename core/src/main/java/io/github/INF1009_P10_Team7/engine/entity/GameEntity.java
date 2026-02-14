package io.github.INF1009_P10_Team7.engine.entity;

// For Collision
import io.github.INF1009_P10_Team7.engine.collision.ICollidable;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

// Represents a game entity with a name.
public class GameEntity extends Entity implements ICollidable {

    private String name;
    private float collisionRadius = 20f; // Default collision radius

    // Creates a new GameEntity with an empty name.
    public GameEntity() {
        super();
        this.name = "";
    }

    // Creates a new GameEntity with the specified name.
    public GameEntity(String name) {
        super();
        this.name = name;
    }

    // Gets the name of this entity.
    public String getName() {
        return name;
    }

    // Sets the name of this entity.
    public void setName(String name) {
        this.name = name;
    }

    // Sets the collision radius for this entity.
    public void setCollisionRadius(float radius) {
        this.collisionRadius = radius;
    }

    // For ICollidable:
    @Override
    public Vector2 getPosition() {
        TransformComponent transform = getComponent(TransformComponent.class);
        if (transform != null) {
            return transform.getPosition();
        }
        return new Vector2(0, 0);
    }

    @Override
    public float getCollisionRadius() {
        return collisionRadius;
    }

    @Override
    public String getObjectId() {
        return name + "_" + getId().toString();
    }

    @Override
    public boolean isCollidable() {
        return isActive();
    }

    // For ICollidable resolution methods:
    @Override
    public boolean isMovable() {
        return hasComponent(PhysicComponent.class);
    }

    @Override
    public Vector2 getVelocity() {
        PhysicComponent physics = getComponent(PhysicComponent.class);
        if (physics != null) {
            return physics.getVelocity();
        }
        return new Vector2(0, 0);
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    @Override
    public String toString() {
        return "GameEntity{name='" + name + "', id=" + getId() + ", active=" + isActive() + "}";
    }
}
