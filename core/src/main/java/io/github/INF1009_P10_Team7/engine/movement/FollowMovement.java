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
    TransformComponent myTransform = entity.getComponent(TransformComponent.class);
    TransformComponent targetTransform = target.getComponent(TransformComponent.class);

    if (myTransform != null && targetTransform != null) {
        Vector2 myPos = myTransform.getPosition();
        Vector2 targetPos = targetTransform.getPosition();

        // Calculate direction manually
        float dx = targetPos.x - myPos.x;
        float dy = targetPos.y - myPos.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Only move if we aren't already there
        if (distance > 0) {
            // Move: Position = Position + (Direction / Distance) * Speed * Time
            myPos.x += (dx / distance) * speed * deltaTime;
            myPos.y += (dy / distance) * speed * deltaTime;
        }
    }
}

    // Getters and Setters as shown in your UML
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}