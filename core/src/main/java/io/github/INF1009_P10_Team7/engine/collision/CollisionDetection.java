package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Handles collision detection between collidable objects.
 * Uses circle-to-circle collision detection based on distance and radius.
 */
public class CollisionDetection {

    /**
     * Represents a collision event between two objects.
     */
    public static class CollisionInfo {
        private final String objectId1;
        private final String objectId2;
        private final float penetrationDepth;
        private final Vector2 collisionNormal;

        public CollisionInfo(String objectId1, String objectId2, float penetrationDepth, Vector2 collisionNormal) {
            this.objectId1 = objectId1;
            this.objectId2 = objectId2;
            this.penetrationDepth = penetrationDepth;
            this.collisionNormal = new Vector2(collisionNormal);
        }

        public String getObjectId1() {
            return objectId1;
        }

        public String getObjectId2() {
            return objectId2;
        }

        public float getPenetrationDepth() {
            return penetrationDepth;
        }

        public Vector2 getCollisionNormal() {
            return collisionNormal;
        }

        /**
         * Checks if a specific object ID is involved in this collision.
         */
        public boolean involves(String objectId) {
            return objectId1.equals(objectId) || objectId2.equals(objectId);
        }
    }

    /**
     * Checks if two collidable objects are colliding.
     * Uses circle-to-circle collision detection.
     *
     * @param obj1 First collidable object
     * @param obj2 Second collidable object
     * @return true if the objects are colliding, false otherwise
     */
    public static boolean checkCollision(ICollidable obj1, ICollidable obj2) {
        if (obj1 == null || obj2 == null) {
            return false;
        }

        // Don't check collision if either object is not collidable
        if (!obj1.isCollidable() || !obj2.isCollidable()) {
            return false;
        }

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        float radius1 = obj1.getCollisionRadius();
        float radius2 = obj2.getCollisionRadius();

        // Calculate distance between centers
        float dx = pos2.x - pos1.x;
        float dy = pos2.y - pos1.y;
        float distanceSquared = dx * dx + dy * dy;

        // Calculate sum of radii
        float radiusSum = radius1 + radius2;
        float radiusSumSquared = radiusSum * radiusSum;

        // Objects are colliding if distance is less than sum of radii
        return distanceSquared < radiusSumSquared;
    }

    /**
     * Gets detailed collision information between two objects.
     * Returns null if objects are not colliding.
     *
     * @param obj1 First collidable object
     * @param obj2 Second collidable object
     * @return CollisionInfo if colliding, null otherwise
     */
    public static CollisionInfo getCollisionInfo(ICollidable obj1, ICollidable obj2) {
        if (!checkCollision(obj1, obj2)) {
            return null;
        }

        Vector2 pos1 = obj1.getPosition();
        Vector2 pos2 = obj2.getPosition();

        float radius1 = obj1.getCollisionRadius();
        float radius2 = obj2.getCollisionRadius();

        // Calculate direction from obj1 to obj2
        float dx = pos2.x - pos1.x;
        float dy = pos2.y - pos1.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Avoid division by zero
        if (distance == 0) {
            distance = 0.01f;
        }

        // Normalize to get collision normal (pointing from obj1 to obj2)
        Vector2 collisionNormal = new Vector2(dx / distance, dy / distance);

        // Calculate penetration depth (how much they overlap)
        float penetrationDepth = (radius1 + radius2) - distance;

        return new CollisionInfo(
            obj1.getObjectId(),
            obj2.getObjectId(),
            penetrationDepth,
            collisionNormal
        );
    }
}
