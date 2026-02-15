package io.github.INF1009_P10_Team7.engine.collision;

/**
 * Interface for collision management (Abstraction).
 *
 * Decouples the engine from the concrete CollisionManager so that
 * the simulation layer (GameScene) depends only on this abstraction,
 * not on the implementation (Dependency Inversion Principle).
 *
 * Uses ICollisionResponse (Strategy Pattern) instead of an enum,
 * enabling Polymorphism — each collidable can have its own resolution
 * behaviour without switch statements.
 */
public interface ICollisionSystem {

    /** Registers a collidable with its collision response strategy. */
    void registerCollidable(ICollidable collidable, ICollisionResponse response);

    /** Removes a collidable from the system. */
    void unregisterCollidable(ICollidable collidable);

    /** Runs collision detection and resolution for the current frame. */
    void update(float deltaTime);

    /** Removes all registered collidables. */
    void clear();

    /** Returns the number of currently registered collidables. */
    int getCollidableCount();

}
