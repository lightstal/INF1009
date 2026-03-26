package io.github.INF1009_P10_Team7.engine.collision;

/**
 * Query-only world collision contract exposed by the engine.
 *
 * <p>Gameplay code should depend on this interface instead of a concrete
 * map or framework-specific collision manager.</p>
 */
public interface IWorldCollisionQuery {

    /**
     * Resolves a circle at {@code (cx, cy)} against static world walls.
     *
     * @param cx desired center X
     * @param cy desired center Y
     * @param radius circle radius
     * @return resolved world position as {@code [x, y]}
     */
    float[] resolveCircleVsWalls(float cx, float cy, float radius);

    /**
     * Checks whether a clear line exists between two world points.
     *
     * @return {@code true} if no wall blocks the segment
     */
    boolean hasLineOfSight(float x1, float y1, float x2, float y2);

    /**
     * Tile-level wall query.
     *
     * @return {@code true} when this tile is blocked
     */
    boolean isWall(int col, int row);

    /**
     * Raw wall grid where indices are {@code [row][col]}.
     */
    boolean[][] getWallGrid();
}
