package io.github.INF1009_P10_Team7.engine.movement;

import java.util.HashMap;

import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import java.util.Map;


// Create following with SRP and OCP principle.
public class PlayerMovement implements MovementHandler {
    // This map the keys and vector to their position
    private final Map<String, Vector2> moveDirections = new HashMap<>();

    public PlayerMovement() {
        // To call
        moveDirections.put("LEFT", new Vector2(-200, 0));
        moveDirections.put("RIGHT", new Vector2(200, 0));
        moveDirections.put("UP", new Vector2(0, 200));
        moveDirections.put("DOWN", new Vector2(0, -200));
    }

    @Override
    public void handle(PhysicComponent physics, IInputController input) {
        Vector2 velocity = physics.getVelocity();
        velocity.set(0, 0); // Dispose

        // When action is pressed, then move
        for (String action : moveDirections.keySet()) {
            if (input.isActionPressed(action)) {
                velocity.add(moveDirections.get(action));
            }
        }
    }
}
