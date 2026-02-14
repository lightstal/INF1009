package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Renders an entity as a filled circle.
 * Concrete implementation of the IRenderBehaviour Strategy.
 */
public class CircleRenderer implements IRenderBehaviour {

    private final float radius;

    public CircleRenderer(float radius) {
        this.radius = radius;
    }

    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc == null) return;

        Vector2 pos = tc.getPosition();
        shape.circle(pos.x, pos.y, radius);
    }
}
