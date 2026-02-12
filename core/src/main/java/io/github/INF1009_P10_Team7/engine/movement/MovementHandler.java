package io.github.INF1009_P10_Team7.engine.movement;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

// interface for playermovement to provide key movement.
public interface MovementHandler{
    void handle(PhysicComponent physics, InputController input);
}
