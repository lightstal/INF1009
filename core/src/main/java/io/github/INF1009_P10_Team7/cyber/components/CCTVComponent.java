package io.github.INF1009_P10_Team7.cyber.components;

import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.cyber.IMapCollision;

public class CCTVComponent implements IComponent {
    private Entity owner;
    private float baseAngle;
    private float phaseOffset;
    private boolean isAlerted = false;
    private IMapCollision mapCollision;

    public CCTVComponent(float baseAngle, float phaseOffset, IMapCollision mapCollision) {
        this.baseAngle = baseAngle;
        this.phaseOffset = phaseOffset;
        this.mapCollision = mapCollision;
    }

    @Override public void onAdded(Entity entity) { this.owner = entity; }
    @Override public void onRemoved(Entity entity) { this.owner = null; }
    @Override public void update(float deltaTime) { }

    // SRP: The CCTV handles its own vision math now
    public boolean checkDetection(Vector2 playerPos, float stateTime, float coneLength, float halfFov) {
        TransformComponent tc = owner.getComponent(TransformComponent.class);
        if (tc == null) return false;

        float cx = tc.getPosition().x;
        float cy = tc.getPosition().y;
        
        float panAngle = (float) Math.sin(stateTime * 0.7f + phaseOffset) * 40f;
        float totalAng = baseAngle + panAngle;

        float pdx = playerPos.x - cx;
        float pdy = playerPos.y - cy;
        float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);

        if (pDist > coneLength) {
            isAlerted = false;
            return false;
        }

        float angleToPlayer = (float) Math.toDegrees(Math.atan2(pdy, pdx));
        float angleDiff = angleToPlayer - totalAng;
        while (angleDiff > 180f) angleDiff -= 360f;
        while (angleDiff < -180f) angleDiff += 360f;

        if (Math.abs(angleDiff) > halfFov) {
            isAlerted = false;
            return false;
        }

        if (!mapCollision.hasLineOfSight(cx, cy, playerPos.x, playerPos.y)) {
            isAlerted = false;
            return false;
        }

        isAlerted = true;
        return true;
    }

    public boolean isAlerted() { return isAlerted; }
}