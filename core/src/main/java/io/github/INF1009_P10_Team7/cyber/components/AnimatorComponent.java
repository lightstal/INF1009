package io.github.INF1009_P10_Team7.cyber.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.cyber.SpriteAnimator;

public class AnimatorComponent implements IComponent {
    private Entity owner;
    private SpriteAnimator animator;
    private float renderScale;

    public AnimatorComponent(SpriteAnimator animator, float renderScale) {
        this.animator = animator;
        this.renderScale = renderScale;
    }

    @Override
    public void onAdded(Entity entity) { this.owner = entity; }

    @Override
    public void onRemoved(Entity entity) { this.owner = null; }

    @Override
    public void update(float deltaTime) {
        if (owner != null && owner.hasComponent(PhysicComponent.class)) {
            PhysicComponent phys = owner.getComponent(PhysicComponent.class);
            float vx = phys.getVelocity().x;
            float vy = phys.getVelocity().y;
            animator.update(deltaTime, vx, vy);
        } else {
            animator.update(deltaTime, 0, 0);
        }
    }

    public void render(SpriteBatch batch, float x, float y) {
        animator.drawCentered(batch, x, y, renderScale);
    }

    public void dispose() {
        if (animator != null) {
            animator.dispose();
        }
    }
}