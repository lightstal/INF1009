package io.github.INF1009_P10_Team7.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;


public class CollisionManager implements ICollisionSystem {

    private final List<ICollidable> collidableObjects;
    private final Map<String, CollisionResolution.ResolutionType> resolutionTypes;
    private final Set<String> activeCollisions;
    private final IAudioController audioController;

    private String collisionSoundPath = null;
    private boolean playSoundOnCollision = false;

    public CollisionManager(IAudioController audioController) {
        this.collidableObjects = new ArrayList<>();
        this.resolutionTypes = new HashMap<>();
        this.activeCollisions = new HashSet<>();
        this.audioController = audioController;
    }

    @Override
    public void registerCollidable(ICollidable collidable, CollisionResolution.ResolutionType resolutionType) {
        if (!collidableObjects.contains(collidable)) {
            collidableObjects.add(collidable);
            resolutionTypes.put(collidable.getObjectId(), resolutionType);
            Gdx.app.log("CollisionManager", "Registered collidable: " + collidable.getObjectId() +
                " with resolution type: " + resolutionType);
        }
    }

    @Override
    public void unregisterCollidable(ICollidable collidable) {
        collidableObjects.remove(collidable);
        resolutionTypes.remove(collidable.getObjectId());
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

                CollisionInfo collisionInfo =
                    CollisionDetection.getCollisionInfo(obj1, obj2);

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

    @Override
    public void onCollision(ICollidable obj1, ICollidable obj2, CollisionInfo collisionInfo) {
        Gdx.app.log("Collision", "Collision detected: " + obj1.getObjectId() +
            " <-> " + obj2.getObjectId());

        if (playSoundOnCollision && collisionSoundPath != null && audioController != null) {
            audioController.playSound(collisionSoundPath);
        }

        CollisionResolution.ResolutionType type1 = resolutionTypes.get(obj1.getObjectId());
        CollisionResolution.ResolutionType type2 = resolutionTypes.get(obj2.getObjectId());

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

        CollisionResolution.resolve(obj1, obj2, collisionInfo, resolutionType);
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
        resolutionTypes.clear();
        activeCollisions.clear();
        Gdx.app.log("CollisionManager", "All collidable objects cleared");
    }

    @Override
    public int getCollidableCount() {
        return collidableObjects.size();
    }
}
