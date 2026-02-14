package io.github.INF1009_P10_Team7.Collision;

import java.util.Map;

import io.github.INF1009_P10_Team7.Entity.Entity;

public interface iCollisionSystem {
	void process(Map<Entity, CollisionType> registry, float worldWidth, float worldHeight);
}
