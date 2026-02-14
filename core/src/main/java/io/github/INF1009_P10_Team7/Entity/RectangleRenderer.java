package io.github.INF1009_P10_Team7.Entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class RectangleRenderer implements iRenderBehaviour {
    @Override
    public void render(Entity entity, ShapeRenderer shape) {
        float halfW = entity.getBounds().width / 2f;
        float halfH = entity.getBounds().height / 2f;
        
        shape.rect(entity.getPosition().x - halfW, entity.getPosition().y - halfH, 
                   entity.getBounds().width, entity.getBounds().height);
    }
}