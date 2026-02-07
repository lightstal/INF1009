package io.github.INF1009_P10_Team7.engine.entity.components;

import io.github.INF1009_P10_Team7.engine.entity.Component;
import io.github.INF1009_P10_Team7.engine.entity.Entity;

// Component that holds a reference to a sprite or texture for rendering purposes.
public class SpriteComponent implements Component {
    private Object spriteRef;
    private Entity owner;

    // Creates a SpriteComponent with no sprite reference.
    public SpriteComponent() {
        this.spriteRef = null;
    }

    // Creates a SpriteComponent with the given sprite reference.
    public SpriteComponent(Object spriteRef) {
        this.spriteRef = spriteRef;
    }

    @Override
    public void onAdded(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void onRemoved(Entity owner) {
        this.owner = null;
    }

    @Override
    public void update(float deltaTime) {
        // SpriteComponent typically doesn't update on its own
        // Rendering is handled by the rendering system
    }

    //  Gets the sprite reference.
    public Object getSpriteRef() {
        return spriteRef;
    }

    //  Sets the sprite reference.
    public void setSpriteRef(Object spriteRef) {
        this.spriteRef = spriteRef;
    }

//   Gets the owner entity.
    public Entity getOwner() {
        return owner;
    }
}
