package io.github.INF1009_P10_Team7.simulation.cyber;

/**
 * IMapCollision — collision interface for tile-map geometry.
 *
 * <p>Abstracts the wall/collision layer of the TMX map so that the drone AI
 * states ({@link io.github.INF1009_P10_Team7.simulation.cyber.drone.PatrolState},
 * {@link io.github.INF1009_P10_Team7.simulation.cyber.drone.ChaseState},
 * {@link io.github.INF1009_P10_Team7.simulation.cyber.drone.SearchState}) can
 * query and resolve movement against walls without depending on the concrete
 * {@link TiledObjectCollisionManager} (DIP).</p>
 *
 * <p>Also used by {@code CyberGameScene} to resolve the player's position
 * against wall tiles each frame.</p>
 */
public interface IMapCollision {

    /**
     * Resolves a circle at {@code (cx, cy)} with the given {@code radius} against
     * all wall geometry. Returns the closest valid position that does not overlap
     * any wall tile.
     *
     * @param cx     desired centre X in world space
     * @param cy     desired centre Y in world space
     * @param radius circle radius in world units
     * @return a two-element {@code float[]} — {@code [resolvedX, resolvedY]}
     */
    float[] resolveCircleVsWalls(float cx, float cy, float radius);

    /**
     * Casts a ray between two world-space points and returns {@code true} if
     * no opaque wall tile blocks the line of sight.
     * Used by drone AI to determine if the player is visible.
     *
     * @param x1 ray start X
     * @param y1 ray start Y
     * @param x2 ray end X
     * @param y2 ray end Y
     * @return {@code true} if the path is clear (unobstructed)
     */
    boolean hasLineOfSight(float x1, float y1, float x2, float y2);
}
