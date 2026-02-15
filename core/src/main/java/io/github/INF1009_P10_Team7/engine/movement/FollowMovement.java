package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2; 
/**
 * <p>
 * This class makes an entity follow a target entity.
 * The entity will always move towards the target position.
 * </p>
 */
public class FollowMovement implements MovementBehaviour {
    /** The entity that we want to follow */
    private Entity target;

    /** Movement speed when following the target */
    private float speed;

    /**
     * <p>
     * Constructor to set the target and speed.
     * </p>
     *
     * @param target the entity to follow
     * @param speed movement speed
     */
    public FollowMovement(Entity target, float speed) {
        this.target = target;
        this.speed = speed;
    }

    /**
     * <p>
     * Move this entity towards the target entity.
     * Uses direction and distance to calculate smooth movement.
     * </p>
     *
     * @param entity the entity that will move
     * @param deltaTime time passed since last frame
     */
    @Override
    public void move(Entity entity, float deltaTime) {
        TransformComponent myTransform = entity.getComponent(TransformComponent.class);
        TransformComponent targetTransform = target.getComponent(TransformComponent.class);

        if (myTransform != null && targetTransform != null) {
            Vector2 myPos = myTransform.getPosition();
            Vector2 targetPos = targetTransform.getPosition();

            // calculate direction from this entity to target
            float dx = targetPos.x - myPos.x;
            float dy = targetPos.y - myPos.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // move only if not already at the target
            if (distance > 0) {
                // Position = Position + (Direction / Distance) * Speed * Time
                myPos.x += (dx / distance) * speed * deltaTime;
                myPos.y += (dy / distance) * speed * deltaTime;
            }
        }
    }

    /**
     * <p>Get the current target entity.</p>
     * @return target entity
     */
    public Entity getTarget() { return target; }

    /**
     * <p>Set a new target entity to follow.</p>
     * @param target new target entity
     */
    public void setTarget(Entity target) { this.target = target; }

    /**
     * <p>Get current movement speed.</p>
     * @return speed value
     */
    public float getSpeed() { return speed; }

    /**
     * <p>Set a new movement speed.</p>
     * @param speed new speed value
     */
    public void setSpeed(float speed) { this.speed = speed; }
}
