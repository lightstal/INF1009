package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * Strategy interface for rendering entities.
 * Allows swapping render styles (circle, rectangle, triangle, diamond) at runtime.
 * Demonstrates the Strategy Pattern and Polymorphism.
 */
public interface IRenderBehaviour {
    void render(Entity entity, ShapeRenderer shape);
}
