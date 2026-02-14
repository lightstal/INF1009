package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;

/**
 * Main collision manager that coordinates collision detection and resolution.
 * Maintains a list of collidable objects and checks for collisions each frame.
 *
 * NOTE:
 * - No boundary function here.
 * - If you want world boundaries, create fixed wall entities and register them.
 * - CollisionManager only checks entity-vs-entity.
 */
public class CollisionManager {

    private final List<ICollidable> collidableObjects;
    private final Map<String, CollisionResolution.ResolutionType> resolutionTypes;
    private final Map<String, CollisionResolution.CollisionCallback> callbacks;
    private final Set<String> activeCollisions; // Track which pairs are currently colliding
    private final AudioController audioController;

    // Sound effect to play on collision
    private String collisionSoundPath = null;
    private boolean playSoundOnCollision = false;

    public CollisionManager(AudioController audioController) {
        this.collidableObjects = new ArrayList<>();
        this.resolutionTypes = new HashMap<>();
        this.callbacks = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.audioController = audioController;
    }

    public void registerCollidable(ICollidable collidable, CollisionResolution.ResolutionType resolutionType) {
        if (!collidableObjects.contains(collidable)) {
            collidableObjects.add(collidable);
            resolutionTypes.put(collidable.getObjectId(), resolutionType);
            Gdx.app.log("CollisionManager", "Registered collidable: " + collidable.getObjectId() +
                " with resolution type: " + resolutionType);
        }
    }

    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        resolutionTypes.remove(collidable.getObjectId());
        callbacks.remove(collidable.getObjectId());
    }

    public void setCollisionCallback(String objectId, CollisionResolution.CollisionCallback callback) {
        callbacks.put(objectId, callback);
    }

    public void setCollisionSound(String soundPath) {
        this.collisionSoundPath = soundPath;
        this.playSoundOnCollision = true;
    }

    public void setPlaySoundOnCollision(boolean enabled) {
        this.playSoundOnCollision = enabled;
    }

    public void update(float deltaTime) {
        Set<String> currentCollisions = new HashSet<>();

        // Entity vs Entity ONLY
        for (int i = 0; i < collidableObjects.size(); i++) {
            ICollidable obj1 = collidableObjects.get(i);
            if (obj1 == null || !obj1.isCollidable()) continue;

            for (int j = i + 1; j < collidableObjects.size(); j++) {
                ICollidable obj2 = collidableObjects.get(j);
                if (obj2 == null || !obj2.isCollidable()) continue;

                CollisionInfo collisionInfo =
                    CollisionDetection.getCollisionInfo(obj1, obj2);

                if (collisionInfo != null) {
                    String key = getCollisionKey(obj1.getObjectId(), obj2.getObjectId());
                    currentCollisions.add(key);

                    // Only process new collisions
                    if (!activeCollisions.contains(key)) {
                        handleCollision(obj1, obj2, collisionInfo);
                    }
                }
            }
        }

        activeCollisions.clear();
        activeCollisions.addAll(currentCollisions);
    }

    private void handleCollision(ICollidable obj1, ICollidable obj2,
                                 CollisionInfo collisionInfo) {

        Gdx.app.log("Collision", "Collision detected: " + obj1.getObjectId() +
            " <-> " + obj2.getObjectId());

        // Play collision sound if enabled
        if (playSoundOnCollision && collisionSoundPath != null && audioController != null) {
            audioController.playSound(collisionSoundPath);
        }

        // Get resolution types for both objects
        CollisionResolution.ResolutionType type1 = resolutionTypes.get(obj1.getObjectId());
        CollisionResolution.ResolutionType type2 = resolutionTypes.get(obj2.getObjectId());

        // Resolution priority:
        // PASS_THROUGH > DESTROY > BOUNCE
        CollisionResolution.ResolutionType resolutionType;
        if (type1 == CollisionResolution.ResolutionType.PASS_THROUGH ||
            type2 == CollisionResolution.ResolutionType.PASS_THROUGH) {
            resolutionType = CollisionResolution.ResolutionType.PASS_THROUGH;
        } else if (type1 == CollisionResolution.ResolutionType.DESTROY ||
            type2 == CollisionResolution.ResolutionType.DESTROY) {
            resolutionType = CollisionResolution.ResolutionType.DESTROY;
        } else {
            resolutionType = CollisionResolution.ResolutionType.BOUNCE;
        }

        // Get callbacks
        CollisionResolution.CollisionCallback callback1 = callbacks.get(obj1.getObjectId());
        CollisionResolution.CollisionCallback callback2 = callbacks.get(obj2.getObjectId());

        // Only resolve if both are Entities (your resolver uses components)
        if (obj1 instanceof Entity && obj2 instanceof Entity) {
            Entity entity1 = (Entity) obj1;
            Entity entity2 = (Entity) obj2;

            if (callback1 != null) callback1.onCollision(entity1, entity2, collisionInfo);
            if (callback2 != null) callback2.onCollision(entity2, entity1, collisionInfo);

            CollisionResolution.resolve(entity1, entity2, collisionInfo, resolutionType, null);
        }
    }

    private String getCollisionKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + ":" + id2;
        } else {
            return id2 + ":" + id1;
        }
    }

    public void clear() {
        collidableObjects.clear();
        resolutionTypes.clear();
        callbacks.clear();
        activeCollisions.clear();
        Gdx.app.log("CollisionManager", "All collidable objects cleared");
    }

    public int getCollidableCount() {
        return collidableObjects.size();
    }
}
