package io.github.INF1009_P10_Team7.cyber.drone;

import io.github.INF1009_P10_Team7.cyber.IMapCollision;
import io.github.INF1009_P10_Team7.cyber.TileMap;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * PatrolState: the drone follows a waypoint circuit.
 *
 * FIX (Issue 3) - Randomised movement so drones never look like they are
 * going in predictable circles:
 *
 *  1. RANDOM DWELL TIME   - each waypoint pause is independently randomised
 *     between PAUSE_MIN and PAUSE_MAX so no two stops feel the same.
 *
 *  2. RANDOM SPEED VARIATION - while travelling between waypoints the drone's
 *     effective speed is perturbed by +/-SPEED_JITTER every SPEED_CHANGE
 *     seconds, so consecutive legs feel different in pace.
 *
 *  3. RANDOM LOOK-AROUND - while dwelling the drone occasionally snaps to a
 *     random "glance angle" for a short random hold before resuming its sweep.
 *     This breaks the robotic constant-rotation feel.
 *
 *  4. WAYPOINT DIRECTION SHUFFLE - on entry there is a 20% chance the circuit
 *     is reversed, so two drones with identical waypoints can orbit in
 *     opposite directions and desynchronise naturally.
 *
 * Transitions to ChaseState when the player enters its sight cone.
 */
public class PatrolState implements DroneState {

    private final float[][] patrolTiles;
    private float[][] waypoints;
    private int       waypointIndex = 0;
    private boolean   reverseOrder  = false;

    private float detectTimer = 0f;
    private static final float DETECT_CONFIRM_TIME = 1.10f;

    // Stuck detection
    private float stuckTimer = 0f;
    private float lastX = 0f, lastY = 0f;
    private static final float STUCK_TIMEOUT   = 0.35f;
    private static final float STUCK_THRESHOLD = 1.2f;

    // Waypoint dwell - Fix 1
    private float waypointPauseTimer = 0f;
    private static final float PAUSE_MIN = 3f;
    private static final float PAUSE_MAX = 14f;
    private boolean dwelling = false;

    // Speed variation - Fix 2
    private float speedMult        = 1.0f;
    private float speedChangeTimer = 0f;
    private static final float SPEED_CHANGE = 2.8f;
    private static final float SPEED_JITTER = 0.28f;

    // Look-around during dwell - Fix 3
    private float   glanceAngle       = 0f;
    private boolean useGlance         = false;
    private float   glanceHoldTimer   = 0f;
    private float   glanceWaitTimer   = 0f;
    private static final float GLANCE_HOLD_MIN = 0.6f;
    private static final float GLANCE_HOLD_MAX = 2.2f;
    private static final float GLANCE_WAIT_MIN = 0.5f;
    private static final float GLANCE_WAIT_MAX = 3.5f;
    private static final float GLANCE_MAX_DEG  = 55f;

    private static final float ROT_SWEEP_DEG_S = 30f;
    private static final float ROT_SNAP_DEG_S  = 80f;
    private static final float ROT_MOVE_DEG_S  = 120f;

    public PatrolState(float[][] patrolTiles) {
        if (patrolTiles != null && patrolTiles.length > 0) {
            this.patrolTiles = patrolTiles;
        } else {
            this.patrolTiles = new float[][]{ {5,5}, {10,5}, {10,10}, {5,10} };
        }
    }

    public PatrolState() { this(null); }

    @Override
    public void enter(DroneAI ai) {
        waypoints = new float[patrolTiles.length][2];
        for (int i = 0; i < patrolTiles.length; i++) {
            waypoints[i][0] = TileMap.tileCentreX((int) patrolTiles[i][0]);
            waypoints[i][1] = TileMap.tileCentreY((int) patrolTiles[i][1]);
        }

        // Fix 4: 20% chance to reverse circuit so two identical-waypoint drones
        // orbit in opposite directions
        reverseOrder = (Math.random() < 0.20);

        // Pick nearest waypoint as start
        float bestDist = Float.MAX_VALUE;
        for (int i = 0; i < waypoints.length; i++) {
            float dx = waypoints[i][0] - ai.getPosition().x;
            float dy = waypoints[i][1] - ai.getPosition().y;
            float d  = dx * dx + dy * dy;
            if (d < bestDist) { bestDist = d; waypointIndex = i; }
        }

        ai.setAlertLevel(0f);
        detectTimer      = 0f;
        stuckTimer       = 0f;
        lastX            = ai.getPosition().x;
        lastY            = ai.getPosition().y;
        dwelling         = false;
        waypointPauseTimer = 0f;
        speedMult        = 1.0f;
        // Stagger speed-change timer per drone so they desync naturally
        speedChangeTimer = randomBetween(0f, SPEED_CHANGE);
        resetGlanceWait();
    }

