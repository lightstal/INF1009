package io.github.INF1009_P10_Team7.cyber.components.terminal;

import java.util.List;

import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Terminal interaction queries for terminal entities.
 */
public class TerminalInteractionSystem {

    public int getNearbyTerminalIndex(
        Vector2 from, float radius, List<GameEntity> terminalEntities, boolean[] terminalSolved
    ) {
        int bestIdx = -1;
        float best = radius;
        for (GameEntity terminalEntity : terminalEntities) {
            TerminalComponent terminalComponent = terminalEntity.getComponent(TerminalComponent.class);
            TransformComponent terminalTransform = terminalEntity.getComponent(TransformComponent.class);
            if (terminalComponent == null || terminalTransform == null) continue;
            int i = terminalComponent.getTerminalIndex();
            if (i < 0 || i >= terminalSolved.length || terminalSolved[i]) continue;
            float tx = terminalTransform.getPosition().x;
            float ty = terminalTransform.getPosition().y;
            float dx = from.x - tx;
            float dy = from.y - ty;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < best) {
                best = d;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public int findInteractableTerminalIndex(
        Vector2 from, float radius, List<GameEntity> terminalEntities, boolean[] terminalSolved
    ) {
        return getNearbyTerminalIndex(from, radius, terminalEntities, terminalSolved);
    }
}
