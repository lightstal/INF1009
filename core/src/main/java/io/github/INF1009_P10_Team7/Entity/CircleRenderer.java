package io.github.INF1009_P10_Team7.Entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CircleRenderer implements iRenderBehaviour {
    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        shape.circle(entity.getPosition().x, entity.getPosition().y, entity.getBounds().width / 2f);
    }
}