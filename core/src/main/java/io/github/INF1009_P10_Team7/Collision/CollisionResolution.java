package io.github.INF1009_P10_Team7.Collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.INF1009_P10_Team7.Entity.Entity;

public class CollisionResolution {

    public static void resolve(Entity a, Entity b, CollisionType typeA, CollisionType typeB) {
        a.onCollision(b);
        b.onCollision(a);

        if (typeA == CollisionType.PASS_THROUGH || typeB == CollisionType.PASS_THROUGH) return;

        if (typeA == CollisionType.DESTROY) a.destroy();
        if (typeB == CollisionType.DESTROY) b.destroy();
        if (typeA == CollisionType.DESTROY || typeB == CollisionType.DESTROY) return;

        Rectangle overlap = CollisionDetection.getOverlap(a, b);
        boolean isXCollision = overlap.width < overlap.height;
        
        boolean aIsStatic = (typeA == CollisionType.STATIC);
        boolean bIsStatic = (typeB == CollisionType.STATIC);
        
        float weightA = aIsStatic ? 0f : (bIsStatic ? 1f : 0.5f);
        float weightB = bIsStatic ? 0f : (aIsStatic ? 1f : 0.5f);

        if (isXCollision) {
            float push = overlap.width;
            if (a.getPosition().x < b.getPosition().x) {
                a.move(-push * weightA, 0); 
                b.move(push * weightB, 0);
            } else {
                a.move(push * weightA, 0); 
                b.move(-push * weightB, 0);
            }

            // Reverse velocity for bouncing entities
            if (typeA == CollisionType.BOUNCE) a.getVelocity().x *= -1;
            if (typeB == CollisionType.BOUNCE) b.getVelocity().x *= -1;
            
        } else {
            float push = overlap.height;
            if (a.getPosition().y < b.getPosition().y) {
                a.move(0, -push * weightA); 
                b.move(0, push * weightB);
            } else {
                a.move(0, push * weightA); 
                b.move(0, -push * weightB);
            }

            // Reverse velocity for bouncing entities
            if (typeA == CollisionType.BOUNCE) a.getVelocity().y *= -1;
            if (typeB == CollisionType.BOUNCE) b.getVelocity().y *= -1;
        }
    }
    
    /**
     * Resolves collision between an entity and the screen/world boundaries.
     */
    public static void resolveBoundary(Entity e, CollisionType type, float worldWidth, float worldHeight) {
//        if (type == CollisionType.PASS_THROUGH || e.isExpired()) return;

        Rectangle bounds = e.getBounds();
        Vector2 vel = e.getVelocity();

        // Check Left Boundary
        if (bounds.x < 0) {
            e.move(-bounds.x, 0); // Push back to 0
            handleBoundaryPhysics(vel, type, true); // true = x axis
        } 
        // Check Right Boundary
        else if (bounds.x + bounds.width > worldWidth) {
            float overlap = (bounds.x + bounds.width) - worldWidth;
            e.move(-overlap, 0); // Push back inside
            handleBoundaryPhysics(vel, type, true);
        }

        // Check Bottom Boundary
        if (bounds.y < 0) {
            e.move(0, -bounds.y);
            handleBoundaryPhysics(vel, type, false); // false = y axis
        } 
        // Check Top Boundary
        else if (bounds.y + bounds.height > worldHeight) {
            float overlap = (bounds.y + bounds.height) - worldHeight;
            e.move(0, -overlap);
            handleBoundaryPhysics(vel, type, false);
        }

//        // If the type is DESTROY, mark for removal if it touched any edge
//        if (type == CollisionType.DESTROY && isOutOfBounds(bounds, worldWidth, worldHeight)) {
//            e.destroy();
//        }
    }

    private static void handleBoundaryPhysics(Vector2 vel, CollisionType type, boolean isX) {
        if (type == CollisionType.BOUNCE) {
            if (isX) vel.x *= -1; else vel.y *= -1;
        } else {
            if (isX) vel.x = 0; else vel.y = 0;
        }
    }

//    private static boolean isOutOfBounds(Rectangle b, float w, float h) {
//        return b.x <= 0 || b.y <= 0 || b.x + b.width >= w || b.y + b.height >= h;
//    }
}