package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * PatrolState: the drone follows a fixed waypoint circuit.
 * Transitions to ChaseState when the player enters its sight cone.
 * Implements DroneState (State Pattern).
 */
public class PatrolState implements DroneState {

    // Waypoints are tile-centre world coordinates
    private static final float[][] PATROL_TILES = {
        {5, 1}, {23, 1}, {23, 7}, {12, 7}, {12, 13}, {1, 13}, {1, 7}, {5, 7}
    };

    private float[][] waypoints;
    private int waypointIndex = 0;

    // Stuck detection
    private float stuckTimer = 0f;
    private float lastX = 0f, lastY = 0f;
    private static final float STUCK_TIMEOUT = 0.75f;
    private static final float STUCK_THRESHOLD = 1.5f;

    @Override
    public void enter(DroneAI ai) {
        waypoints = new float[PATROL_TILES.length][2];
        for (int i = 0; i < PATROL_TILES.length; i++) {
            waypoints[i][0] = TileMap.tileCentreX((int) PATROL_TILES[i][0]);
            waypoints[i][1] = TileMap.tileCentreY((int) PATROL_TILES[i][1]);
        }
        ai.setAlertLevel(0f);
        stuckTimer = 0f;
        lastX = ai.getPosition().x;
        lastY = ai.getPosition().y;
    }

    @Override
    public void update(DroneAI ai, TileMap map, Vector2 playerPos, float dt) {
        Vector2 pos = ai.getPosition();

        // Move toward current waypoint
        float tx = waypoints[waypointIndex][0];
        float ty = waypoints[waypointIndex][1];
        float dx = tx - pos.x;
        float dy = ty - pos.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 6f) {
            waypointIndex = (waypointIndex + 1) % waypoints.length;
            stuckTimer = 0f;
        } else {
            float speed = ai.getPatrolSpeed();
            pos.x += (dx / dist) * speed * dt;
            pos.y += (dy / dist) * speed * dt;
            ai.setFacingAngle((float) Math.toDegrees(Math.atan2(dy, dx)));
        }

        // Wall resolution for drone
        float[] resolved = map.resolveCircleVsWalls(pos.x, pos.y, ai.getRadius());
        pos.x = resolved[0];
        pos.y = resolved[1];

        // Stuck detection — if barely moved, skip to next waypoint
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
        lastX = pos.x;
        lastY = pos.y;

        // Sight check: distance + line of sight
        float pdx = playerPos.x - pos.x;
        float pdy = playerPos.y - pos.y;
        float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
        float sightRange = ai.getSightRange();

        if (pDist < sightRange) {
            // Check angle within sight cone
            float angleToPlayer = (float) Math.toDegrees(Math.atan2(pdy, pdx));
            float dAngle = Math.abs(angleDiff(angleToPlayer, ai.getFacingAngle()));
            if (dAngle < ai.getSightAngle() / 2f) {
                if (map.hasLineOfSight(pos.x, pos.y, playerPos.x, playerPos.y)) {
                    ai.transitionTo(new ChaseState());
                }
            }
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
