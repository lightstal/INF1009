package io.github.INF1009_P10_Team7.cyber;

import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.MovementHandler;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Translates WASD / arrow-key input into player velocity.
 * Implements MovementHandler so it plugs into the engine's
 * InputDrivenMovement + MovementManager pipeline without modification (OCP, DIP).
 */
public class CyberPlayerMovement implements MovementHandler {

    /** Player movement speed in world units per second. */
    private static final float SPEED = 120f;

    @Override
    public void handle(PhysicComponent physics, IInputController input) {
        Vector2 vel = physics.getVelocity();
        vel.set(0, 0);

        if (input.isActionPressed("LEFT"))  vel.x -= SPEED;
        if (input.isActionPressed("RIGHT")) vel.x += SPEED;
        if (input.isActionPressed("UP"))    vel.y += SPEED;
        if (input.isActionPressed("DOWN"))  vel.y -= SPEED;

        // Normalise diagonal movement to keep consistent speed
        float len = vel.len();
        if (len > SPEED) {
            vel.x = (vel.x / len) * SPEED;
            vel.y = (vel.y / len) * SPEED;
        }
    }
}
