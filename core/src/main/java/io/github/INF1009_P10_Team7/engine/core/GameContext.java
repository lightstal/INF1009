package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

public interface GameContext {
    EventBus getEventBus();
    InputController getInputController();
    EntityManager getEntityManager();
}
