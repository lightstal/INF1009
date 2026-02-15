package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * <p>
 * LinearMovement moves an entity in a fixed direction at a constant speed.
 * The direction is normalized so the speed stays consistent.
 * </p>
 */
public class LinearMovement implements MovementBehaviour {
    /** Direction vector for movement */
    private Vector2 direction;

    /** Movement speed */
    private float speed;

    /**
     * <p>
     * Constructor to set direction and speed.
     * Direction will be normalized to keep movement speed consistent.
     * </p>
     *
     * @param direction movement direction
     * @param speed movement speed
     */
    public LinearMovement(Vector2 direction, float speed) {
        this.direction = direction.nor(); // Normalize to ensure consistent speed
        this.speed = speed;
    }

    /**
     * <p>
     * Move the entity in a straight line based on direction and speed.
     * Uses deltaTime for smooth frame-based movement.
     * </p>
     *
     * @param entity the entity to move
     * @param deltaTime time passed since last frame
     */
    @Override
    public void move(Entity entity, float deltaTime) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.getPosition().x += direction.x * speed * deltaTime;
            tc.getPosition().y += direction.y * speed * deltaTime;
        }
    }

    /**
     * <p>Get the current movement direction.</p>
     * @return direction vector
     */
    public Vector2 getDirection() {
        return direction;
    }

    /**
     * <p>
     * Set a new movement direction.
     * The direction will be normalized.
     * </p>
     *
     * @param direction new direction vector
     */
    public void setDirection(Vector2 direction) {
        this.direction = direction.nor();
    }

    /**
     * <p>Get the current movement speed.</p>
     * @return speed value
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * <p>Set a new movement speed.</p>
     * @param speed new speed value
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * <p>
     * Reverse the X direction.
     * Used for bouncing on horizontal boundaries.
     * </p>
     */
    public void reverseX() {
        direction.x = -direction.x;
    }

    /**
     * <p>
     * Reverse the Y direction.
     * Used for bouncing on vertical boundaries.
     * </p>
     */
    public void reverseY() {
        direction.y = -direction.y;
    }

    /**
     * <p>
     * Reverse both X and Y directions.
     * This makes the entity move in the opposite direction.
     * </p>
     */
    public void reverseDirection() {
        reverseX();
        reverseY();
    }
}
