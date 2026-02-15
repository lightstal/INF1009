package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;

/**
 * <p>
 * InputDrivenMovement is a movement behaviour that uses player input.
 * It connects the input system with the movement system.
 * Instead of moving directly, it lets MovementHandler process the input.
 * </p>
 */
public class InputDrivenMovement implements MovementBehaviour {
    /** Handles how movement is processed based on input */
    private final MovementHandler handler;

    /** Reads the user input (keyboard, controller, etc.) */
    private final IInputController inputController;

    /**
     * <p>
     * Constructor to set the movement handler and input controller.
     * Both cannot be null.
     * </p>
     *
     * @param handler movement handler that processes input
     * @param inputController controller that provides input data
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

    /**
     * <p>
     * Move the entity based on player input.
     * If entity is inactive or null, nothing will happen.
     * Movement logic is delegated to the MovementHandler.
     * </p>
     *
     * @param entity the entity to move
     * @param deltaTime time passed since last frame
     */
    @Override
    public void move(Entity entity, float deltaTime) {
        if (entity == null || !entity.isActive()) {
            return;
        }

        // get physics component of the entity
        PhysicComponent physics = entity.getComponent(PhysicComponent.class);
        
        if (physics != null) {
            // let the handler process input and update movement
            handler.handle(physics, inputController);
        }
    }
}
