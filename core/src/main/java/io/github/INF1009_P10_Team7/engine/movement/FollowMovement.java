package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2; // USE YOUR TEAM'S VECTOR

public class FollowMovement implements MovementBehaviour {
    private Entity target;
    private float speed;

    public FollowMovement(Entity target, float speed) {
        this.target = target;
        this.speed = speed;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        // Get the transform of the entity that is moving
        TransformComponent myTransform = entity.getComponent(TransformComponent.class);
        // Get the transform of the target entity to follow
        TransformComponent targetTransform = target.getComponent(TransformComponent.class);

        if (myTransform != null && targetTransform != null) {
            Vector2 myPos = myTransform.getPosition();
            Vector2 targetPos = targetTransform.getPosition();

            // Calculate the direction vector: (Target - Current)
            // Using your custom Vector2 constructor
            Vector2 direction = new Vector2(targetPos.x - myPos.x, targetPos.y - myPos.y);
            
            // Only move if we aren't already at the target position
            // Note: If your custom Vector2 doesn't have .isZero() or .nor(), 
            // we calculate the length manually to avoid the LibGDX mismatch.
            float distance = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y);
            
            if (distance > 0) {
                // Normalize manually: direction / length
                float dirX = direction.x / distance;
                float dirY = direction.y / distance;
                
                // Update position based on normalized direction, speed, and time
                myPos.x += dirX * speed * deltaTime;
                myPos.y += dirY * speed * deltaTime;
            }
        }
    }

    // Getters and Setters as shown in your UML
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}