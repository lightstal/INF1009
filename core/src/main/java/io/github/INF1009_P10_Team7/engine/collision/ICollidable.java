package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * <p>Interface for any object that can collide.
 * The collision system works through this and never needs to know the
 * concrete type.</p>
 */
public interface ICollidable {

    /** @return the object's position */
    Vector2 getPosition();

    /** @return the collision radius */
    float getCollisionRadius();

    /** @return a unique identifier for this object */
    String getObjectId();

    /** @return {@code true} if this object should be checked for collisions */
    boolean isCollidable();

    /** @return {@code true} if this object can be moved during resolution */
    boolean isMovable();

    /** @return the object's current velocity */
    Vector2 getVelocity();

    /** <p>Deactivates this object, removing it from the simulation.</p> */
    void deactivate();
}
