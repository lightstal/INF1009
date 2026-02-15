package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * InputDrivenMovement - A MovementBehaviour that uses MovementHandler for input-based control.
 * 
 * This class bridges the gap between the input system (PlayerMovement/MovementHandler) 
 * and the movement system (MovementManager/MovementBehaviour).
 * 
 * SOLID Principles:
 * - SRP: Focused solely on translating input into movement via delegation
 * - OCP: Extensible through different MovementHandler implementations
 * - LSP: Implements MovementBehaviour, fully substitutable
 * - ISP: Depends on narrow interfaces (MovementHandler, IInputController)
 * - DIP: Depends on abstractions (MovementHandler, IInputController), not concrete classes
 */
public class InputDrivenMovement implements MovementBehaviour {
    private final MovementHandler handler;
    private final IInputController inputController;

    /**
     * Constructor for InputDrivenMovement
     * @param handler The movement handler that processes input
     * @param inputController The input controller to read input from
     */
    public InputDrivenMovement(MovementHandler handler, IInputController inputController) {
        if (handler == null) {
            throw new IllegalArgumentException("MovementHandler cannot be null");
        }
        if (inputController == null) {
            throw new IllegalArgumentException("IInputController cannot be null");
        }
        this.handler = handler;
        this.inputController = inputController;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        if (entity == null || !entity.isActive()) {
            return;
        }

        // Get the physics component
        PhysicComponent physics = entity.getComponent(PhysicComponent.class);
        
        if (physics != null) {
            // Delegate input handling to the MovementHandler
            handler.handle(physics, inputController);
        }
    }
}