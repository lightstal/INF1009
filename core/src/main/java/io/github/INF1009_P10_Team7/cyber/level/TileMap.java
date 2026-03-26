package io.github.INF1009_P10_Team7.cyber.level;

/**
 * TileMap - static coordinate utilities for the 40x22 world grid.
 *
 * All level maps are now defined in Tiled (.tmx) files.
 * This class only provides the shared constants and helper methods
 * used throughout the game to convert between tile and world space.
 */
public final class TileMap {

    public static final int   COLS      = 40;
    public static final int   ROWS      = 22;
    public static final int   TILE_SIZE = 32;
    public static final float WORLD_W   = COLS * TILE_SIZE;   // 1280
    public static final float WORLD_H   = ROWS * TILE_SIZE;   // 704

    private TileMap() {}   // utility class – no instances

    /** @return the left edge (world X) of tile column {@code col} */
    public static float tileLeft   (int col) { return col * TILE_SIZE; }
    /** @return the bottom edge (world Y) of tile row {@code row} (row 0 = top of map) */
    public static float tileBottom (int row) { return (ROWS - 1 - row) * TILE_SIZE; }
    /** @return the world X coordinate of the centre of tile column {@code col} */
    public static float tileCentreX(int col) { return col * TILE_SIZE + TILE_SIZE * 0.5f; }
    /** @return the world Y coordinate of the centre of tile row {@code row} */
    public static float tileCentreY(int row) { return (ROWS - 1 - row) * TILE_SIZE + TILE_SIZE * 0.5f; }
    /** @return the tile column index for world X {@code wx}, clamped to valid range */
    public static int   worldToCol (float wx) { return Math.max(0, Math.min(COLS - 1, (int)(wx / TILE_SIZE))); }
    /** @return the tile row index for world Y {@code wy}, clamped to valid range */
    public static int   worldToRow (float wy) { return Math.max(0, Math.min(ROWS - 1, ROWS - 1 - (int)(wy / TILE_SIZE))); }
}
