package io.github.INF1009_P10_Team7.cyber.components.cctv;

import java.util.List;

import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Entity-based CCTV visibility checks.
 */
public class CctvDetectionSystem {

    public boolean updateAlerts(
        List<GameEntity> cctvEntities,
        boolean[] cctvAlerted,
        Vector2 playerPos,
        IWorldCollisionQuery mapCollision,
        float tileSize,
        float stateTime
    ) {
        boolean anyVisible = false;
        float coneLen = tileSize * 2.6f;
        float halfFov = 28f;

        for (GameEntity cameraEntity : cctvEntities) {
            CctvComponent cctv = cameraEntity.getComponent(CctvComponent.class);
            TransformComponent transform = cameraEntity.getComponent(TransformComponent.class);
            if (cctv == null || transform == null) continue;
            int ci = cctv.getCameraIndex();
            if (ci < 0 || ci >= cctvAlerted.length) continue;

            float cx = transform.getPosition().x;
            float cy = transform.getPosition().y;
            float phase = ci * 1.3f;
            float panAng = (float) Math.sin(stateTime * 0.7f + phase) * 40f;
            float totalAng = cctv.getBaseAngle() + panAng;

            float pdx = playerPos.x - cx;
            float pdy = playerPos.y - cy;
            float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
            if (pDist > coneLen) { cctvAlerted[ci] = false; continue; }

            float angleToPlayer = (float) Math.toDegrees(Math.atan2(pdy, pdx));
            float angleDiff = angleToPlayer - totalAng;
            while (angleDiff > 180f) angleDiff -= 360f;
            while (angleDiff < -180f) angleDiff += 360f;
            if (Math.abs(angleDiff) > halfFov) { cctvAlerted[ci] = false; continue; }

            if (!mapCollision.hasLineOfSight(cx, cy, playerPos.x, playerPos.y)) {
                cctvAlerted[ci] = false;
                continue;
            }

            cctvAlerted[ci] = true;
            anyVisible = true;
        }
        return anyVisible;
    }
}
