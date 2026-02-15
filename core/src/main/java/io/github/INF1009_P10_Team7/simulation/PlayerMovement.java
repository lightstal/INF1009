package io.github.INF1009_P10_Team7.simulation;

import java.util.HashMap;

import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.MovementHandler;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import java.util.Map;

/**
 * <p>Handles player movement based on key input. Maps actions like
 * LEFT, RIGHT, UP, DOWN to velocity directions.</p>
 */
public class PlayerMovement implements MovementHandler {
    // Maps action names to their velocity directions
    private final Map<String, Vector2> moveDirections = new HashMap<>();

    public PlayerMovement() {
        moveDirections.put("LEFT", new Vector2(-200, 0));
        moveDirections.put("RIGHT", new Vector2(200, 0));
        moveDirections.put("UP", new Vector2(0, 200));
        moveDirections.put("DOWN", new Vector2(0, -200));
    }

    /**
     * <p>Sets the velocity based on which movement keys are pressed.
     * Resets velocity to zero first, then adds the direction of
     * each pressed key.</p>
     *
     * @param physics the physics component to update
     * @param input   the input controller to check for key presses
     */
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
