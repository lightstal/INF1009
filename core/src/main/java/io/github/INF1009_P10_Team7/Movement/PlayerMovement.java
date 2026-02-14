package io.github.INF1009_P10_Team7.Movement;

import io.github.INF1009_P10_Team7.Entity.Entity;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;

public class PlayerMovement implements iMovementBehaviour {
    
    private float speed;
    private iInputController input;

    public PlayerMovement(float speed, iInputController input) {
        this.speed = speed;
        this.input = input;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        // Apply movement directly to the entity based on input
        if (input.isActionPressed("UP")) entity.move(0, speed * deltaTime);
        if (input.isActionPressed("DOWN")) entity.move(0, -speed * deltaTime);
        if (input.isActionPressed("LEFT")) entity.move(-speed * deltaTime, 0);
        if (input.isActionPressed("RIGHT")) entity.move(speed * deltaTime, 0);
    }
}