package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * <p>Stores information about a detected collision</p>
 *
 * <p>All fields are private and final, and getters return copies of
 * mutable objects so nothing outside can change the data.</p>
 */
public final class CollisionInfo {
    private final String objectId1;
    private final String objectId2;
    private final float penetrationDepth;
    private final Vector2 collisionNormal;

    /**
     * <p>Creates a new CollisionInfo. A copy of the collision normal is stored.</p>
     *
     * @param objectId1        ID of the first object
     * @param objectId2        ID of the second object
     * @param penetrationDepth how far the two objects overlap
     * @param collisionNormal  direction of the collision
     */
    public CollisionInfo(String objectId1, String objectId2, float penetrationDepth, Vector2 collisionNormal) {
        this.objectId1 = objectId1;
        this.objectId2 = objectId2;
        this.penetrationDepth = penetrationDepth;
        this.collisionNormal = new Vector2(collisionNormal); // Defensive copy on input
    }

    /** @return the first object's ID */
    public String getObjectId1() {
        return objectId1;
    }

    /** @return the second object's ID */
    public String getObjectId2() {
        return objectId2;
    }

    /** @return the penetration depth */
    public float getPenetrationDepth() {
        return penetrationDepth;
    }

    /**
     * <p>Returns a copy of the collision normal so outside code
     * cannot change the internal state.</p>
     *
     * @return a copy of the collision normal
     */
    public Vector2 getCollisionNormal() {
        return new Vector2(collisionNormal); // Defensive copy on output (Encapsulation)
    }

    /**
     * <p>Checks if this collision involves the given object.</p>
     *
     * @param objectId the ID to check
     * @return {@code true} if the object is part of this collision
     */
    public boolean involves(String objectId) {
        return objectId1.equals(objectId) || objectId2.equals(objectId);
    }
}
