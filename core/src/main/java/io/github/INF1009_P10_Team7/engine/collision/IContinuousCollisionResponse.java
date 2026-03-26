package io.github.INF1009_P10_Team7.engine.collision;

/**
 * Marker interface for collision responses that must run continuously while two
 * objects remain overlapping (not just on initial overlap).
 *
 * <p>Useful for "trigger volume" style interactions (e.g., proximity prompts)
 * where game logic needs the overlap state every frame.</p>
 */
public interface IContinuousCollisionResponse extends ICollisionResponse {
    // No additional methods.
}

