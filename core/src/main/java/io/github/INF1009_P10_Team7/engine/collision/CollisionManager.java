package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;

/**
 * Concrete collision management implementation.
 *
 * Demonstrates all 4 OOP pillars:
 *
 * Encapsulation — all fields are private; internal state (active collisions,
 *   object lists, response map) is hidden behind the ICollisionSystem interface.
 *
 * Abstraction — implements ICollisionSystem so callers (GameScene, GameEngine)
 *   depend on the interface, not this concrete class.
 *
 * Inheritance — CollisionDetection is an instance (not static), so a subclass
 *   (e.g. AABBCollisionDetection) can be injected via setDetector() to change
 *   detection behaviour without modifying this class.
 *
 * Polymorphism — collision responses are ICollisionResponse objects (Strategy
 *   Pattern). Each registered collidable carries its own response, and resolution
 *   is a polymorphic resolve() call — no switch/if-else on type.
 */
public class CollisionManager implements ICollisionSystem {

    private final List<ICollidable> collidableObjects;
    private final Map<String, ICollisionResponse> responses;
    private final Set<String> activeCollisions;
    private final IAudioController audioController;

    private CollisionDetection detector;

    private String collisionSoundPath = null;
    private boolean playSoundOnCollision = false;

    public CollisionManager(IAudioController audioController) {
        this.collidableObjects = new ArrayList<>();
        this.responses = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.audioController = audioController;
        this.detector = new CollisionDetection(); // Default: circle-circle
    }

    /**
     * Allows swapping the detection algorithm at runtime (Inheritance + Polymorphism).
     * For example, pass in an AABBCollisionDetection subclass for box-based detection.
     */
    public void setDetector(CollisionDetection detector) {
        this.detector = detector;
    }

    @Override
    public void registerCollidable(ICollidable collidable, ICollisionResponse response) {
        if (!collidableObjects.contains(collidable)) {
            collidableObjects.add(collidable);
            responses.put(collidable.getObjectId(), response);
            Gdx.app.log("CollisionManager", "Registered collidable: " + collidable.getObjectId());
        }
    }

    @Override
    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        responses.remove(collidable.getObjectId());
    }

    @Override
    public void setCollisionSound(String soundPath) {
        this.collisionSoundPath = soundPath;
        this.playSoundOnCollision = true;
    }

    @Override
    public void update(float deltaTime) {
        Set<String> currentCollisions = new HashSet<>();

        for (int i = 0; i < collidableObjects.size(); i++) {
            ICollidable obj1 = collidableObjects.get(i);
            if (obj1 == null || !obj1.isCollidable()) continue;

            for (int j = i + 1; j < collidableObjects.size(); j++) {
                ICollidable obj2 = collidableObjects.get(j);
                if (obj2 == null || !obj2.isCollidable()) continue;

                // Uses instance method — can be overridden by subclass (Inheritance)
                CollisionInfo collisionInfo = detector.getCollisionInfo(obj1, obj2);

                if (collisionInfo != null) {
                    String key = getCollisionKey(obj1.getObjectId(), obj2.getObjectId());
                    currentCollisions.add(key);

                    if (!activeCollisions.contains(key)) {
                        onCollision(obj1, obj2, collisionInfo);
                    }
                }
            }
        }

        activeCollisions.clear();
        activeCollisions.addAll(currentCollisions);
    }

    /**
     * Resolves a collision using the registered ICollisionResponse strategies.
     *
     * Priority logic (Polymorphism — no switch statement):
     * - If either object has PASS_THROUGH, use PASS_THROUGH.
     * - If either object has DESTROY, use DESTROY.
     * - Otherwise use the first object's response (typically BOUNCE).
     */
    @Override
    public void onCollision(ICollidable obj1, ICollidable obj2, CollisionInfo collisionInfo) {
        Gdx.app.log("Collision", "Collision detected: " + obj1.getObjectId() +
            " <-> " + obj2.getObjectId());

        if (playSoundOnCollision && collisionSoundPath != null && audioController != null) {
            audioController.playSound(collisionSoundPath);
        }

        ICollisionResponse r1 = responses.get(obj1.getObjectId());
        ICollisionResponse r2 = responses.get(obj2.getObjectId());

        // Pick the response using priority rules (Polymorphism)
        ICollisionResponse response = pickResponse(r1, r2);
        response.resolve(obj1, obj2, collisionInfo);
    }

    /**
     * Determines which response to use when two collidables collide.
     * PASS_THROUGH wins over DESTROY, which wins over everything else.
     */
    private ICollisionResponse pickResponse(ICollisionResponse r1, ICollisionResponse r2) {
        // PASS_THROUGH takes highest priority
        if (r1 == CollisionResolution.PASS_THROUGH || r2 == CollisionResolution.PASS_THROUGH) {
            return CollisionResolution.PASS_THROUGH;
        }
        // DESTROY takes second priority
        if (r1 == CollisionResolution.DESTROY || r2 == CollisionResolution.DESTROY) {
            return CollisionResolution.DESTROY;
        }
        // Default: use obj1's response (e.g. BOUNCE)
        return r1 != null ? r1 : CollisionResolution.BOUNCE;
    }

    private String getCollisionKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + ":" + id2;
        } else {
            return id2 + ":" + id1;
        }
    }

    @Override
    public void clear() {
        collidableObjects.clear();
        responses.clear();
        activeCollisions.clear();
        Gdx.app.log("CollisionManager", "All collidable objects cleared");
    }

    @Override
    public int getCollidableCount() {
        return collidableObjects.size();
    }
}
