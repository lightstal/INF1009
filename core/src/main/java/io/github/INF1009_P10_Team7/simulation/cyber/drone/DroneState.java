package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * State Pattern interface for the Hunter Drone AI.
 * Each concrete state encapsulates its own movement logic and transition conditions.
 * The DroneAI context holds the current state and delegates update calls to it.
 */
public interface DroneState {
    /** Called once when this state becomes active. */
    void enter(DroneAI ai);
    /** Called every frame while this state is active. */
    void update(DroneAI ai, TileMap map, Vector2 playerPos, float dt);
    /** Called once when transitioning away from this state. */
    void exit(DroneAI ai);
    /** Display name for HUD / debug. */
    String getName();
}
