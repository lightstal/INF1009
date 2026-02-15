package io.github.INF1009_P10_Team7.engine.core;

import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.movement.MovementManager;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * GameEngine (engine layer)
 *
 * Owns and orchestrates all engine sub-systems.
 * Exposes ONLY interfaces to the simulation layer (Dependency Inversion).
 *
 * CHANGES FROM ORIGINAL V2:
 * - Removed rebuildForScene() — scenes now register entities with managers directly
 * - Uses ICollisionSystem, IMovementSystem, IEntitySystem interfaces
 * - No references to EntityDefinition, MovementComponent, PhysicComponent (no context coupling)
 */
public class GameEngine {

    private final InputOutputManager io;
    private final CollisionManager collision;
    private final MovementManager movement;
    private final EntityManager entities;
    private final SceneManager scenes;

    public GameEngine() {
        io = new InputOutputManager();
        collision = new CollisionManager(io);
        movement = new MovementManager();
        entities = new EntityManager();
        scenes = new SceneManager();
    }

    // Expose ONLY interfaces to simulation/scenes
    public IInputController getInput() { return io; }
    public IAudioController getAudio() { return io; }
    public EntityQuery getEntityQuery() { return entities; }
    public IEntitySystem getEntitySystem() { return entities; }
    public ICollisionSystem getCollisionSystem() { return collision; }
    public IMovementSystem getMovementSystem() { return movement; }
    public SceneNavigator getNavigator() { return scenes; }

    public SceneManager getSceneManager() { return scenes; }

    public void update(float dt) {
        io.update();

        // When a scene is about to be replaced, clear all manager state FIRST
        // so the new scene's onLoad() populates fresh managers.
        if (scenes.hasPendingReplace()) {
            collision.clear();
            movement.clear();
            entities.clear();
        }

        scenes.update(dt);
        // Consume the flag (no extra action needed — scene already loaded by SceneManager)
        scenes.consumeSceneReplacedFlag();

        Scene top = scenes.getCurrentScene();
        boolean pauseWorld = top != null && top.blocksWorldUpdate();

        if (!pauseWorld) {
            movement.updateAll(dt);
            collision.update(dt);
            scenes.lateUpdate(dt);
            entities.updateAll(dt);
        }
    }

    public void render() {
        scenes.render();
    }

    public void resize(int w, int h) {
        scenes.resize(w, h);
    }

    public void dispose() {
        scenes.dispose();
        collision.clear();
        movement.clear();
        entities.dispose();
        io.dispose();
    }
}
