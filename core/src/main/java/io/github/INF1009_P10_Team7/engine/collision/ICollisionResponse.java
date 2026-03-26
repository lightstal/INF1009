package io.github.INF1009_P10_Team7.engine.collision;

/**
 * ICollisionResponse, Strategy interface for resolving a detected collision.
 *
 * <p>When the {@link CollisionManager} detects an overlap between two
 * {@link ICollidable} objects it looks up each object's registered response
 * and calls {@link #resolve}. Built-in strategies are provided as static
 * constants in {@link CollisionResolution}:</p>
 * <ul>
 * <li>{@link CollisionResolution#BOUNCE} , separate + reflect velocities</li>
 * <li>{@link CollisionResolution#DESTROY} , deactivate both objects</li>
 * <li>{@link CollisionResolution#PASS_THROUGH}, detect but do nothing</li>
 * </ul>
 *
 * <p>Custom responses can be created inline with lambdas and passed to
 * {@link CollisionManager#registerCollidable} without modifying any engine
 * class (OCP).</p>
 */
@FunctionalInterface
public interface ICollisionResponse {

    /**
     * Handles the response to a detected collision between two objects.
     *
     * @param obj1 the first colliding object
     * @param obj2 the second colliding object
     * @param collisionInfo details about the collision (normal, penetration depth)
     */
    void resolve(ICollidable obj1, ICollidable obj2, CollisionInfo collisionInfo);
}
