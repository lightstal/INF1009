package io.github.INF1009_P10_Team7.Entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DiamondRenderer implements iRenderBehaviour {
    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        float halfW = entity.getBounds().width / 2f;
        float halfH = entity.getBounds().height / 2f;

        float topX = entity.getPosition().x, topY = entity.getPosition().y + halfH;
        float rightX = entity.getPosition().x + halfW, rightY = entity.getPosition().y;
        float bottomX = entity.getPosition().x, bottomY = entity.getPosition().y - halfH;
        float leftX = entity.getPosition().x - halfW, leftY = entity.getPosition().y;

        shape.triangle(leftX, leftY, topX, topY, rightX, rightY);
        shape.triangle(leftX, leftY, bottomX, bottomY, rightX, rightY);
    }
}