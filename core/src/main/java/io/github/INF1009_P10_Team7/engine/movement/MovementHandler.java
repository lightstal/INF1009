package io.github.INF1009_P10_Team7.engine.movement;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

public interface MovementHandler{
    void handle(PhysicComponent physics, InputController input);
}
