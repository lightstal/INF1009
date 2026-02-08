package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;

public class ContextImplementation implements GameContext {
    private final EventBus eventBus;
    private final InputController inputController; 

    // Constructor accepts the Interface
    public ContextImplementation(EventBus eventBus, InputController inputController) {
        this.eventBus = eventBus;
        this.inputController = inputController;
    }
    
    @Override
    public EventBus getEventBus() {
    	return eventBus;
    }

    @Override
    public InputController getInputController() { 
        return inputController; 
    }
}