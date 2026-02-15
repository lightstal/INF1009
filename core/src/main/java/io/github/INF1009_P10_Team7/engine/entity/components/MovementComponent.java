package io.github.INF1009_P10_Team7.engine.entity.components;

import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;

/**
 * Component that handles entity movement using the Strategy pattern.
 * Allows switching between different movement behaviors at runtime.
 */
public class MovementComponent implements IComponent {
    private MovementBehaviour movementBehaviour;
    private Entity owner;

    public MovementComponent(MovementBehaviour movementBehaviour) {
        this.movementBehaviour = movementBehaviour;
    }

    @Override
    public void onAdded(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void onRemoved(Entity owner) {
        this.owner = null;
    }

    @Override
    public void update(float deltaTime) {
        // Intentionally empty.
        // Movement is executed centrally by MovementManager to keep engine systems consistent
        // and avoid double-applying movement (component + manager).
    }

    // Allows changing movement behavior at runtime
    public void setMovementBehaviour(MovementBehaviour movementBehaviour) {
        this.movementBehaviour = movementBehaviour;
    }

    public MovementBehaviour getMovementBehaviour() {
        return movementBehaviour;
    }
}