package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

public class ContextImplementation implements GameContext {
    private final EventBus eventBus;
    private final InputController inputController;
    private final EntityManager entityManager;

    // Constructor accepts the Interface
    public ContextImplementation(EventBus eventBus, InputController inputController, EntityManager entityManager) {
        this.eventBus = eventBus;
        this.inputController = inputController;
        this.entityManager = entityManager;
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
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
