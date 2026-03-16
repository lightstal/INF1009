package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * ChaseState: the drone actively pursues the player at full speed.
 * Alert level rises to 1.0 on entry.
 * Returns to PatrolState when line of sight is lost for > 2.5 seconds.
 * Implements DroneState (State Pattern).
 */
public class ChaseState implements DroneState {

    private float lostSightTimer = 0f;
    private static final float LOST_TIMEOUT = 2.5f;

    @Override
    public void enter(DroneAI ai) {
        ai.setAlertLevel(1f);
        lostSightTimer = 0f;
    }

    @Override
    public void update(DroneAI ai, TileMap map, Vector2 playerPos, float dt) {
        Vector2 pos = ai.getPosition();

        // Always face the player
        float dx = playerPos.x - pos.x;
        float dy = playerPos.y - pos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 4f) {
            float speed = ai.getChaseSpeed();
            pos.x += (dx / dist) * speed * dt;
            pos.y += (dy / dist) * speed * dt;
            ai.setFacingAngle((float) Math.toDegrees(Math.atan2(dy, dx)));
        }

        float[] resolved = map.resolveCircleVsWalls(pos.x, pos.y, ai.getRadius());
        pos.x = resolved[0];
        pos.y = resolved[1];

        // Check if we still have line of sight
        boolean canSee = map.hasLineOfSight(pos.x, pos.y, playerPos.x, playerPos.y)
                         && dist < ai.getSightRange() * 1.4f;

        if (!canSee) {
            lostSightTimer += dt;
            // Alert level decays while searching
            ai.setAlertLevel(Math.max(0f, 1f - (lostSightTimer / LOST_TIMEOUT) * 0.5f));
            if (lostSightTimer >= LOST_TIMEOUT) {
                ai.transitionTo(new PatrolState());
            }
        } else {
            lostSightTimer = 0f;
            ai.setAlertLevel(1f);
        }
    }

    @Override
    public void exit(DroneAI ai) { ai.setAlertLevel(0f); }

    @Override
    public String getName() { return "CHASE"; }
}
