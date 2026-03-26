package io.github.INF1009_P10_Team7.engine.collision;

/**
 * ICollisionSystem, public contract for the collision subsystem.
 *
 * <p>Scenes depend on this narrow interface rather than the concrete
 * {@link CollisionManager} (DIP). This ensures scenes can register
 * collidables and unregister them on pickup/destruction without coupling
 * to the detection or resolution internals.</p>
 */
public interface ICollisionSystem {

    /**
     * Registers a collidable object with its response strategy.
     * Duplicate registrations (same object) are silently ignored.
     *
     * @param collidable the object to register
     * @param response the collision response to use when this object collides;
     * a {@code null} response falls back to
     * {@link CollisionResolution#PASS_THROUGH}
     */
    void registerCollidable(ICollidable collidable, ICollisionResponse response);

    /**
     * Removes a collidable object from the system. Typically called when an
     * entity is collected or destroyed so it no longer participates in
     * collision checks.
     *
     * @param collidable the object to unregister
     */
    void unregisterCollidable(ICollidable collidable);

    /**
     * Runs all collision checks for the current frame and invokes the
     * appropriate response for each new overlap detected.
     *
     * @param deltaTime seconds since the last frame
     */
    void update(float deltaTime);

    /** Removes all registered collidables and resets internal state. */
    void clear();

    /** @return the number of collidable objects currently registered */
    int getCollidableCount();
}
