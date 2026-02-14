package io.github.INF1009_P10_Team7.engine.core;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.collision.CollisionManager;
import io.github.INF1009_P10_Team7.engine.entity.EntityDefinition;
import io.github.INF1009_P10_Team7.engine.entity.EntityManager;
import io.github.INF1009_P10_Team7.engine.entity.EntityQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.MovementComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputController;
import io.github.INF1009_P10_Team7.engine.inputoutput.InputOutputManager;
import io.github.INF1009_P10_Team7.engine.movement.MovementBehaviour;
import io.github.INF1009_P10_Team7.engine.movement.MovementManager;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneManager;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

import java.util.List;
import java.util.Map;


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
    public InputController getInput() { return io; }
    public AudioController getAudio() { return io; }
    public EntityQuery getEntities() { return entities; }
    public SceneNavigator getNavigator() { return scenes; }

    public SceneManager getSceneManager() { return scenes; }

    public void setCollisionSound(String soundPath) {
        collision.setCollisionSound(soundPath);
    }

    public void update(float dt) {
        io.update();

        scenes.update(dt);

        if (scenes.consumeSceneReplacedFlag()) {
            rebuildForScene(scenes.getCurrentScene());
        }

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

    private void rebuildForScene(Scene scene) {
        if (scene == null) return;

        collision.clear();
        movement.clear();
        entities.clear();

        List<EntityDefinition> defs = scene.getEntityDefinitions();
        Map<String, GameEntity> created = entities.createEntitiesFromDefinitions(defs);

        for (EntityDefinition def : defs) {
            GameEntity e = created.get(def.name);
            if (e == null) continue;

            if (def.resolutionType != null && def.collisionRadius > 0f) {
                collision.registerCollidable(e, def.resolutionType);
            }

            MovementComponent mc = e.getComponent(MovementComponent.class);
            MovementBehaviour behaviour = null;

            if (mc != null) {
                behaviour = mc.getMovementBehaviour();
            }

            if (behaviour != null || e.getComponent(PhysicComponent.class) != null) {
                movement.addEntity(e, behaviour);
            }
        }

        Gdx.app.log("GameEngine", "Rebuilt scene state: entities=" + created.size() +
            ", collidables=" + collision.getCollidableCount() +
            ", movers=" + movement.getEntityCount());
    }
}
