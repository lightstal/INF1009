package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

public interface MovementBehaviour {
    // Standard move method as per UML diagram
    void move(Entity entity, float deltaTime);
}