package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Provides built-in collision response strategies as static constants.
 *
 * Demonstrates Polymorphism (Strategy Pattern):
 * - Each response (BOUNCE, DESTROY, PASS_THROUGH) implements ICollisionResponse.
 * - No switch/if-else — each strategy resolves itself via its own resolve() method.
 * - New strategies can be created externally (e.g. in GameScene) without
 *   modifying this class (Open/Closed Principle).
 *
 * The old enum + switch approach violated OCP because adding a new type
 * required editing both the enum and the switch in resolve().
 */
public class CollisionResolution {

    /**
     * BOUNCE — separates overlapping objects and reflects their velocities.
     * Only affects movable objects (isMovable() == true).
     */
    public static final ICollisionResponse BOUNCE = (obj1, obj2, info) -> {
        resolveBounce(obj1, obj2, info);
    };

    /**
     * DESTROY — deactivates both colliding objects.
     */
    public static final ICollisionResponse DESTROY = (obj1, obj2, info) -> {
        obj1.deactivate();
        obj2.deactivate();
    };

    /**
     * PASS_THROUGH — detects collision but takes no action.
     * Useful for triggers, pickups, or sensor zones.
     */
    public static final ICollisionResponse PASS_THROUGH = (obj1, obj2, info) -> {
        // Intentionally empty — collision is detected but no physics response
    };

    // ---- Private helper for bounce logic ----

    private static void resolveBounce(ICollidable obj1, ICollidable obj2, CollisionInfo info) {
        if (obj1 == null || obj2 == null || info == null) return;

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        if (pos1 == null || pos2 == null) return;

        Vector2 n = info.getCollisionNormal();
        float penetration = info.getPenetrationDepth();

        boolean m1 = obj1.isMovable();
        boolean m2 = obj2.isMovable();

        // ---- Separate overlap ----
        if (m1 && m2) {
            float half = penetration / 2f;

            pos1.x -= n.x * half;
            pos1.y -= n.y * half;

            pos2.x += n.x * half;
            pos2.y += n.y * half;

        } else if (m1) {
            pos1.x -= n.x * penetration;
            pos1.y -= n.y * penetration;

        } else if (m2) {
            pos2.x += n.x * penetration;
            pos2.y += n.y * penetration;

        } else {
            return;
        }

        // ---- Reflect velocity only for movable ----
        if (m1) {
            Vector2 v1 = obj1.getVelocity();
            float dot = v1.x * n.x + v1.y * n.y;
            v1.x -= 2f * dot * n.x;
            v1.y -= 2f * dot * n.y;
        }

        if (m2) {
            Vector2 v2 = obj2.getVelocity();
            float dot = v2.x * n.x + v2.y * n.y;
            v2.x -= 2f * dot * n.x;
            v2.y -= 2f * dot * n.y;
        }
    }
}
