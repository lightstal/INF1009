package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.AudioController;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

/**
 * Main collision manager that coordinates collision detection and resolution.
 * Maintains a list of collidable objects and checks for collisions each frame.
 */
public class CollisionManager {

    private final List<ICollidable> collidableObjects;
    private final Map<String, CollisionResolution.ResolutionType> resolutionTypes;
    private final Map<String, CollisionResolution.CollisionCallback> callbacks;
    private final Set<String> activeCollisions;
    private final AudioController audioController;

    // Sound effect to play on collision
    private String collisionSoundPath = null;
    private boolean playSoundOnCollision = false;

    // FIXED WORLD BOUNDS - matches the virtual world, NOT the screen!
    private float worldWidth = 800f;
    private float worldHeight = 480f;

    /**
     * Creates a new CollisionManager.
     *
     * @param audioController AudioController for playing collision sounds
     */
    public CollisionManager(AudioController audioController) {
        this.collidableObjects = new ArrayList<>();
        this.resolutionTypes = new HashMap<>();
        this.callbacks = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.audioController = audioController;
    }

    /**
     * Sets the world bounds for boundary collision detection.
     * These should match the virtual world size used by the viewport (e.g., 800x480).
     */
    public void setWorldBounds(float width, float height) {
        this.worldWidth = width;
        this.worldHeight = height;
        Gdx.app.log("CollisionManager", "World bounds set to: " + width + "x" + height);
    }

    /**
     * Registers a collidable object with the collision manager.
     */
    public void registerCollidable(ICollidable collidable, CollisionResolution.ResolutionType resolutionType) {
        if (!collidableObjects.contains(collidable)) {
            collidableObjects.add(collidable);
            resolutionTypes.put(collidable.getObjectId(), resolutionType);
            Gdx.app.log("CollisionManager", "Registered collidable: " + collidable.getObjectId() +
                " with resolution type: " + resolutionType);
        }
    }

