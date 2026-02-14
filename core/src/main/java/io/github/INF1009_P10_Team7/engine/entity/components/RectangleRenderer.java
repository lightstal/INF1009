package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Renders an entity as a filled rectangle (optionally rotated to look like a diamond).
 * Concrete implementation of the IRenderBehaviour Strategy.
 */
public class RectangleRenderer implements IRenderBehaviour {

    private final float width;
    private final float height;

    public RectangleRenderer(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc == null) return;

        Vector2 pos = tc.getPosition();
        float rotation = tc.getRotation();

        shape.rect(
            pos.x - width / 2f, pos.y - height / 2f,
            width / 2f, height / 2f,
            width, height,
            1f, 1f,
            rotation
        );
    }
}
