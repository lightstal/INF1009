package io.github.INF1009_P10_Team7.cyber.drone;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.cyber.IMapCollision;

/**
 * SearchState — drone AI state for investigating a last-known player position.
 *
 * <p>After losing line of sight in {@link ChaseState}, the drone moves to the
 * last known player coordinates and sweeps the area for a short duration
 * before reverting to {@link PatrolState}.</p>
 *
 * <p>Implements {@link DroneState} (State Pattern).</p>
 *
 * <p>Transitions:</p>
 * <ul>
 *   <li>→ {@link ChaseState}  if the player re-enters the sight cone</li>
 *   <li>→ {@link PatrolState} when the search timer expires or the drone
 *       is stuck and cannot reach the target</li>
 * </ul>
 */
public class SearchState implements DroneState {
    private final float targetX;
    private final float targetY;
    private final float duration;
    private float timer;

    // Stuck detection — if drone barely moves for long enough, give up and patrol
    private float stuckTimer = 0f;
    private float lastX = 0f, lastY = 0f;
    private static final float STUCK_TIMEOUT   = 0.6f;
    private static final float STUCK_THRESHOLD = 1.0f;

    public SearchState(float targetX, float targetY) {
        this(targetX, targetY, 1.2f);
    }

    public SearchState(float targetX, float targetY, float duration) {
        this.targetX  = targetX;
        this.targetY  = targetY;
        this.duration = duration;
        this.timer    = duration;
    }

    @Override
    public void enter(DroneAI ai) {
        ai.setAlertLevel(0.55f);
        stuckTimer = 0f;
        lastX = ai.getPosition().x;
        lastY = ai.getPosition().y;
    }

    @Override
    public void update(DroneAI ai, IMapCollision map, Vector2 playerPos, float dt) {
        Vector2 pos = ai.getPosition();

        float dx = targetX - pos.x;
        float dy = targetY - pos.y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        if (dist > 6f) {
            float speed = ai.getPatrolSpeed() * 0.95f;
            float nextX = pos.x + (dx / dist) * speed * dt;
            float nextY = pos.y + (dy / dist) * speed * dt;
            float[] resolved = map.resolveCircleVsWalls(nextX, nextY, ai.getRadius());
            pos.x = resolved[0];
            pos.y = resolved[1];
            // Smooth turn toward last known position (150 deg/s max)
            float targetAngle = (float)Math.toDegrees(Math.atan2(dy, dx));
            float aDiff = angleDiff(targetAngle, ai.getFacingAngle());
            float maxTurn = 150f * dt;
            if (Math.abs(aDiff) > maxTurn) {
                ai.setFacingAngle(ai.getFacingAngle() + Math.signum(aDiff) * maxTurn);
            } else {
                ai.setFacingAngle(targetAngle);
            }
        } else {
            ai.setFacingAngle(ai.getFacingAngle() + 110f * dt);
        }

        float pdx = playerPos.x - pos.x;
        float pdy = playerPos.y - pos.y;
        float pDist = (float)Math.sqrt(pdx * pdx + pdy * pdy);

        boolean seesPlayer = false;
        if (pDist < ai.getSightRange()) {
            float angleToPlayer = (float)Math.toDegrees(Math.atan2(pdy, pdx));
            float dAngle = Math.abs(angleDiff(angleToPlayer, ai.getFacingAngle()));
            if (dAngle < ai.getSightAngle() / 2f) {
                seesPlayer = map.hasLineOfSight(pos.x, pos.y, playerPos.x, playerPos.y);
            }
        }

        if (seesPlayer) {
            ai.transitionTo(new ChaseState());
            return;
        }

        // Stuck detection: if the drone barely moved this frame, increment stuck timer
        float moved = Math.abs(pos.x - lastX) + Math.abs(pos.y - lastY);
        if (moved < STUCK_THRESHOLD) {
            stuckTimer += dt;
            if (stuckTimer >= STUCK_TIMEOUT) {
                // Can't reach target — give up and return to patrol
                ai.transitionTo(new PatrolState(ai.getPatrolWaypoints()));
                return;
            }
        } else {
            stuckTimer = 0f;
        }
        lastX = pos.x;
        lastY = pos.y;

        timer -= dt;
        ai.setAlertLevel(Math.max(0.2f, timer / duration * 0.55f));
        if (timer <= 0f) {
            ai.transitionTo(new PatrolState(ai.getPatrolWaypoints()));
        }
    }

    @Override
    public void exit(DroneAI ai) { }

    @Override
    public String getName() { return "SEARCH"; }

    private float angleDiff(float a, float b) {
        float d = a - b;
        while (d > 180f) d -= 360f;
        while (d < -180f) d += 360f;
        return d;
    }
}
