package io.github.INF1009_P10_Team7.engine.collision;

/**
 * Interface for collision management.
 * Decouples the engine from the concrete CollisionManager.
 */
public interface ICollisionSystem {

    void registerCollidable(ICollidable collidable, CollisionResolution.ResolutionType resolutionType);

    void unregisterCollidable(ICollidable collidable);

    void setCollisionSound(String soundPath);

    void update(float deltaTime);

    void clear();

    int getCollidableCount();
    void onCollision(ICollidable obj1, ICollidable obj2, CollisionInfo info);

}
