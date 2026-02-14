package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Immutable data class representing a collision event between two objects.
 * Contains information about which objects collided, the penetration depth,
 * and the collision normal vector.
 */
public final class CollisionInfo {
    private final String objectId1;
    private final String objectId2;
    private final float penetrationDepth;
    private final Vector2 collisionNormal;

    /**
     * Creates a new collision info object.
     *
     * @param objectId1 ID of the first object involved in the collision
     * @param objectId2 ID of the second object involved in the collision
     * @param penetrationDepth How deep the objects are overlapping
     * @param collisionNormal The normalized direction vector from obj1 to obj2
     */
    public CollisionInfo(String objectId1, String objectId2, float penetrationDepth, Vector2 collisionNormal) {
        this.objectId1 = objectId1;
        this.objectId2 = objectId2;
        this.penetrationDepth = penetrationDepth;
        this.collisionNormal = new Vector2(collisionNormal);
    }

    /**
     * Gets the ID of the first object in the collision.
     *
     * @return The first object's ID
     */
    public String getObjectId1() {
        return objectId1;
    }

    /**
     * Gets the ID of the second object in the collision.
     *
     * @return The second object's ID
     */
    public String getObjectId2() {
        return objectId2;
    }

    /**
     * Gets the penetration depth (overlap amount) of the collision.
     *
     * @return The penetration depth
     */
    public float getPenetrationDepth() {
        return penetrationDepth;
    }

    /**
     * Gets the collision normal vector (direction from obj1 to obj2).
     *
     * @return The collision normal vector
     */
    public Vector2 getCollisionNormal() {
        return collisionNormal;
    }

    /**
     * Checks if a specific object ID is involved in this collision.
     *
     * @param objectId The object ID to check
     * @return true if the object is involved in this collision, false otherwise
     */
    public boolean involves(String objectId) {
        return objectId1.equals(objectId) || objectId2.equals(objectId);
    }
}
