package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * MovementBehaviour, Strategy interface for entity movement.
 *
 * <p>Each concrete implementation encapsulates one movement algorithm
 * (e.g. {@link AImovement} for random wandering, {@link FollowMovement}
 * for target-following, {@link LinearMovement} for straight-line travel,
 * {@link InputDrivenMovement} for player control). The
 * {@link MovementManager} holds a behaviour per entity and calls
 * {@link #move} every frame.</p>
 *
 * <p>Design note (Strategy Pattern + LSP): all implementations are
 * interchangeable at runtime. Swapping a behaviour via
 * {@link MovementManager#setBehavior} does not require any changes to the
 * entity, the manager, or the scene, only the algorithm changes.</p>
 */
public interface MovementBehaviour {

    /**
     * Advances the entity's position for one frame.
     * Implementations read the entity's
     * {@link io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent}
     * and update it directly.
     *
     * @param entity the entity to move
     * @param deltaTime seconds elapsed since the last frame
     */
    void move(Entity entity, float deltaTime);
}
