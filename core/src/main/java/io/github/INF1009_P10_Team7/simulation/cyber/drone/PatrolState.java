package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * PatrolState: the drone follows a waypoint circuit.
 * At each waypoint it pauses for a randomized 5–20 seconds before
 * continuing, giving it a believable patrol feel.
 * Transitions to ChaseState when the player enters its sight cone.
 */
public class PatrolState implements DroneState {

    private final float[][] patrolTiles;
    private float[][] waypoints;
    private int waypointIndex = 0;
    private float detectTimer = 0f;
    private static final float DETECT_CONFIRM_TIME = 1.10f;

    // Stuck detection
    private float stuckTimer = 0f;
    private float lastX = 0f, lastY = 0f;
    private static final float STUCK_TIMEOUT = 0.35f;
    private static final float STUCK_THRESHOLD = 1.2f;

    /** Time the drone waits at a waypoint before moving to the next. */
    private float waypointPauseTimer = 0f;
    /** Minimum seconds to dwell at a waypoint. */
    private static final float PAUSE_MIN = 5f;
    /** Maximum seconds to dwell at a waypoint. */
    private static final float PAUSE_MAX = 20f;

    /** Whether the drone is currently dwelling at a waypoint. */
    private boolean dwelling = false;

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
        // Pick nearest waypoint
        float bestDist = Float.MAX_VALUE;
        for (int i = 0; i < waypoints.length; i++) {
            float dx = waypoints[i][0] - ai.getPosition().x;
            float dy = waypoints[i][1] - ai.getPosition().y;
            float d  = dx * dx + dy * dy;
            if (d < bestDist) { bestDist = d; waypointIndex = i; }
        }
        ai.setAlertLevel(0f);
        detectTimer = 0f;
        stuckTimer = 0f;
        lastX = ai.getPosition().x;
        lastY = ai.getPosition().y;
        dwelling = false;
        waypointPauseTimer = 0f;
    }

    @Override
    public void update(DroneAI ai, TileMap map, Vector2 playerPos, float dt) {
        Vector2 pos = ai.getPosition();

        float tx = waypoints[waypointIndex][0];
        float ty = waypoints[waypointIndex][1];
        float dx = tx - pos.x;
        float dy = ty - pos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dwelling) {
            // Standing at waypoint, slowly rotating to face next waypoint
            waypointPauseTimer -= dt;
            int nextIndex = (waypointIndex + 1) % waypoints.length;
            float ndx = waypoints[nextIndex][0] - pos.x;
            float ndy = waypoints[nextIndex][1] - pos.y;
            // Slow smooth rotation
            float targetAngle = (float) Math.toDegrees(Math.atan2(ndy, ndx));
            float currentAngle = ai.getFacingAngle();
            float diff = angleDiff(targetAngle, currentAngle);
            float rotSpeed = 30f; // degrees per second
            if (Math.abs(diff) > rotSpeed * dt) {
                currentAngle += Math.signum(diff) * rotSpeed * dt;
            } else {
                currentAngle = targetAngle;
            }
            ai.setFacingAngle(currentAngle);

            if (waypointPauseTimer <= 0f) {
                dwelling = false;
                waypointIndex = nextIndex;
                stuckTimer = 0f;
            }
        } else if (dist < 7f) {
            // Arrived at waypoint — start dwelling
            dwelling = true;
            waypointPauseTimer = PAUSE_MIN + (float)(Math.random() * (PAUSE_MAX - PAUSE_MIN));
        } else {
            // Moving toward current waypoint
            float speed = ai.getPatrolSpeed();

            // Try direct movement
            float nextX = pos.x + (dx / dist) * speed * dt;
            float nextY = pos.y + (dy / dist) * speed * dt;

            float[] resolved = map.resolveCircleVsWalls(nextX, nextY, ai.getRadius());
            float moved = Math.abs(resolved[0] - pos.x) + Math.abs(resolved[1] - pos.y);

            if (moved < 0.5f) {
                // Blocked by wall — try sliding along each axis independently
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
                    // Truly stuck — let stuck detection handle it
                    pos.x = resolved[0]; pos.y = resolved[1];
                }
            } else {
                pos.x = resolved[0];
                pos.y = resolved[1];
            }

            ai.setFacingAngle((float) Math.toDegrees(Math.atan2(dy, dx)));
        }

        // Stuck detection — if barely moved while not dwelling, skip waypoint
        if (!dwelling) {
            float moved = Math.abs(pos.x - lastX) + Math.abs(pos.y - lastY);
            if (moved < STUCK_THRESHOLD) {
                stuckTimer += dt;
                if (stuckTimer >= STUCK_TIMEOUT) {
                    waypointIndex = (waypointIndex + 1) % waypoints.length;
                    stuckTimer = 0f;
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

        float pdx = playerPos.x - pos.x;
        float pdy = playerPos.y - pos.y;
        float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
        float sightRange = ai.getSightRange();

        boolean seesPlayer = false;
        if (pDist < sightRange) {
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

    @Override
    public void exit(DroneAI ai) { /* nothing */ }

    @Override
    public String getName() { return "PATROL"; }

    private float angleDiff(float a, float b) {
        float d = a - b;
        while (d >  180f) d -= 360f;
        while (d < -180f) d += 360f;
        return d;
    }
}
