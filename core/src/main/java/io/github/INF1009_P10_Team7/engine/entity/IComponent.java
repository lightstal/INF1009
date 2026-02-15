package io.github.INF1009_P10_Team7.engine.entity;

// Interface for components that can be attached to entities.
public interface IComponent {

    // Called when this component is added to an entity.
    // Use this to initialize resources or references.
    void onAdded(Entity owner);

    // Called when this component is removed from an entity.
    // Use this to clean up resources or references.
    void onRemoved(Entity owner);

    // Called every frame to update the component's state.
    void update(float deltaTime);
}
