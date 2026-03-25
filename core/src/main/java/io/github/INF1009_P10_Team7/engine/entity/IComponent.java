package io.github.INF1009_P10_Team7.engine.entity;

/**
 * IComponent — marker/lifecycle interface for all entity components.
 *
 * <p>Every component that can be attached to an {@link Entity} must implement
 * this interface. The engine calls {@link #onAdded} and {@link #onRemoved}
 * when the component is wired to or detached from an entity, and calls
 * {@link #update} once per frame while the entity is active.</p>
 *
 * <p>Design note (SRP / ISP): Components are data + behaviour bundles.
 * Each component should be responsible for exactly one concern
 * (e.g. transform, physics, rendering).</p>
 */
public interface IComponent {

    /**
     * Called by {@link Entity#addComponent} immediately after this component
     * has been registered on the given entity.
     *
     * @param owner the entity this component now belongs to
     */
    void onAdded(Entity owner);

    /**
     * Called by {@link Entity#removeComponent} just before this component
     * is unregistered from the given entity.
     * Use this to release any resources or back-references.
     *
     * @param owner the entity this component is being removed from
     */
    void onRemoved(Entity owner);

    /**
     * Called once per frame by {@link Entity#update} while the owning entity
     * is active. Components that manage per-frame state should update here.
     *
     * @param deltaTime seconds elapsed since the last frame
     */
    void update(float deltaTime);
}
