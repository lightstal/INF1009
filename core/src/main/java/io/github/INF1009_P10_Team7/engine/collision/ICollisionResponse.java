package io.github.INF1009_P10_Team7.engine.collision;

/**
 * Strategy interface for collision resolution (Strategy Pattern).
 *
 * Replaces the ResolutionType enum + switch statement with polymorphism.
 * Each resolution behaviour implements this interface, allowing new
 * behaviours to be added without modifying existing code (Open/Closed Principle).
 *
 * FunctionalInterface allows lambda/anonymous implementations for custom responses.
 */
@FunctionalInterface
public interface ICollisionResponse {
    void resolve(ICollidable obj1, ICollidable obj2, CollisionInfo info);
}
