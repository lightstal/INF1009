package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Handles collision resolution - determines what happens when objects collide.
 * Supports different resolution strategies: bounce, destroy, or pass-through.
 *
 * IMPORTANT:
 * - FIXED objects = NO PhysicComponent
 * - MOVABLE objects = has PhysicComponent
 * - Bounce works for movable-vs-fixed and movable-vs-movable
 */
public class CollisionResolution {

    public enum ResolutionType {
        BOUNCE,
        DESTROY,
        PASS_THROUGH
    }

    public interface CollisionCallback {
        void onCollision(Entity entity1, Entity entity2, CollisionDetection.CollisionInfo info);
    }

    /**
     * Bounce resolution supporting:
     * - movable vs movable (both bounce)
     * - movable vs fixed (only movable bounces)
     * - fixed vs fixed (do nothing)
     */
    public static void resolveBounce(Entity entity1, Entity entity2, CollisionDetection.CollisionInfo info) {
        if (entity1 == null || entity2 == null || info == null) return;

        PhysicComponent p1 = entity1.getComponent(PhysicComponent.class);
        PhysicComponent p2 = entity2.getComponent(PhysicComponent.class);
        TransformComponent t1 = entity1.getComponent(TransformComponent.class);
        TransformComponent t2 = entity2.getComponent(TransformComponent.class);

        if (t1 == null || t2 == null) return;

        Vector2 n = info.getCollisionNormal();
        float penetration = info.getPenetrationDepth();

        boolean m1 = (p1 != null); // movable if has physics
        boolean m2 = (p2 != null);

        // ---- Separate overlap ----
        if (m1 && m2) {
            // both move half
            float half = penetration / 2f;

            Vector2 pos1 = t1.getPosition();
            Vector2 pos2 = t2.getPosition();

            pos1.x -= n.x * half;
            pos1.y -= n.y * half;

            pos2.x += n.x * half;
            pos2.y += n.y * half;

        } else if (m1) {
            // only entity1 moves
            Vector2 pos1 = t1.getPosition();
            pos1.x -= n.x * penetration;
            pos1.y -= n.y * penetration;

        } else if (m2) {
            // only entity2 moves
            Vector2 pos2 = t2.getPosition();
            pos2.x += n.x * penetration;
            pos2.y += n.y * penetration;

        } else {
            // both fixed
            return;
        }

        // ---- Reflect velocity only for movable ----
        if (m1) {
            Vector2 v1 = p1.getVelocity();
            float dot = v1.x * n.x + v1.y * n.y;
            v1.x -= 2f * dot * n.x;
            v1.y -= 2f * dot * n.y;
        }

        if (m2) {
            Vector2 v2 = p2.getVelocity();
            float dot = v2.x * n.x + v2.y * n.y;
            v2.x -= 2f * dot * n.x;
            v2.y -= 2f * dot * n.y;
        }
    }

    public static void resolveDestroy(Entity entity1, Entity entity2) {
        entity1.setActive(false);
        entity2.setActive(false);
    }

    public static void resolveDestroySingle(Entity entity) {
        entity.setActive(false);
    }

    public static void resolve(Entity entity1, Entity entity2,
                               CollisionDetection.CollisionInfo info,
                               ResolutionType type, CollisionCallback callback) {

        if (callback != null) {
            callback.onCollision(entity1, entity2, info);
        }

        switch (type) {
            case BOUNCE:
                resolveBounce(entity1, entity2, info);
                break;
            case DESTROY:
                resolveDestroy(entity1, entity2);
                break;
            case PASS_THROUGH:
                break;
        }
    }
}
