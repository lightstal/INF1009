package io.github.INF1009_P10_Team7.Collision;

import com.badlogic.gdx.math.Rectangle;

public interface iCollidable {
    Rectangle getBounds();
    void onCollision(iCollidable other);
}