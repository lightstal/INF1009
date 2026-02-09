package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Handles collision resolution - determines what happens when objects collide.
 * Supports different resolution strategies: bounce, destroy, or custom behaviors.
 */
public class CollisionResolution {

    /**
     * Enum defining different types of collision resolution behaviors.
     */
    public enum ResolutionType {
        BOUNCE,      // Objects bounce off each other (elastic collision)
        DESTROY,     // Objects are destroyed/deactivated
        PASS_THROUGH // Objects pass through each other (no physical response)
    }

    /**
     * Callback interface for custom collision responses.
     */
    public interface CollisionCallback {
        void onCollision(Entity entity1, Entity entity2, CollisionDetection.CollisionInfo info);
    }

    /**
     * Resolves a collision between two entities using elastic bounce.
     * Uses physics principles to compute new velocities after collision.
     *
     * @param entity1 First entity involved in collision
     * @param entity2 Second entity involved in collision
     * @param collisionInfo Collision information (normal, penetration depth, etc.)
     */
    public static void resolveBounce(Entity entity1, Entity entity2, CollisionDetection.CollisionInfo collisionInfo) {
        PhysicComponent physics1 = entity1.getComponent(PhysicComponent.class);
        PhysicComponent physics2 = entity2.getComponent(PhysicComponent.class);
        TransformComponent transform1 = entity1.getComponent(TransformComponent.class);
        TransformComponent transform2 = entity2.getComponent(TransformComponent.class);

        // Need physics components for bounce
        if (physics1 == null || physics2 == null || transform1 == null || transform2 == null) {
            return;
        }

        Vector2 collisionNormal = collisionInfo.getCollisionNormal();
        float penetrationDepth = collisionInfo.getPenetrationDepth();

        // Separate objects to prevent overlap
        separateObjects(transform1, transform2, collisionNormal, penetrationDepth);

        // Get velocities and masses
        Vector2 vel1 = physics1.getVelocity();
        Vector2 vel2 = physics2.getVelocity();
        float mass1 = physics1.getMass();
        float mass2 = physics2.getMass();

        // Calculate relative velocity
        float relativeVelX = vel1.x - vel2.x;
        float relativeVelY = vel1.y - vel2.y;

        // Calculate relative velocity in terms of the normal direction
        float velAlongNormal = relativeVelX * collisionNormal.x + relativeVelY * collisionNormal.y;

        // Do not resolve if velocities are separating
        if (velAlongNormal > 0) {
            return;
        }

        // Calculate restitution (bounciness) - 1.0 is perfectly elastic
        float restitution = 0.8f;

        // Calculate impulse scalar
        float impulseScalar = -(1 + restitution) * velAlongNormal;
        impulseScalar /= (1 / mass1 + 1 / mass2);

        // Apply impulse
        Vector2 impulse = new Vector2(
            impulseScalar * collisionNormal.x,
            impulseScalar * collisionNormal.y
        );

        // Update velocities
        vel1.x += impulse.x / mass1;
        vel1.y += impulse.y / mass1;
        vel2.x -= impulse.x / mass2;
        vel2.y -= impulse.y / mass2;
    }

    /**
     * Resolves collision by deactivating both entities.
     *
     * @param entity1 First entity involved in collision
     * @param entity2 Second entity involved in collision
     */
    public static void resolveDestroy(Entity entity1, Entity entity2) {
        entity1.setActive(false);
        entity2.setActive(false);
    }

    /**
     * Resolves collision by deactivating only one entity.
     *
     * @param entity The entity to deactivate
     */
    public static void resolveDestroySingle(Entity entity) {
        entity.setActive(false);
    }

    /**
     * Separates two overlapping objects to prevent them from being stuck together.
     *
     * @param transform1 Transform of first object
     * @param transform2 Transform of second object
     * @param collisionNormal Normal vector pointing from obj1 to obj2
     * @param penetrationDepth How much the objects are overlapping
     */
    private static void separateObjects(TransformComponent transform1, TransformComponent transform2,
                                        Vector2 collisionNormal, float penetrationDepth) {
        // Move objects apart by half the penetration depth each
        float separationDistance = penetrationDepth / 2.0f;

        Vector2 pos1 = transform1.getPosition();
        Vector2 pos2 = transform2.getPosition();

        // Move first object away from second
        pos1.x -= collisionNormal.x * separationDistance;
        pos1.y -= collisionNormal.y * separationDistance;

        // Move second object away from first
        pos2.x += collisionNormal.x * separationDistance;
        pos2.y += collisionNormal.y * separationDistance;
    }

    /**
     * Generic collision resolution that allows custom behavior.
     *
     * @param entity1 First entity
     * @param entity2 Second entity
     * @param collisionInfo Collision information
     * @param type Resolution type
     * @param callback Optional callback for custom behavior
     */
    public static void resolve(Entity entity1, Entity entity2,
                               CollisionDetection.CollisionInfo collisionInfo,
                               ResolutionType type, CollisionCallback callback) {
        // Call custom callback first if provided
        if (callback != null) {
            callback.onCollision(entity1, entity2, collisionInfo);
        }

        // Apply resolution strategy
        switch (type) {
            case BOUNCE:
                resolveBounce(entity1, entity2, collisionInfo);
                break;
            case DESTROY:
                resolveDestroy(entity1, entity2);
                break;
            case PASS_THROUGH:
                // Do nothing - objects pass through each other
                break;
        }
    }
}
