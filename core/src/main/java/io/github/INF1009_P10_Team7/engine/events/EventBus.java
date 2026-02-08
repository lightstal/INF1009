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

    public void publish(GameEvent event) {
        if (listeners.containsKey(event.type)) {
            for (EventListener listener : listeners.get(event.type)) {
                listener.onNotify(event);
            }
        }
    }
}