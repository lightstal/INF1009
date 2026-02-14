package io.github.INF1009_P10_Team7.Movement;

import io.github.INF1009_P10_Team7.Entity.Entity;
import java.util.Random;

public class AIMovement implements iMovementBehaviour {
    
    private float speed;
    private float changeInterval; // How often to change direction (e.g. 1.0s)
    private float timer;
    
    private float dx, dy;
    private final Random random;

    public AIMovement(float speed, float changeInterval) { 
        this.speed = speed;
        this.changeInterval = changeInterval;
        this.random = new Random();
        
        // Set timer to interval so it picks a direction immediately on the first frame
        this.timer = changeInterval; 
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        // 1. Update Timer
        timer += deltaTime;

        // 2. Time to change direction?
        if (timer >= changeInterval) {
            pickNewDirection();
            timer = 0;
        }

        // 3. Apply Movement using Entity's built-in method
        // Move amount = Direction * Speed * Time
        entity.move(dx * speed * deltaTime, dy * speed * deltaTime);
    }

    private void pickNewDirection() {
        // Random between -1 and 1
        dx = random.nextFloat() * 2f - 1f; 
        dy = random.nextFloat() * 2f - 1f; 
        
        // Normalize to ensure diagonal movement isn't faster
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }
    }
    
    // Getters and Setters
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}