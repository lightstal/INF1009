package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * MovementHandler, bridge between raw device input and physics velocity.
 *
 * <p>Implementations translate player key/button presses into a velocity
 * vector that is written directly to the entity's {@link PhysicComponent}.
 * This interface is used by {@link InputDrivenMovement} so the
 * "which keys map to which velocity" policy can be swapped without touching
 * the movement system (OCP / DIP).</p>
 *
 * <p>Example implementations:</p>
 * <ul>
 * <li>{@link io.github.INF1009_P10_Team7.simulation.PlayerMovement}
 * , WASD for the Part 1 demo scene</li>
 * <li>{@link io.github.INF1009_P10_Team7.cyber.CyberPlayerMovement}
 * , WASD with diagonal normalisation for Cyber Maze Escape</li>
 * </ul>
 */
public interface MovementHandler {

    /**
     * Reads the current input state and writes the resulting velocity into
     * {@code physics}. Typically sets the velocity to zero first then adds
     * the direction of each pressed key.
     *
     * @param physics the physics component whose velocity will be set
     * @param input the input controller to poll for key/button state
     */
    void handle(PhysicComponent physics, IInputController input);
}
