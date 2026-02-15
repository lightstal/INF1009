package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Abstraction for any object that can participate in collision detection.
 *
 * Demonstrates Abstraction — the collision system works with this interface
 * and never needs to know the concrete type (GameEntity, etc.).
 *
 * Also enables Polymorphism — any class implementing ICollidable can be
 * passed to CollisionDetection and CollisionManager.
 */
public interface ICollidable {

    Vector2 getPosition();

    float getCollisionRadius();

    String getObjectId();

    boolean isCollidable();

    boolean isMovable();

    Vector2 getVelocity();

    void deactivate();
}
