package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Interface for entities that can participate in collision detection.
 * Entities implementing this interface can be checked for collisions.
 */
public interface ICollidable {

    /**
     * Gets the center position of the collidable object.
     * @return The center position as a Vector2
     */
    Vector2 getPosition();

    /**
     * Gets the radius of the collidable object for circular collision detection.
     * @return The collision radius
     */
    float getCollisionRadius();

    /**
     * Gets the unique identifier of this collidable object.
     * @return The object's ID as a String
     */
    String getObjectId();

    /**
     * Checks if this collidable object is active and should participate in collision detection.
     * @return true if active, false otherwise
     */
    boolean isCollidable();

    /**
     * Returns true if this object is movable (has physics/velocity).
     * Movable objects get their velocity reflected on bounce.
     * Fixed objects (return false) do not move during resolution.
     */
    boolean isMovable();

    /**
     * Gets the velocity of this object.
     * Only meaningful if isMovable() returns true.
     * @return The velocity vector
     */
    Vector2 getVelocity();

    /**
     * Deactivates this object (e.g. for DESTROY resolution).
     */
    void deactivate();
}

