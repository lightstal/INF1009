package io.github.INF1009_P10_Team7.engine.entity.components;

import io.github.INF1009_P10_Team7.engine.entity.Component;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;

/**
 * Component that handles entity movement using the Strategy pattern.
 * Allows switching between different movement behaviors at runtime.
 */
public class MovementComponent implements Component {
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
        if (movementBehaviour != null && owner != null) {
            movementBehaviour.move(owner, deltaTime);
        }
    }

    // Allows changing movement behavior at runtime
    public void setMovementBehaviour(MovementBehaviour movementBehaviour) {
        this.movementBehaviour = movementBehaviour;
    }

    public MovementBehaviour getMovementBehaviour() {
        return movementBehaviour;
    }
}