package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;

public interface GameContext {
    EventBus getEventBus();
    InputController getInputController();
    AudioController getAudioController();
    EntityManager getEntityManager();
    CollisionManager getCollisionManager();
}
