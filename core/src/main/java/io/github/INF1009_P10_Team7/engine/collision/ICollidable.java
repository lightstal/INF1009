package io.github.INF1009_P10_Team7.engine.collision;

import io.github.INF1009_P10_Team7.engine.utils.Vector2;


public interface ICollidable {


    Vector2 getPosition();


    float getCollisionRadius();


    String getObjectId();


    boolean isCollidable();


    boolean isMovable();


    Vector2 getVelocity();


    void deactivate();
}