    /**
     * Unregisters a collidable object from the collision manager.
     */
    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        resolutionTypes.remove(collidable.getObjectId());
        callbacks.remove(collidable.getObjectId());
    }

    /**
     * Sets a custom callback for a specific collidable object.
     */
    public void setCollisionCallback(String objectId, CollisionResolution.CollisionCallback callback) {
        callbacks.put(objectId, callback);
    }

    /**
     * Sets the sound effect to play when collisions occur.
     */
    public void setCollisionSound(String soundPath) {
        this.collisionSoundPath = soundPath;
        this.playSoundOnCollision = true;
    }

    /**
     * Enables or disables collision sound effects.
     */
    public void setPlaySoundOnCollision(boolean enabled) {
        this.playSoundOnCollision = enabled;
    }

    /**
     * Updates collision detection and resolution.
     * Should be called every frame.
     */
    public void update(float deltaTime) {
        Set<String> currentCollisions = new HashSet<>();

        // ---- Entity vs Entity ----
        for (int i = 0; i < collidableObjects.size(); i++) {
            ICollidable obj1 = collidableObjects.get(i);
            if (!obj1.isCollidable()) continue;

            for (int j = i + 1; j < collidableObjects.size(); j++) {
                ICollidable obj2 = collidableObjects.get(j);
                if (!obj2.isCollidable()) continue;

                CollisionDetection.CollisionInfo collisionInfo =
                    CollisionDetection.getCollisionInfo(obj1, obj2);

                if (collisionInfo != null) {
                    String key = getCollisionKey(obj1.getObjectId(), obj2.getObjectId());
                    currentCollisions.add(key);

                    if (!activeCollisions.contains(key)) {
                        handleCollision(obj1, obj2, collisionInfo);
                    }
                }
            }
        }

        // ---- Entity vs Boundary (edge-based using radius) ----
        for (ICollidable obj : collidableObjects) {
            handleBoundaryCollision(obj);
        }

        activeCollisions.clear();
        activeCollisions.addAll(currentCollisions);
    }


    /**
     * Handles a collision between two objects.
     */
    private void handleCollision(ICollidable obj1, ICollidable obj2,
                                 CollisionDetection.CollisionInfo collisionInfo) {
        Gdx.app.log("Collision", "Collision detected: " + obj1.getObjectId() +
            " <-> " + obj2.getObjectId());

        // Play collision sound if enabled
        if (playSoundOnCollision && collisionSoundPath != null && audioController != null) {
            audioController.playSound(collisionSoundPath);
        }

        // Get resolution types for both objects
        CollisionResolution.ResolutionType type1 = resolutionTypes.get(obj1.getObjectId());
        CollisionResolution.ResolutionType type2 = resolutionTypes.get(obj2.getObjectId());

        // Use BOUNCE as default if not specified
        CollisionResolution.ResolutionType resolutionType =
            (type1 != null) ? type1 : CollisionResolution.ResolutionType.BOUNCE;

        // Get callback
        CollisionResolution.CollisionCallback callback1 = callbacks.get(obj1.getObjectId());
        CollisionResolution.CollisionCallback callback2 = callbacks.get(obj2.getObjectId());

        // Try to cast to Entity (needed for resolution)
        if (obj1 instanceof Entity && obj2 instanceof Entity) {
            Entity entity1 = (Entity) obj1;
            Entity entity2 = (Entity) obj2;

            // Call callbacks if they exist
            if (callback1 != null) {
                callback1.onCollision(entity1, entity2, collisionInfo);
            }
            if (callback2 != null) {
                callback2.onCollision(entity2, entity1, collisionInfo);
            }

            // Resolve collision
            CollisionResolution.resolve(entity1, entity2, collisionInfo, resolutionType, null);
        }
    }

    /**
     * Handles boundary collision using WORLD coordinates and EDGE detection (radius-aware).
     * The entity's EDGE (center - radius) must stay inside the world, not just its center.
     * Works for ALL entities - with or without PhysicComponent.
     */
    private void handleBoundaryCollision(ICollidable obj) {
        if (obj == null || !obj.isCollidable()) return;

        Vector2 pos = obj.getPosition();
        float r = obj.getCollisionRadius();

        float w = worldWidth;
        float h = worldHeight;

        boolean hit = false;

        // Edge-based boundary: entity's SIDE touches the wall, not its center
        if (pos.x - r < 0) { pos.x = r;     hit = true; }
        if (pos.x + r > w) { pos.x = w - r;  hit = true; }
        if (pos.y - r < 0) { pos.y = r;     hit = true; }
        if (pos.y + r > h) { pos.y = h - r;  hit = true; }

        // If entity has physics, bounce the velocity
        if (hit && obj instanceof Entity) {
            Entity e = (Entity) obj;
            PhysicComponent pc = e.getComponent(PhysicComponent.class);
            if (pc != null) {
                Vector2 vel = pc.getVelocity();
                if (pos.x <= r)     vel.x = Math.abs(vel.x);
                if (pos.x >= w - r) vel.x = -Math.abs(vel.x);
                if (pos.y <= r)     vel.y = Math.abs(vel.y);
                if (pos.y >= h - r) vel.y = -Math.abs(vel.y);
            }
        }

        if (hit) {
            Gdx.app.log("Boundary CollisionManager", "Boundary collision: " + obj.getObjectId());
        }
    }


    /**
     * Creates a unique key for a collision pair.
     */
    private String getCollisionKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + ":" + id2;
        } else {
            return id2 + ":" + id1;
        }
    }

    /**
     * Clears all registered collidable objects.
     */
    public void clear() {
        collidableObjects.clear();
        resolutionTypes.clear();
        callbacks.clear();
        activeCollisions.clear();
        Gdx.app.log("CollisionManager", "All collidable objects cleared");
    }

    /**
     * Gets the number of registered collidable objects.
     */
    public int getCollidableCount() {
        return collidableObjects.size();
    }
}
