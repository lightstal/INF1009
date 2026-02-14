package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

public class CollisionResolution {

    public enum ResolutionType {
        BOUNCE,
        DESTROY,
        PASS_THROUGH
    }

    public interface CollisionCallback {
        void onCollision(ICollidable obj1, ICollidable obj2, CollisionInfo info);
    }


    public static void resolveBounce(ICollidable obj1, ICollidable obj2, CollisionInfo info) {
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

    public static void resolveDestroy(ICollidable obj1, ICollidable obj2) {
        obj1.deactivate();
        obj2.deactivate();
    }

    public static void resolveDestroySingle(ICollidable obj) {
        obj.deactivate();
    }

    public static void resolve(ICollidable obj1, ICollidable obj2,
                               CollisionInfo info,
                               ResolutionType type, CollisionCallback callback) {

        if (callback != null) {
            callback.onCollision(obj1, obj2, info);
        }

        switch (type) {
            case BOUNCE:
                resolveBounce(obj1, obj2, info);
                break;
            case DESTROY:
                resolveDestroy(obj1, obj2);
                break;
            case PASS_THROUGH:
                break;
        }
    }
}
