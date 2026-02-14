package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Component;
import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * Component that holds a render behaviour (Strategy Pattern) and a color.
 * Allows swapping the visual representation of an entity at runtime.
 */
public class RenderComponent implements Component {

    private IRenderBehaviour renderBehaviour;
    private Color color;
    private Entity owner;

    public RenderComponent(IRenderBehaviour renderBehaviour, Color color) {
        this.renderBehaviour = renderBehaviour;
        this.color = new Color(color);
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
        // Rendering is handled externally by the scene's render loop
    }

    /**
     * Renders this entity using the assigned behaviour and color.
     */
    public void render(ShapeRenderer shape) {
        if (owner == null || renderBehaviour == null) return;
        shape.setColor(color);
        renderBehaviour.render(owner, shape);
    }

    // --- Getters / Setters ---

    public IRenderBehaviour getRenderBehaviour() {
        return renderBehaviour;
    }

    public void setRenderBehaviour(IRenderBehaviour renderBehaviour) {
        this.renderBehaviour = renderBehaviour;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }
}
