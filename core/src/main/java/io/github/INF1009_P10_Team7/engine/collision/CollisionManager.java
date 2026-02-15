package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

/**
 * <p>Handles collision detection and resolution for all registered
 * collidable objects. Implements {@link ICollisionSystem}.</p>
 *
 * <p>The detection strategy can be swapped at runtime by passing in
 * a different detector, and each collidable can have its own
 * response behaviour.</p>
 */
public class CollisionManager implements ICollisionSystem {

    private final List<ICollidable> collidableObjects;
    private final Map<String, ICollisionResponse> responses;
    private final Set<String> activeCollisions;

    private CollisionDetection detector;

    /** <p>Creates a new CollisionManager with the default detection strategy.</p> */
    public CollisionManager() {
        this.collidableObjects = new ArrayList<>();
        this.responses = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.detector = new CollisionDetection();
    }

    /**
     * <p>Swaps the detection algorithm at runtime.
     * E.g., passing in an AABBCollisionDetection subclass
     * for box-based detection.</p>
     *
     * @param detector the new detection strategy
     */
    public void setDetector(CollisionDetection detector) {
        this.detector = detector;
    }

    /**
     * <p>Registers a collidable with its response. Duplicates are ignored.</p>
     *
     * @param collidable the object to register
     * @param response   the response strategy for this object
     */
    @Override
    public void registerCollidable(ICollidable collidable, ICollisionResponse response) {
        if (!collidableObjects.contains(collidable)) {
            collidableObjects.add(collidable);
            responses.put(collidable.getObjectId(), response);
            Gdx.app.log("CollisionManager", "Registered collidable: " + collidable.getObjectId());
        }
    }

    /**
     * <p>Removes a collidable from the system.</p>
     *
     * @param collidable the object to unregister
     */
    @Override
    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        responses.remove(collidable.getObjectId());
    }

    /**
     * <p>Runs collision checks each frame and resolves any new collisions.
     * Already-active collisions are tracked to avoid duplicates.</p>
     *
     * @param deltaTime time since last frame in seconds
     */
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
     * <p>Called when a collision is detected. Picks the right response
     * based on priority and resolves it.</p>
     *
     * @param obj1          first colliding object
     * @param obj2          second colliding object
     * @param collisionInfo details about the collision
     */
    public void onCollision(ICollidable obj1, ICollidable obj2, CollisionInfo collisionInfo) {
        Gdx.app.log("Collision", "Collision detected: " + obj1.getObjectId() +
            " <-> " + obj2.getObjectId());

        ICollisionResponse r1 = responses.get(obj1.getObjectId());
        ICollisionResponse r2 = responses.get(obj2.getObjectId());

        ICollisionResponse response = pickResponse(r1, r2);
        response.resolve(obj1, obj2, collisionInfo);
    }

    /**
     * <p>Picks which response to use. PASS_THROUGH has priority over
     * DESTROY, which follows how other game engine are designed</p>
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

    /** <p>Generates a consistent key for a collision pair regardless of order.</p> */
    private String getCollisionKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + ":" + id2;
        } else {
            return id2 + ":" + id1;
        }
    }

    /** <p>Removes all registered collidables and resets internal state.</p> */
    @Override
    public void clear() {
        collidableObjects.clear();
        responses.clear();
        activeCollisions.clear();
        Gdx.app.log("CollisionManager", "All collidable objects cleared");
    }

    /** @return the number of registered collidables */
    @Override
    public int getCollidableCount() {
        return collidableObjects.size();
    }
}
