package io.github.INF1009_P10_Team7.engine.entity;

import io.github.INF1009_P10_Team7.engine.collision.CollisionResolution;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Data class that stores the blueprint for creating an entity.
 * Does NOT instantiate the entity - just holds the data.
 */
public class EntityDefinition {
    public enum EntityType {
        PLAYER,
        ENEMY,
        STATIC_OBJECT,
        LINEAR_ENTITY,
        AI_WANDERER,
        INACTIVE_ENTITY
    }

    public final String name;
    public final EntityType type;
    public final Vector2 position;
    public final float rotation;
    public final float collisionRadius;
    public final boolean isActive;

    // Physics properties (optional)
    public final Vector2 initialVelocity;
    public final float mass;

    // Movement properties (optional)
    public final Vector2 linearDirection;
    public final float movementSpeed;
    public final Entity targetEntity; // For follow movement

    // Collision properties
    public final CollisionResolution.ResolutionType resolutionType;

    private EntityDefinition(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.position = builder.position;
        this.rotation = builder.rotation;
        this.collisionRadius = builder.collisionRadius;
        this.isActive = builder.isActive;
        this.initialVelocity = builder.initialVelocity;
        this.mass = builder.mass;
        this.linearDirection = builder.linearDirection;
        this.movementSpeed = builder.movementSpeed;
        this.targetEntity = builder.targetEntity;
        this.resolutionType = builder.resolutionType;
    }

    public static class Builder {
        // Required
        private final String name;
        private final EntityType type;
        private final Vector2 position;

        // Optional with defaults
        private float rotation = 0f;
        private float collisionRadius = 20f;
        private boolean isActive = true;
        private Vector2 initialVelocity = null;
        private float mass = 1.0f;
        private Vector2 linearDirection = null;
        private float movementSpeed = 0f;
        private Entity targetEntity = null;
        private CollisionResolution.ResolutionType resolutionType = CollisionResolution.ResolutionType.BOUNCE;
k
        public Builder(String name, EntityType type, Vector2 position) {
            this.name = name;
            this.type = type;
            this.position = position;
        }

        public Builder rotation(float rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder collisionRadius(float radius) {
            this.collisionRadius = radius;
            return this;
        }

        public Builder isActive(boolean active) {
            this.isActive = active;
            return this;
        }

        public Builder physics(Vector2 velocity, float mass) {
            this.initialVelocity = velocity;
            this.mass = mass;
            return this;
        }

        public Builder linearMovement(Vector2 direction, float speed) {
            this.linearDirection = direction;
            this.movementSpeed = speed;
            return this;
        }

        public Builder followMovement(Entity target, float speed) {
            this.targetEntity = target;
            this.movementSpeed = speed;
            return this;
        }

        public Builder aiMovement(float speed) {
            this.movementSpeed = speed;
            return this;
        }

        public Builder resolutionType(CollisionResolution.ResolutionType type) {
            this.resolutionType = type;
            return this;
        }

        public EntityDefinition build() {
            return new EntityDefinition(this);
        }
    }
}
