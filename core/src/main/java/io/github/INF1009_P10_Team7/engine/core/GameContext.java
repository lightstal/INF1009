package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;

//move
import io.github.INF1009_P10_Team7.engine.movement.MovementManager;
public interface GameContext {
    EventBus getEventBus();
    InputController getInputController();
    AudioController getAudioController();
    CollisionManager getCollisionManager();

    MovementManager getMovementManager();
}
