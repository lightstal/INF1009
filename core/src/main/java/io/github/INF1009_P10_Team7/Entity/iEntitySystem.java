package io.github.INF1009_P10_Team7.Entity;

import java.util.List;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.InputOutput.iInputController;

public interface iEntitySystem {
    void updateEntities(List<Entity> active, List<Entity> pending, float deltaTime, iInputController input);
    void renderEntities(List<Entity> active, SpriteBatch batch, ShapeRenderer shape);
}