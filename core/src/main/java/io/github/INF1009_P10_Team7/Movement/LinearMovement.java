package io.github.INF1009_P10_Team7.Movement;

import com.badlogic.gdx.math.Vector2;
import io.github.INF1009_P10_Team7.Entity.Entity;

public class LinearMovement implements iMovementBehaviour {
	private final Vector2 initialDirection;
    private final float speed;
    private boolean isInitialized = false;

    public LinearMovement(float dirX, float dirY, float speed) {
    	this.initialDirection = new Vector2(dirX, dirY).nor();
    this.speed = speed;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
    	Vector2 vel = entity.getVelocity();

        // Initialize the Entity's velocity once using our direction and speed
        if (!isInitialized) {
            vel.set(initialDirection).scl(speed);
            isInitialized = true;
        }

        // Move the entity based on its CURRENT velocity
        // This velocity is what CollisionResolution flips during a bounce
        entity.move(vel.x * deltaTime, vel.y * deltaTime);
    }
    
    public void reverseDirection(Entity entity) {
        entity.getVelocity().scl(-1);
    }
}