    @Override
    public void update(DroneAI ai, IMapCollision map, Vector2 playerPos, float dt) {
        Vector2 pos = ai.getPosition();

        // Fix 2: periodic speed perturbation
        speedChangeTimer -= dt;
        if (speedChangeTimer <= 0f) {
            speedMult        = 1.0f + (float)(Math.random() * 2.0 - 1.0) * SPEED_JITTER;
            speedMult        = Math.max(0.72f, Math.min(1.28f, speedMult));
            speedChangeTimer = randomBetween(SPEED_CHANGE * 0.6f, SPEED_CHANGE * 1.4f);
        }

        float tx   = waypoints[waypointIndex][0];
        float ty   = waypoints[waypointIndex][1];
        float dx   = tx - pos.x;
        float dy   = ty - pos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dwelling) {
            updateDwelling(ai, dt);
        } else if (dist < 7f) {
            // Arrived - begin dwelling with fresh random pause
            dwelling           = true;
            waypointPauseTimer = randomBetween(PAUSE_MIN, PAUSE_MAX);
            resetGlanceWait();
        } else {
            // Moving toward current waypoint
            float speed = ai.getPatrolSpeed() * speedMult;
            float nextX = pos.x + (dx / dist) * speed * dt;
            float nextY = pos.y + (dy / dist) * speed * dt;

            float[] resolved = map.resolveCircleVsWalls(nextX, nextY, ai.getRadius());
            float moved = Math.abs(resolved[0] - pos.x) + Math.abs(resolved[1] - pos.y);

            if (moved < 0.5f) {
                float[] slideX = map.resolveCircleVsWalls(
                    pos.x + (dx / dist) * speed * dt, pos.y, ai.getRadius());
                float[] slideY = map.resolveCircleVsWalls(
                    pos.x, pos.y + (dy / dist) * speed * dt, ai.getRadius());
                float movedX = Math.abs(slideX[0] - pos.x) + Math.abs(slideX[1] - pos.y);
                float movedY = Math.abs(slideY[0] - pos.x) + Math.abs(slideY[1] - pos.y);

                if (movedX > movedY && movedX > moved) {
                    pos.x = slideX[0]; pos.y = slideX[1];
                } else if (movedY > moved) {
                    pos.x = slideY[0]; pos.y = slideY[1];
                } else {
                    pos.x = resolved[0]; pos.y = resolved[1];
                }
            } else {
                pos.x = resolved[0];
                pos.y = resolved[1];
            }

            // Smooth turn toward waypoint
            float targetAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
            float diff = angleDiff(targetAngle, ai.getFacingAngle());
            if (Math.abs(diff) > ROT_MOVE_DEG_S * dt) {
                ai.setFacingAngle(ai.getFacingAngle() + Math.signum(diff) * ROT_MOVE_DEG_S * dt);
            } else {
                ai.setFacingAngle(targetAngle);
            }
        }

        // Stuck detection
        if (!dwelling) {
            float moved = Math.abs(pos.x - lastX) + Math.abs(pos.y - lastY);
            if (moved < STUCK_THRESHOLD) {
                stuckTimer += dt;
                if (stuckTimer >= STUCK_TIMEOUT) {
                    waypointIndex = nextWaypointIndex();
                    stuckTimer    = 0f;
                }
            } else {
                stuckTimer = 0f;
            }
        }
        lastX = pos.x;
        lastY = pos.y;

