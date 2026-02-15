package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import java.util.Random;

/**
 * <p>
 * This class handles AI random movement.
 * The entity will move in a random direction and change direction every 1 second.
 * </p>
 */
public class AImovement implements MovementBehaviour {
    /** Movement speed of the AI */
    private float speed;

    /** Timer to control when direction should change */
    private float timer = 0;

    /** Current movement direction on x and y axis */
    private float dx = 0, dy = 0;

    /** Random generator to pick new directions */
    private Random random;

    /**
     * <p>
     * Constructor to set the speed of the AI movement.
     * </p>
     *
     * @param speed movement speed value
     */
    public AImovement(float speed) {
        this.speed = speed;
        this.random = new Random();
    }

    /**
     * <p>
     * Move the entity in a random direction.
     * Direction changes every 1 second.
     * Movement is scaled using deltaTime for smooth motion.
     * </p>
     *
     * @param entity the entity that will be moved
     * @param deltaTime time passed since last frame
     */
    @Override
    public void move(Entity entity, float deltaTime) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);

        // increase timer
        timer += deltaTime;

        // change direction every 1 second
        if (timer > 1.0f) {
            // random direction between -1 and 1
            dx = random.nextFloat() * 2f - 1f;
            dy = random.nextFloat() * 2f - 1f;

            // normalize so speed stays consistent
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }

            // reset timer after changing direction
            timer = 0;
        }

        // update position if transform exists
        if (tc != null) {
            tc.getPosition().x += dx * speed * deltaTime;
            tc.getPosition().y += dy * speed * deltaTime;
        }
    }

    /**
     * <p>Get current movement speed.</p>
     * @return current speed
     */
    public float getSpeed() { return speed; }

    /**
     * <p>Set a new movement speed.</p>
     * @param speed new speed value
     */
    public void setSpeed(float speed) { this.speed = speed; }
}
