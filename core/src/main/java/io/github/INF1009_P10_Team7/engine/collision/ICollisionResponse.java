package io.github.INF1009_P10_Team7.engine.collision;

/**
 * <p>Strategy interface for collision resolution. Can be implemented
 * with lambdas since it is a {@link FunctionalInterface}.</p>
 *
 * <p>New response behaviours can be added without modifying
 * existing code.</p>
 */
@FunctionalInterface
public interface ICollisionResponse {

    /**
     * <p>Resolves a collision between two collidable objects.</p>
     *
     * @param obj1 first collidable
     * @param obj2 second collidable
     * @param info collision details (normal and penetration depth)
     */
    void resolve(ICollidable obj1, ICollidable obj2, CollisionInfo info);
}
