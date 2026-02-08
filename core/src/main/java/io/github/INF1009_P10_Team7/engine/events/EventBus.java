package io.github.INF1009_P10_Team7.engine.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private Map<EventType, List<EventListener>> listeners;

    public EventBus() {
        this.listeners = new HashMap<>();
        for (EventType type : EventType.values()) {
            listeners.put(type, new ArrayList<>());
        }
    }

    public void subscribe(EventType type, EventListener listener) {
        if (!listeners.containsKey(type)) listeners.put(type, new ArrayList<>());
        listeners.get(type).add(listener);
    }
    
    public void unsubscribe(EventListener listener) {
        for (List<EventListener> list : listeners.values()) {
            if (list.contains(listener)) {
                list.remove(listener);
            }
        }
    }

    public void publish(GameEvent event) {
        if (listeners.containsKey(event.type)) {
        	
        	// Create a copy of the list to iterate over
            // This prevents concurrent modification errors if a listener unsubscribe while processing an event
            List<EventListener> currentListeners = new ArrayList<>(listeners.get(event.type));
            for (EventListener listener : currentListeners) {
                listener.onNotify(event);
            }
        }
    }
}