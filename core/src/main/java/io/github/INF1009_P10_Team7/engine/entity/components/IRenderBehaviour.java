package io.github.INF1009_P10_Team7.engine.entity.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.engine.entity.Entity;

/**
 * IRenderBehaviour, Strategy interface for entity rendering.
 *
 * <p>Concrete implementations ({@link CircleRenderer}, {@link RectangleRenderer},
 * {@link TriangleRenderer}) each draw the owning entity in a different shape.
 * The {@link RenderComponent} holds an instance of this interface and calls
 * {@link #render} each frame, allowing the visual representation of any entity
 * to be swapped at runtime without modifying entity or component code (OCP).</p>
 *
 * <p>Design note (Strategy Pattern): the algorithm for "how to draw this entity"
 * is encapsulated here and is interchangeable independently of the entity that
 * owns it.</p>
 */
public interface IRenderBehaviour {

    /**
     * Draws the given entity using the provided {@link ShapeRenderer}.
     * Implementations should read the entity's {@link TransformComponent}
     * for position and rotation.
     *
     * <p>The {@link ShapeRenderer} is already begun by the caller;
     * implementations must NOT call {@code begin()} or {@code end()}.</p>
     *
     * @param entity the entity to draw
     * @param shape the active ShapeRenderer to draw with
     */
    void render(Entity entity, ShapeRenderer shape);
}
