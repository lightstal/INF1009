
package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

public class LinearMovement implements MovementBehaviour {
    private Vector2 direction;
    private float speed;

    public LinearMovement(Vector2 direction, float speed) {
        this.direction = direction.nor(); // Normalize to ensure consistent speed
        this.speed = speed;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.getPosition().x += direction.x * speed * deltaTime;
            tc.getPosition().y += direction.y * speed * deltaTime;
        }
    }

    // Getters and setters
    public Vector2 getDirection() {
        return direction;
    }

    public void setDirection(Vector2 direction) {
        this.direction = direction.nor();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // Reverses the X component of direction (for horizontal boundary bouncing).
    public void reverseX() {
        direction.x = -direction.x;
    }

    // Reverses the Y component of direction (for vertical boundary bouncing).
    public void reverseY() {
        direction.y = -direction.y;
    }

    // Reverses both X and Y direction components.
    public void reverseDirection() {
        reverseX();
        reverseY();
    }
}
