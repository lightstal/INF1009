package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Circle-based collision detection (default strategy).
 *
 * Demonstrates Inheritance — methods are non-static and can be overridden
 * by subclasses (e.g. AABBCollisionDetection) to provide alternative
 * detection algorithms without modifying existing code.
 *
 * CollisionManager holds a CollisionDetection reference, so swapping
 * in a subclass changes behaviour polymorphically.
 */
public class CollisionDetection {

    /**
     * Checks whether two collidable objects are overlapping (circle-circle).
     *
     * @param obj1 first collidable
     * @param obj2 second collidable
     * @return true if the objects overlap
     */
    public boolean checkCollision(ICollidable obj1, ICollidable obj2) {
        if (obj1 == null || obj2 == null) {
            return false;
        }

        if (!obj1.isCollidable() || !obj2.isCollidable()) {
            return false;
        }

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        float radius1 = obj1.getCollisionRadius();
        float radius2 = obj2.getCollisionRadius();

        float dx = pos2.x - pos1.x;
        float dy = pos2.y - pos1.y;
        float distanceSquared = dx * dx + dy * dy;

        float radiusSum = radius1 + radius2;
        float radiusSumSquared = radiusSum * radiusSum;

        return distanceSquared < radiusSumSquared;
    }

    /**
     * Returns detailed collision info if the two objects overlap, or null otherwise.
     * Can be overridden by subclasses for different detection shapes.
     *
     * @param obj1 first collidable
     * @param obj2 second collidable
     * @return CollisionInfo with normal and penetration depth, or null
     */
    public CollisionInfo getCollisionInfo(ICollidable obj1, ICollidable obj2) {
        if (!checkCollision(obj1, obj2)) {
            return null;
        }

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        float radius1 = obj1.getCollisionRadius();
        float radius2 = obj2.getCollisionRadius();

        float dx = pos2.x - pos1.x;
        float dy = pos2.y - pos1.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            distance = 0.01f;
        }

        Vector2 collisionNormal = new Vector2(dx / distance, dy / distance);

        float penetrationDepth = (radius1 + radius2) - distance;

        return new CollisionInfo(
            obj1.getObjectId(),
            obj2.getObjectId(),
            penetrationDepth,
            collisionNormal
        );
    }
}
