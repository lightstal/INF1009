package io.github.INF1009_P10_Team7.engine.events;

import java.util.HashMap;
import java.util.Map;

public class GameEvent {
	public final EventType type;
    public final Map<String, Object> params;

    public GameEvent(EventType type) {
        this.type = type;
        this.params = new HashMap<>();
    }

    public GameEvent add(String key, Object value) {
        params.put(key, value);
        return this;
    }
}
