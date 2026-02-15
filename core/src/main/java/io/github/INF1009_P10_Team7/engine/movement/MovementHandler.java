package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * <p>
 * MovementHandler processes player input and applies movement to the physics component.
 * Different implementations can define different movement logic.
 * </p>
 */
public interface MovementHandler {

    /**
     * <p>
     * Handle movement based on input and update the physics component.
     * </p>
     *
     * @param physics the physics component of the entity
     * @param input the input controller that provides user input
     */
    void handle(PhysicComponent physics, IInputController input);
}
