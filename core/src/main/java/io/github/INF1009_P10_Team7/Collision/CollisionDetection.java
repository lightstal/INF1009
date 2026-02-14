package io.github.INF1009_P10_Team7.Collision;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

public class CollisionDetection {

    public static boolean isOverlapping(iCollidable a, iCollidable b) {
        if (a == null || b == null) return false;
        return a.getBounds().overlaps(b.getBounds());
    }

    public static Rectangle getOverlap(iCollidable a, iCollidable b) {
        Rectangle intersection = new Rectangle();
        Intersector.intersectRectangles(a.getBounds(), b.getBounds(), intersection);
        return intersection;
    }
}