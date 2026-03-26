package io.github.INF1009_P10_Team7.engine.movement;

import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * IMovementSystem, public contract for the movement subsystem.
 *
 * <p>Scenes depend on this interface rather than the concrete
 * {@link MovementManager} (DIP). This allows movement behaviour to be
 * added, removed, queried, and swapped at runtime without coupling scene
 * code to implementation details.</p>
 *
 * <p>Key runtime operations exposed:</p>
 * <ul>
 * <li>{@link #setBehavior}, hot-swap a movement algorithm (Strategy Pattern, LSP)</li>
 * <li>{@link #getBehavior}, read current algorithm (used before a swap)</li>
 * <li>{@link #hasEntity} , guard check before operating on an entity</li>
 * </ul>
 */
public interface IMovementSystem {

    /**
     * Registers an entity with an initial movement behaviour.
     * If {@code behavior} is {@code null}, only physics integration is applied.
     *
     * @param entity the entity to register
     * @param behavior the initial movement behaviour, or {@code null}
     */
    void addEntity(Entity entity, MovementBehaviour behavior);

    /**
     * Unregisters an entity so it is no longer updated by this system.
     *
     * @param entity the entity to remove
     */
    void removeEntity(Entity entity);

    /**
     * Advances all registered, active entities by one frame:
     * first applies physics velocity integration, then calls each
     * entity's {@link MovementBehaviour#move}.
     *
     * @param deltaTime seconds since the last frame
     */
    void updateAll(float deltaTime);

    /** Removes all entities and clears all behaviour mappings. */
    void clear();

    /** @return the total number of entities currently registered */
    int getEntityCount();

    /**
     * Returns {@code true} if the entity is currently registered
     * with this system. Use as a guard before calling {@link #setBehavior}.
     *
     * @param entity the entity to check
     * @return {@code true} if managed by this system
     */
    boolean hasEntity(Entity entity);

    /**
     * Returns the {@link MovementBehaviour} currently assigned to the entity,
     * or {@code null} if the entity is not registered or has no behaviour.
     *
     * @param entity the entity whose behaviour to retrieve
     * @return current behaviour, or {@code null}
     */
    MovementBehaviour getBehavior(Entity entity);

    /**
     * Replaces the movement behaviour of a registered entity at runtime.
     * Used to implement runtime strategy swaps (e.g. follower switching from
     * {@link FollowMovement} to {@link AImovement}).
     *
     * @param entity the entity whose behaviour to replace
     * @param behavior the new behaviour to assign
     */
    void setBehavior(Entity entity, MovementBehaviour behavior);
}
