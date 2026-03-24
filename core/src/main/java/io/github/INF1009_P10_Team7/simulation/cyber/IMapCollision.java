package io.github.INF1009_P10_Team7.simulation.cyber;

/**
 * Minimal interface used by DroneAI states for wall collision and line-of-sight.
 * Implemented by TiledObjectCollisionManager so drone code no longer depends
 * on the hardcoded TileMap class.
 */
public interface IMapCollision {
    float[] resolveCircleVsWalls(float px, float py, float radius);
    boolean hasLineOfSight(float x1, float y1, float x2, float y2);
}
