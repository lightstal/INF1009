package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.events.EventBus;
import io.github.INF1009_P10_Team7.engine.events.EventType;
import io.github.INF1009_P10_Team7.engine.events.GameEvent;

/**
 * Main collision manager that coordinates collision detection and resolution.
 * Maintains a list of collidable objects and checks for collisions each frame.
 */
public class CollisionManager {

    private final List<ICollidable> collidableObjects;
    private final Map<String, CollisionResolution.ResolutionType> resolutionTypes;
    private final Map<String, CollisionResolution.CollisionCallback> callbacks;
    private final Set<String> activeCollisions; // Track which pairs are currently colliding
    private final EventBus eventBus;

    // Sound effect to play on collision
    private String collisionSoundPath = null;
    private boolean playSoundOnCollision = false;

    /**
     * Creates a new CollisionManager.
     *
     * @param io InputOutputManager for playing collision sounds
     */
    public CollisionManager(EventBus eventBus) {
        this.collidableObjects = new ArrayList<>();
        this.resolutionTypes = new HashMap<>();
        this.callbacks = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.eventBus = eventBus;
    }

    /**
     * Registers a collidable object with the collision manager.
     *
     * @param collidable The object to register
     * @param resolutionType How collisions should be resolved for this object
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
     *
     * @param collidable The object to unregister
     */
    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        resolutionTypes.remove(collidable.getObjectId());
        callbacks.remove(collidable.getObjectId());
    }

    /**
     * Sets a custom callback for a specific collidable object.
     * This callback will be invoked whenever the object collides with another.
     *
     * @param objectId The ID of the collidable object
     * @param callback The callback to invoke on collision
     */
    public void setCollisionCallback(String objectId, CollisionResolution.CollisionCallback callback) {
        callbacks.put(objectId, callback);
    }

    /**
     * Sets the sound effect to play when collisions occur.
     *
     * @param soundPath Path to the sound file (e.g., "bell.mp3")
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
     *
     * @param deltaTime Time since last frame
     */
    public void update(float deltaTime) {
        Set<String> currentCollisions = new HashSet<>();

        // Check all pairs of collidable objects
        for (int i = 0; i < collidableObjects.size(); i++) {
            ICollidable obj1 = collidableObjects.get(i);

            if (!obj1.isCollidable()) {
                continue;
            }

            for (int j = i + 1; j < collidableObjects.size(); j++) {
                ICollidable obj2 = collidableObjects.get(j);

                if (!obj2.isCollidable()) {
                    continue;
                }

                // Check if collision occurs
                CollisionDetection.CollisionInfo collisionInfo =
                    CollisionDetection.getCollisionInfo(obj1, obj2);

                if (collisionInfo != null) {
                    String collisionKey = getCollisionKey(obj1.getObjectId(), obj2.getObjectId());
                    currentCollisions.add(collisionKey);

                    // Only process if this is a new collision (not continuing from previous frame)
                    if (!activeCollisions.contains(collisionKey)) {
                        handleCollision(obj1, obj2, collisionInfo);
                    }
                }
            }
        }

        // Update active collisions
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
        if (playSoundOnCollision && collisionSoundPath != null && eventBus != null) {
            GameEvent collisionEvent = new GameEvent(EventType.PLAY_SOUND).add("file_path", collisionSoundPath);
            eventBus.publish(collisionEvent);
        }

        // Get resolution types for both objects
        CollisionResolution.ResolutionType type1 = resolutionTypes.get(obj1.getObjectId());
        CollisionResolution.ResolutionType type2 = resolutionTypes.get(obj2.getObjectId());

        // Use BOUNCE as default if not specified
        CollisionResolution.ResolutionType resolutionType =
            (type1 != null) ? type1 : CollisionResolution.ResolutionType.BOUNCE;

        // If both objects need to be resolved, use the first one's type
        // (You can implement more complex logic here if needed)

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
     * Creates a unique key for a collision pair.
     * Ensures the same key is generated regardless of order.
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
