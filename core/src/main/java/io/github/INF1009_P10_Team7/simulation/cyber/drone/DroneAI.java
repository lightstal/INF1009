package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
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
        currentState = new PatrolState(waypoints);
        currentState.enter(this);
    }

    public DroneAI(float startX, float startY, float[][] waypoints) {
        this(startX, startY, waypoints, 48f, 84f, 110f, 65f);
    }

    public DroneAI(float startX, float startY) {
        this(startX, startY, null, 48f, 84f, 110f, 65f);
    }

    public void update(TileMap map, Vector2 playerPos, float dt) {
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
        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 6f);
        boolean chasing = currentState.getName().equals("CHASE");
        float ax = position.x, ay = position.y;

        float r1 = (float)Math.toRadians(facingAngle - sightAngle/2f);
        float r2 = (float)Math.toRadians(facingAngle + sightAngle/2f);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        float coneAlpha = isDetectionSuppressed() ? 0.012f : 0.025f;
        sr.setColor(chasing ? new Color(0.9f, 0.1f, 0f, 0.04f * alertLevel)
                            : new Color(0.9f, 0.85f, 0f, coneAlpha));
        int steps = 24;
        for (int i=0;i<steps;i++) {
            float a1 = r1 + (r2-r1)*i/steps;
            float a2 = r1 + (r2-r1)*(i+1)/steps;
            sr.triangle(ax, ay,
                ax+(float)Math.cos(a1)*sightRange, ay+(float)Math.sin(a1)*sightRange,
                ax+(float)Math.cos(a2)*sightRange, ay+(float)Math.sin(a2)*sightRange);
        }

        float innerRange = sightRange * 0.5f;
        sr.setColor(chasing ? new Color(1f, 0.2f, 0f, 0.08f * alertLevel)
                            : new Color(1f, 1f, 0.1f, isDetectionSuppressed() ? 0.02f : 0.045f));
        for (int i=0;i<steps;i++) {
            float a1 = r1 + (r2-r1)*i/steps;
            float a2 = r1 + (r2-r1)*(i+1)/steps;
            sr.triangle(ax, ay,
                ax+(float)Math.cos(a1)*innerRange, ay+(float)Math.sin(a1)*innerRange,
                ax+(float)Math.cos(a2)*innerRange, ay+(float)Math.sin(a2)*innerRange);
        }

        if (chasing) {
            float ringR  = radius + 10f + 6f * pulse;
            float ringR2 = radius + 20f + 8f * (1f - pulse);
            sr.setColor(1f, 0.15f * pulse, 0f, 0.35f * pulse);
            sr.circle(ax, ay, ringR, 24);
            sr.setColor(1f, 0.1f, 0f, 0.15f * alertLevel);
            sr.circle(ax, ay, ringR2, 24);
        }

        sr.setColor(0f, 0f, 0f, 0.3f);
        sr.ellipse(ax - radius * 0.9f, ay - 4f, radius * 1.8f, radius * 0.5f);

        Color outerColor = chasing
            ? new Color(0.6f + 0.4f*pulse, 0.05f, 0f, 1f)
            : new Color(0.10f, 0.55f, 0.80f, 1f);
        sr.setColor(outerColor);
        int sides = 6;
        float rot = stateTime * (chasing ? 2f : 0.5f);
        for (int i=0;i<sides;i++) {
            double a1 = 2*Math.PI*i/sides + rot;
            double a2 = 2*Math.PI*(i+1)/sides + rot;
            sr.triangle(ax, ay,
                ax+(float)Math.cos(a1)*radius, ay+(float)Math.sin(a1)*radius,
                ax+(float)Math.cos(a2)*radius, ay+(float)Math.sin(a2)*radius);
        }

        float innerR = radius * 0.65f;
        sr.setColor(chasing ? new Color(0.15f, 0.02f, 0f, 1f)
                            : new Color(0.02f, 0.12f, 0.20f, 1f));
        for (int i=0;i<sides;i++) {
            double a1 = 2*Math.PI*i/sides + rot;
            double a2 = 2*Math.PI*(i+1)/sides + rot;
            sr.triangle(ax, ay,
                ax+(float)Math.cos(a1)*innerR, ay+(float)Math.sin(a1)*innerR,
                ax+(float)Math.cos(a2)*innerR, ay+(float)Math.sin(a2)*innerR);
        }

        float armLen = radius + 6f;
        Color rotorColor = chasing ? new Color(0.9f, 0.3f, 0f, 0.8f)
                                   : new Color(0.3f, 0.9f, 1f, 0.8f);
        for (int i=0;i<4;i++) {
            double armAngle = Math.toRadians(rotorAngle * (i%2==0?1:-1)) + i * Math.PI/2;
            float rx = ax + (float)Math.cos(armAngle) * armLen;
            float ry = ay + (float)Math.sin(armAngle) * armLen;
            sr.setColor(chasing ? new Color(0.5f, 0.1f, 0f, 0.9f)
                                 : new Color(0.1f, 0.4f, 0.6f, 0.9f));
            float perpX = -(float)Math.sin(armAngle), perpY = (float)Math.cos(armAngle);
            sr.triangle(ax + perpX, ay + perpY, rx + perpX, ry + perpY, rx - perpX, ry - perpY);
            sr.triangle(ax + perpX, ay + perpY, ax - perpX, ay - perpY, rx - perpX, ry - perpY);
            sr.setColor(rotorColor);
            sr.circle(rx, ry, 5f, 8);
            sr.setColor(rotorColor.r, rotorColor.g, rotorColor.b, 0.3f);
            sr.circle(rx, ry, 7f, 12);
        }

        sr.setColor(chasing ? new Color(1f, 0.3f, 0f, 1f) : new Color(0f, 1f, 1f, 1f));
        sr.circle(ax, ay, 5f, 16);
        float pupilPulse = chasing ? pulse : 0.5f + 0.5f * (float)Math.sin(stateTime * 3f);
        sr.setColor(1f, 1f, 1f, 0.9f);
        sr.circle(ax, ay, 2.5f * pupilPulse, 10);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(chasing ? new Color(1f, 0.3f, 0f, 0.55f)
                            : new Color(1f, 0.95f, 0f, isDetectionSuppressed() ? 0.12f : 0.25f));
        sr.line(ax, ay, ax+(float)Math.cos(r1)*sightRange, ay+(float)Math.sin(r1)*sightRange);
        sr.line(ax, ay, ax+(float)Math.cos(r2)*sightRange, ay+(float)Math.sin(r2)*sightRange);

        steps = 16;
        for (int i=0;i<steps;i++) {
            float a1 = r1+(r2-r1)*i/steps, a2 = r1+(r2-r1)*(i+1)/steps;
            sr.line(ax+(float)Math.cos(a1)*sightRange, ay+(float)Math.sin(a1)*sightRange,
                    ax+(float)Math.cos(a2)*sightRange, ay+(float)Math.sin(a2)*sightRange);
        }

        sr.setColor(chasing ? new Color(1f, 0.5f, 0f, 0.7f)
                            : new Color(0.2f, 0.9f, 1f, 0.6f));
        for (int i=0;i<sides;i++) {
            double a1=2*Math.PI*i/sides+rot, a2=2*Math.PI*(i+1)/sides+rot;
            sr.line(ax+(float)Math.cos(a1)*radius, ay+(float)Math.sin(a1)*radius,
                    ax+(float)Math.cos(a2)*radius, ay+(float)Math.sin(a2)*radius);
        }
        sr.end();
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
