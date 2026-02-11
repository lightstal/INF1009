package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;
import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;

public class ContextImplementation implements GameContext {
    private final EventBus eventBus;
    private final InputController inputController;
    private final AudioController audioController;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;

    // Constructor accepts the Interface
    public ContextImplementation(EventBus eventBus,
                                 InputOutputManager inputOutputManager,
                                 EntityManager entityManager,
                                 CollisionManager collisionManager) {
        this.eventBus = eventBus;
        this.inputController = inputOutputManager;
        this.audioController = inputOutputManager.getAudioOutput();
        this.entityManager = entityManager;
        this.collisionManager = collisionManager;
    }

    @Override
    public EventBus getEventBus() {
    	return eventBus;
    }

    @Override
    public InputController getInputController() {
        return inputController;
    }

    @Override
    public AudioController getAudioController() {
        return audioController;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public CollisionManager getCollisionManager() { return collisionManager; }
}
