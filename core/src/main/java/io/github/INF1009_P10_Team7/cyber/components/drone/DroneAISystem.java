package io.github.INF1009_P10_Team7.cyber.components.drone;

import java.util.ArrayList;
import java.util.List;

import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * DroneAISystem updates all drone entities each frame.
 * Keeps DroneAI orchestration outside scene update loops.
 */
public class DroneAISystem {

    public List<DroneAI> updateDrones(
        List<GameEntity> droneEntities,
        IWorldCollisionQuery map,
        Vector2 playerPos,
        float delta,
        List<DroneAI> newlyChasingOut
    ) {
        List<DroneAI> drones = new ArrayList<>(droneEntities.size());
        if (newlyChasingOut != null) newlyChasingOut.clear();
        if (playerPos == null) return drones;

        for (GameEntity droneEntity : droneEntities) {
            DroneComponent droneComponent = droneEntity.getComponent(DroneComponent.class);
            if (droneComponent == null) continue;

            DroneAI drone = droneComponent.getDrone();
            if (drone == null) continue;

            boolean wasChasing = "CHASE".equals(drone.getStateName());
            drone.update(map, playerPos, delta);
            boolean nowChasing = "CHASE".equals(drone.getStateName());
            if (!wasChasing && nowChasing && newlyChasingOut != null) {
                newlyChasingOut.add(drone);
            }
            drones.add(drone);
        }
        return drones;
    }

    public boolean isPlayerCaught(List<DroneAI> drones, Vector2 playerPos, float playerRadius) {
        if (playerPos == null || drones == null || drones.isEmpty()) return false;
        for (DroneAI drone : drones) {
            if (drone.isCatchingPlayer(playerPos, playerRadius)) return true;
        }
        return false;
    }
}
