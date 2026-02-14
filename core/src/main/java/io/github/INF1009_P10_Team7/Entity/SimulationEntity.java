package io.github.INF1009_P10_Team7.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.Collision.iCollidable;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;

public class SimulationEntity extends Entity {
    

    private Color color;
    private iRenderBehaviour renderBehaviour;

    public SimulationEntity(float x, float y, float width, float height, Color color, iRenderBehaviour renderBehaviour) {
        super(x, y, width, height);
        this.color = color;
        this.renderBehaviour = renderBehaviour;
    }

    @Override
    public void update(float deltaTime, iInputController input) {
    }

    @Override
    public void render(ShapeRenderer shape) {
        shape.setColor(color);
        if (renderBehaviour != null) {
            renderBehaviour.render(this, shape);
        }
    }
    
    @Override
    public void onCollision(iCollidable other) {
    }
}