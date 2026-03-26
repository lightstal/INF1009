package io.github.INF1009_P10_Team7.cyber.components.drone;

import java.util.List;

import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * DroneAIMovementBehaviour, routes drone movement through the engine
 * {@link io.github.INF1009_P10_Team7.engine.movement.IMovementSystem}.
 *
 * <p>The drone's state machine still decides behaviour, but the engine
 * calls this behaviour each frame and updates the entity Transform.</p>
 */
public class DroneAIMovementBehaviour implements MovementBehaviour {

    private final IWorldCollisionQuery mapCollision;
    private final Vector2 playerPosSnapshot;
    private final List<DroneAI> newlyChasingOut;

    public DroneAIMovementBehaviour(
        IWorldCollisionQuery mapCollision,
        Vector2 playerPosSnapshot,
        List<DroneAI> newlyChasingOut
    ) {
        this.mapCollision = mapCollision;
        this.playerPosSnapshot = playerPosSnapshot;
        this.newlyChasingOut = newlyChasingOut;
    }

    @Override
    public void move(Entity entity, float deltaTime) {
        if (entity == null) return;

        DroneComponent droneComponent = entity.getComponent(DroneComponent.class);
        if (droneComponent == null) return;

        DroneAI drone = droneComponent.getDrone();
        if (drone == null) return;

        boolean wasChasing = "CHASE".equals(drone.getStateName());
        drone.update(mapCollision, playerPosSnapshot, deltaTime);
        boolean nowChasing = "CHASE".equals(drone.getStateName());

        if (newlyChasingOut != null && !wasChasing && nowChasing) {
            newlyChasingOut.add(drone);
        }

        // Keep transform synced for render + collision queries.
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.getPosition().set(drone.getPosition().x, drone.getPosition().y);
        }
    }
}

