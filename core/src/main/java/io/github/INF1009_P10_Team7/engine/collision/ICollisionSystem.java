package io.github.INF1009_P10_Team7.engine.collision;

/**
 * <p>Interface for collision management. The engine depends on this
 * instead of the concrete {@link CollisionManager}.</p>
 */
public interface ICollisionSystem {

    /**
     * <p>Registers a collidable with its response strategy.</p>
     *
     * @param collidable the object to register
     * @param response   the response strategy for this object
     */
    void registerCollidable(ICollidable collidable, ICollisionResponse response);

    /**
     * <p>Removes a collidable from the system.</p>
     *
     * @param collidable the object to unregister
     */
    void unregisterCollidable(ICollidable collidable);

    /**
     * <p>Runs collision detection and resolution for the current frame.</p>
     *
     * @param deltaTime time since last frame in seconds
     */
    void update(float deltaTime);

    /** <p>Removes all registered collidables.</p> */
    void clear();

    /** @return the number of registered collidables */
    int getCollidableCount();

}
