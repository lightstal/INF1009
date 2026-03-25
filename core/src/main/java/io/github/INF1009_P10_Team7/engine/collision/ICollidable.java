package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * ICollidable — contract for any object that participates in collision detection.
 *
 * <p>Entities that can collide implement this interface (or inherit from
 * {@link io.github.INF1009_P10_Team7.engine.entity.GameEntity} which already
 * implements it). The {@link CollisionManager} uses these methods to test
 * overlaps, apply responses, and determine whether an object can be moved
 * or deactivated.</p>
 *
 * <p>Design note (ISP): only the methods actually needed for collision
 * processing are declared here; full entity state is not exposed.</p>
 */
public interface ICollidable {

    /** @return the world-space centre position of this object */
    Vector2 getPosition();

    /** @return the collision radius used for circle–circle overlap tests */
    float getCollisionRadius();

    /**
     * Returns a unique string identifier used to key this object in the
     * collision manager's response map and to build collision-pair keys.
     *
     * @return unique object ID (e.g. {@code "Player_<uuid>"})
     */
    String getObjectId();

    /**
     * @return {@code true} if this object should be included in collision
     *         checks this frame (typically mirrors {@code Entity.isActive()})
     */
    boolean isCollidable();

    /**
     * @return {@code true} if this object has a physics component and can
     *         have its position and velocity modified by the collision resolver
     */
    boolean isMovable();

    /**
     * @return the current velocity of this object, used by the bounce resolver
     *         to reflect it on collision; returns a zero vector for static objects
     */
    Vector2 getVelocity();

    /**
     * Deactivates this object (e.g. called by {@link CollisionResolution#DESTROY}).
     * Typically delegates to {@code Entity.setActive(false)}.
     */
    void deactivate();
}
