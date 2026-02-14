package io.github.INF1009_P10_Team7.Entity;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.INF1009_P10_Team7.InputOutput.iInputController;


public class EntityManager implements iEntitySystem {

	@Override
    public void updateEntities(List<Entity> active, List<Entity> pending, float deltaTime, iInputController input) {
        // 1. Move pending to active
        active.addAll(pending);
        pending.clear();

        // 2. Safely update and remove expired entities
        Iterator<Entity> iter = active.iterator();
        while (iter.hasNext()) {
            Entity e = iter.next();
            e.update(deltaTime, input);

            if (e.isExpired()) {
                iter.remove();
            }
        }
    }

    @Override
    public void renderEntities(List<Entity> active, SpriteBatch batch, ShapeRenderer shape) {
        batch.begin();
        for (Entity e : active) {
            e.render(batch);
        }
        batch.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity e : active) {
            e.render(shape);
        }
        shape.end();
    }
}