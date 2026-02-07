package io.github.INF1009_P10_Team7.engine.entity;

// Represents a game entity with a name.
public class GameEntity extends Entity {

    private String name;

    // Creates a new GameEntity with an empty name.
    public GameEntity() {
        super();
        this.name = "";
    }

    // Creates a new GameEntity with the specified name.
    public GameEntity(String name) {
        super();
        this.name = name;
    }

    // Gets the name of this entity.
    public String getName() {
        return name;
    }

    // Sets the name of this entity.
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GameEntity{name='" + name + "', id=" + getId() + ", active=" + isActive() + "}";
    }
}
