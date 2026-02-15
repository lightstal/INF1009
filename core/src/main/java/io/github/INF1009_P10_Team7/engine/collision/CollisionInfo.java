package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Immutable data class holding information about a detected collision.
 * Demonstrates Encapsulation:
 * - All fields are private final (no direct access).
 * - Getters return copies of mutable objects (Vector2) to prevent
 *   external code from corrupting internal state.
 * - Class is final — cannot be subclassed to break invariants.
 */
public final class CollisionInfo {
    private final String objectId1;
    private final String objectId2;
    private final float penetrationDepth;
    private final Vector2 collisionNormal;

    public CollisionInfo(String objectId1, String objectId2, float penetrationDepth, Vector2 collisionNormal) {
        this.objectId1 = objectId1;
        this.objectId2 = objectId2;
        this.penetrationDepth = penetrationDepth;
        this.collisionNormal = new Vector2(collisionNormal); // Defensive copy on input
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

    /**
     * Returns a defensive copy of the collision normal.
     * This ensures external code cannot modify our internal state.
     */
    public Vector2 getCollisionNormal() {
        return new Vector2(collisionNormal); // Defensive copy on output (Encapsulation)
    }

    public boolean involves(String objectId) {
        return objectId1.equals(objectId) || objectId2.equals(objectId);
    }
}
