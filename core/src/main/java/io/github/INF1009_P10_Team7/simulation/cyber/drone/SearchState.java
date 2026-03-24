package io.github.INF1009_P10_Team7.simulation.cyber.drone;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.simulation.cyber.IMapCollision;

public class SearchState implements DroneState {
    private final float targetX;
    private final float targetY;
    private final float duration;
    private float timer;

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
            ai.setFacingAngle((float)Math.toDegrees(Math.atan2(dy, dx)));
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
