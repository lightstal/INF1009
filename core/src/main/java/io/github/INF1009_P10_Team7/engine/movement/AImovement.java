package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import java.util.Random;

public class AImovement implements MovementBehaviour {
    private float speed;
    private float timer = 0;
    private float dx = 0, dy = 0;
    private Random random;

    public AImovement(float speed) {
        this.speed = speed;
        this.random = new Random();
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        timer += deltaTime;
        if (timer > 1.0f) { // Change direction every 1 second
            dx = random.nextFloat() * 2f - 1f; // Random between -1 and 1
            dy = random.nextFloat() * 2f - 1f; // Random between -1 and 1

            // Normalize to maintain consistent speed
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                dx /= length;
                dy /= length;
            }

            timer = 0;
        }
        if (tc != null) {
            tc.getPosition().x += dx * speed * deltaTime;
            tc.getPosition().y += dy * speed * deltaTime;
        }
    }

    // Getters and setters
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}
