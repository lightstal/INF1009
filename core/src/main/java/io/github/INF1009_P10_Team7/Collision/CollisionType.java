package io.github.INF1009_P10_Team7.Collision;

public enum CollisionType {
    PASS_THROUGH, // Trigger event but don't stop movement
    DESTROY,      // Destroy one or both
    BOUNCE,       // Elastic collision
    SLIDE,        // Stop movement
    STATIC		  // Immovable Solid Object
}