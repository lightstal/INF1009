package io.github.INF1009_P10_Team7.cyber.components;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.cyber.drone.DroneAI;

public class DroneComponent implements IComponent {
    private Entity owner;
    private DroneAI aiLogic;

    public DroneComponent(DroneAI aiLogic) {
        this.aiLogic = aiLogic;
    }

    @Override
    public void onAdded(Entity entity) { this.owner = entity; }

    @Override
    public void onRemoved(Entity entity) { this.owner = null; }

    @Override
    public void update(float deltaTime) {
        // AI update is handled in Scene for now to pass collision/player info, 
        // but it belongs to this component structurally.
    }

    public DroneAI getAILogic() {
        return aiLogic;
    }
}