        // Sight check
        if (ai.isDetectionSuppressed()) {
            detectTimer = 0f;
            ai.setAlertLevel(0f);
            return;
        }

        float pdx   = playerPos.x - pos.x;
        float pdy   = playerPos.y - pos.y;
        float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);

        boolean seesPlayer = false;
        if (pDist < ai.getSightRange()) {
            float angleToPlayer = (float) Math.toDegrees(Math.atan2(pdy, pdx));
            float dAngle = Math.abs(angleDiff(angleToPlayer, ai.getFacingAngle()));
            if (dAngle < ai.getSightAngle() / 2f) {
                seesPlayer = map.hasLineOfSight(pos.x, pos.y, playerPos.x, playerPos.y);
            }
        }

        if (seesPlayer) {
            detectTimer = Math.min(DETECT_CONFIRM_TIME, detectTimer + dt);
            ai.setAlertLevel(Math.min(0.78f, detectTimer / DETECT_CONFIRM_TIME));
            if (detectTimer >= DETECT_CONFIRM_TIME) {
                ai.transitionTo(new ChaseState());
            }
        } else {
            detectTimer = Math.max(0f, detectTimer - dt * 1.5f);
            ai.setAlertLevel(Math.max(0f, detectTimer / DETECT_CONFIRM_TIME * 0.5f));
        }
    }

    /**
     * Fix 3: Randomised look-around while dwelling.
     *
     * The drone sweeps toward the next waypoint direction but periodically
     * interrupts that sweep with a random glance at a nearby angle. Each
     * glance lasts a short random duration before the sweep resumes.
     */
    private void updateDwelling(DroneAI ai, float dt) {
        waypointPauseTimer -= dt;

        int nextIndex = nextWaypointIndex();
        Vector2 pos   = ai.getPosition();
        float ndx     = waypoints[nextIndex][0] - pos.x;
        float ndy     = waypoints[nextIndex][1] - pos.y;
        float baseAngle = (float) Math.toDegrees(Math.atan2(ndy, ndx));

        // Schedule / expire glance events
        if (!useGlance) {
            glanceWaitTimer -= dt;
            if (glanceWaitTimer <= 0f) {
                useGlance       = true;
                glanceAngle     = baseAngle + randomBetween(-GLANCE_MAX_DEG, GLANCE_MAX_DEG);
                glanceHoldTimer = randomBetween(GLANCE_HOLD_MIN, GLANCE_HOLD_MAX);
            }
        } else {
            glanceHoldTimer -= dt;
            if (glanceHoldTimer <= 0f) {
                useGlance = false;
                resetGlanceWait();
            }
        }

        float targetAngle  = useGlance ? glanceAngle : baseAngle;
        float currentAngle = ai.getFacingAngle();
        float diff         = angleDiff(targetAngle, currentAngle);
        // Faster snap when returning from glance, slower sweep otherwise
        float rotSpeed = useGlance ? ROT_SNAP_DEG_S : ROT_SWEEP_DEG_S;
        if (Math.abs(diff) > rotSpeed * dt) {
            currentAngle += Math.signum(diff) * rotSpeed * dt;
        } else {
            currentAngle = targetAngle;
        }
        ai.setFacingAngle(currentAngle);

        if (waypointPauseTimer <= 0f) {
            dwelling      = false;
            waypointIndex = nextIndex;
            stuckTimer    = 0f;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private int nextWaypointIndex() {
        if (reverseOrder) {
            return (waypointIndex - 1 + waypoints.length) % waypoints.length;
        }
        return (waypointIndex + 1) % waypoints.length;
    }

    private void resetGlanceWait() {
        glanceWaitTimer = randomBetween(GLANCE_WAIT_MIN, GLANCE_WAIT_MAX);
        useGlance       = false;
    }

    private float randomBetween(float min, float max) {
        return min + (float)(Math.random() * (max - min));
    }

    private float angleDiff(float a, float b) {
        float d = a - b;
        while (d >  180f) d -= 360f;
        while (d < -180f) d += 360f;
        return d;
    }

    @Override public void exit(DroneAI ai) { /* nothing */ }
    @Override public String getName()      { return "PATROL"; }
}
