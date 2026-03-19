package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * <p>Provides collision response strategies as static constants.
 * Each one implements {@link ICollisionResponse}.</p>
 *
 * <p>New strategies can be added externally by creating a new
 * {@link ICollisionResponse} implementation, for example using
 * a lambda passed into {@link CollisionManager#registerCollidable}.</p>
 */
public class CollisionResolution {

    /**
     * <p>BOUNCE — separates overlapping objects and reflects
     * their velocities. Only affects movable objects.</p>
     */
    public static final ICollisionResponse BOUNCE = (obj1, obj2, info) -> {
        resolveBounce(obj1, obj2, info);
    };

    /** <p>DESTROY — deactivates both colliding objects.</p> */
    public static final ICollisionResponse DESTROY = (obj1, obj2, info) -> {
        obj1.deactivate();
        obj2.deactivate();
    };

    /**
     * <p>PASS_THROUGH — detects the collision but takes no action.
     * For triggers or sensor zones.</p>
     */
    public static final ICollisionResponse PASS_THROUGH = (obj1, obj2, info) -> {
        // Intentionally empty — collision is detected but no physics response
    };

    /**
     * <p>Separates overlapping objects and reflects their velocities
     * along the collision normal.</p>
     */
    private static void resolveBounce(ICollidable obj1, ICollidable obj2, CollisionInfo info) {
        if (obj1 == null || obj2 == null || info == null) return;

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        if (pos1 == null || pos2 == null) return;

        Vector2 n = info.getCollisionNormal();
        float penetration = info.getPenetrationDepth();

        boolean m1 = obj1.isMovable();
        boolean m2 = obj2.isMovable();

        // Separate overlap
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

        // Reflect velocity but only for movable
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
