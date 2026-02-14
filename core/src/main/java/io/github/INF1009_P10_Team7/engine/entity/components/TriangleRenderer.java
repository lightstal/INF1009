package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Renders an entity as a filled triangle.
 * Concrete implementation of the IRenderBehaviour Strategy.
 */
public class TriangleRenderer implements IRenderBehaviour {

    private final float size;

    public TriangleRenderer(float size) {
        this.size = size;
    }

    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc == null) return;

        Vector2 pos = tc.getPosition();
        shape.triangle(
            pos.x, pos.y + size,
            pos.x - size * 0.8f, pos.y - size * 0.6f,
            pos.x + size * 0.8f, pos.y - size * 0.6f
        );
    }
}
