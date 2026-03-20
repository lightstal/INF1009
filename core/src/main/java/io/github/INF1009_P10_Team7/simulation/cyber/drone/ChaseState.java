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
    private static final float CHASE_LEASH_MULTIPLIER = 1.8f;
    private static final float CHASE_ROT_DEG_S = 150f;

    @Override
    public void enter(DroneAI ai) {
        ai.setAlertLevel(1f);
        lostSightTimer = 0f;
    }

    @Override
    public void update(DroneAI ai, TileMap map, Vector2 playerPos, float dt) {
        if (ai.isDetectionSuppressed()) {
            ai.transitionTo(new PatrolState(ai.getPatrolWaypoints()));
            return;
        }

        Vector2 pos = ai.getPosition();

        float dx = playerPos.x - pos.x;
        float dy = playerPos.y - pos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.001f) return;

        float dirX = dx / dist;
        float dirY = dy / dist;
        float targetAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        float angleDiff = targetAngle - ai.getFacingAngle();
        while (angleDiff >  180f) angleDiff -= 360f;
        while (angleDiff < -180f) angleDiff += 360f;
        if (Math.abs(angleDiff) > CHASE_ROT_DEG_S * dt) {
            ai.setFacingAngle(ai.getFacingAngle() + Math.signum(angleDiff) * CHASE_ROT_DEG_S * dt);
        } else {
            ai.setFacingAngle(targetAngle);
        }

        float speed = ai.getChaseSpeed();
        float nextX = pos.x + dirX * speed * dt;
        float nextY = pos.y + dirY * speed * dt;

        float[] resolved = map.resolveCircleVsWalls(nextX, nextY, ai.getRadius());
        float moved = Math.abs(resolved[0] - pos.x) + Math.abs(resolved[1] - pos.y);
        float expectedMove = speed * dt;
        float blockThreshold = expectedMove * 0.25f;

        if (moved < blockThreshold) {
            // Truly blocked by a wall — try sidestep
            float sideX = -dirY;
            float sideY = dirX;

            float[] sideA = map.resolveCircleVsWalls(
                pos.x + sideX * speed * 0.82f * dt,
                pos.y + sideY * speed * 0.82f * dt,
                ai.getRadius());
            float movedA = Math.abs(sideA[0] - pos.x) + Math.abs(sideA[1] - pos.y);

            float[] sideB = map.resolveCircleVsWalls(
                pos.x - sideX * speed * 0.82f * dt,
                pos.y - sideY * speed * 0.82f * dt,
                ai.getRadius());
            float movedB = Math.abs(sideB[0] - pos.x) + Math.abs(sideB[1] - pos.y);

            if (movedA >= movedB && movedA > moved) {
                pos.x = sideA[0]; pos.y = sideA[1];
            } else if (movedB > moved) {
                pos.x = sideB[0]; pos.y = sideB[1];
            } else {
                lostSightTimer += dt;
            }
        } else {
            pos.x = resolved[0];
            pos.y = resolved[1];
        }

        boolean canSee = map.hasLineOfSight(pos.x, pos.y, playerPos.x, playerPos.y)
                         && dist < ai.getSightRange() * CHASE_LEASH_MULTIPLIER;

        if (!canSee || dist > ai.getSightRange() * CHASE_LEASH_MULTIPLIER) {
            lostSightTimer += dt;
            ai.setAlertLevel(Math.max(0f, 1f - (lostSightTimer / LOST_TIMEOUT)));
            if (lostSightTimer >= LOST_TIMEOUT) {
                ai.transitionTo(new SearchState(playerPos.x, playerPos.y));
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
