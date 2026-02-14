package io.github.INF1009_P10_Team7.Movement;

import java.util.Map;

import io.github.INF1009_P10_Team7.Entity.Entity;

public interface iMovementSystem {
	void process(Map<Entity, iMovementBehaviour> registry, float deltaTime);
}
