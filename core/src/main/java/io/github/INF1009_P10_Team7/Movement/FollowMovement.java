package io.github.INF1009_P10_Team7.Movement;

import com.badlogic.gdx.math.Vector2;
import io.github.INF1009_P10_Team7.Entity.Entity;

public class FollowMovement implements iMovementBehaviour {

    private Entity target;
    private float speed;
    private final float STOP_DISTANCE = 5.0f; // Stop when this close to prevent jitter

    public FollowMovement(Entity target, float speed) {
        this.target = target;
        this.speed = speed;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        // 1. Safety Check: If target is null or dead, stop moving
        if (target == null || target.isExpired()) {
            return;
        }

        Vector2 myPos = entity.getPosition();
        Vector2 targetPos = target.getPosition();

        // 2. Calculate Direction (Target - Me)
        float dx = targetPos.x - myPos.x;
        float dy = targetPos.y - myPos.y;
        
        // 3. Calculate Distance
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 4. Move only if we are outside the stop distance
        if (distance > STOP_DISTANCE) {
            // Normalize (Direction / Distance)
            float dirX = dx / distance;
            float dirY = dy / distance;

            // Calculate movement amount
            float moveX = dirX * speed * deltaTime;
            float moveY = dirY * speed * deltaTime;

            // Apply to Entity
            entity.move(moveX, moveY);
        }
    }

    // Getters and Setters
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}