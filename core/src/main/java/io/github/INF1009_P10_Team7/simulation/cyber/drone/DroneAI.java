package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.simulation.cyber.IMapCollision;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * DroneAI  -  Context class for the State Pattern.
 * Delegates all behaviour to the current DroneState.
 *
 * Now accepts per-drone waypoints, speed and sight configuration so each
 * level can tune drone difficulty independently (Bug-4 fix, Improvement-6).
 */
public class DroneAI {

    private final Vector2 position;
    private final float spawnX;
    private final float spawnY;
    private final float initialFacingAngle;
    private float facingAngle = 0f;
    private final float radius;

    private final float patrolSpeed;
    private final float chaseSpeed;
    private final float sightRange;
    private final float sightAngle;

    private final float[][] patrolWaypoints;

    private DroneState currentState;
    private float alertLevel = 0f;
    private float stateTime  = 0f;
    private float rotorAngle = 0f;
    private float detectionSuppressedTimer = 0f;

    public DroneAI(float startX, float startY, float[][] waypoints,
                   float patrolSpeed, float chaseSpeed,
                   float sightRange, float sightAngle) {
        this.position        = new Vector2(startX, startY);
        this.spawnX          = startX;
        this.spawnY          = startY;
        this.patrolWaypoints = waypoints;
        this.patrolSpeed     = patrolSpeed;
        this.chaseSpeed      = chaseSpeed;
        this.sightRange      = sightRange;
        this.sightAngle      = sightAngle;
        this.radius          = 14f;
        // Compute initial facing angle toward first waypoint (or default to 0)
        if (waypoints != null && waypoints.length > 0) {
            float wpX = waypoints[0][0] * 16f + 8f; // approx tile centre
            float wpY = waypoints[0][1] * 16f + 8f;
            this.initialFacingAngle = (float) Math.toDegrees(Math.atan2(wpY - startY, wpX - startX));
        } else {
            this.initialFacingAngle = 0f;
        }
        this.facingAngle = this.initialFacingAngle;
        currentState = new PatrolState(waypoints);
        currentState.enter(this);
    }

    public DroneAI(float startX, float startY, float[][] waypoints) {
        this(startX, startY, waypoints, 48f, 84f, 110f, 65f);
    }

    public DroneAI(float startX, float startY) {
        this(startX, startY, null, 48f, 84f, 110f, 65f);
    }

    public void update(IMapCollision map, Vector2 playerPos, float dt) {
        stateTime  += dt;
        rotorAngle += dt * (currentState.getName().equals("CHASE") ? 900f : 400f);
        if (detectionSuppressedTimer > 0f) {
            detectionSuppressedTimer = Math.max(0f, detectionSuppressedTimer - dt);
        }
        currentState.update(this, map, playerPos, dt);
    }

    public void transitionTo(DroneState newState) {
        if (currentState != null) currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
    }

    public boolean isCatchingPlayer(Vector2 playerPos, float playerRadius) {
        float dx = playerPos.x - position.x;
        float dy = playerPos.y - position.y;
        return (float)Math.sqrt(dx*dx+dy*dy) < (radius + playerRadius);
    }

    public void resetToPatrolAtSpawn(float suppressSeconds) {
        position.x = spawnX;
        position.y = spawnY;
        facingAngle = initialFacingAngle;
        transitionTo(new PatrolState(patrolWaypoints));
        stateTime = 0f;
        rotorAngle = 0f;
        alertLevel = 0f;
        suppressDetection(suppressSeconds);
    }

    public void suppressDetection(float seconds) {
        detectionSuppressedTimer = Math.max(detectionSuppressedTimer, seconds);
        alertLevel = 0f;
        if (!"PATROL".equals(getStateName())) {
            transitionTo(new PatrolState(patrolWaypoints));
        }
    }

    public boolean isDetectionSuppressed() {
        return detectionSuppressedTimer > 0f;
    }

    public float[][] getPatrolWaypoints() { return patrolWaypoints; }

    public void render(ShapeRenderer sr) {
        // Vision cone rendering removed — drone sprite is drawn by CyberGameScene
    }

    public Vector2 getPosition()          { return position; }
    public float   getRadius()            { return radius; }
    public float   getPatrolSpeed()       { return patrolSpeed; }
    public float   getChaseSpeed()        { return chaseSpeed; }
    public float   getSightRange()        { return sightRange; }
    public float   getSightAngle()        { return sightAngle; }
    public float   getFacingAngle()       { return facingAngle; }

    public float getSpawnX() { return spawnX; }
    public float getSpawnY() { return spawnY; }
    public void    setFacingAngle(float a){ facingAngle = a; }
    public float   getAlertLevel()        { return alertLevel; }
    public void    setAlertLevel(float v) { alertLevel = Math.max(0f, Math.min(1f, v)); }
    public String  getStateName()         { return currentState.getName(); }
}
