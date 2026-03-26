package io.github.INF1009_P10_Team7.cyber;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Reads rectangle objects from a named Tiled object layer and resolves
 * circle-vs-rectangle collisions. Also builds a boolean wall grid from
 * the tile layer for line-of-sight checks used by DroneAI.
 *
 * Implements IMapCollision so DroneAI states no longer depend on TileMap.
 */
/**
 * TiledObjectCollisionManager — builds and queries tile-map collision geometry
 * from a Tiled (.tmx) map file.
 *
 * <p>Implements {@link IMapCollision} so drone AI states and the player can
 * call {@link #resolveCircleVsWalls} and {@link #hasLineOfSight} without
 * depending on TMX internals (DIP).</p>
 *
 * <p>On {@link #build}, wall tiles from the named layers are stored in a
 * boolean grid. Circle-vs-wall resolution sweeps the grid and pushes the
 * circle out of any overlapping wall tiles. Line-of-sight casting steps along
 * the ray and checks each grid cell.</p>
 *
 * <p>Also exposes {@link #getWallGrid()} for the minimap renderer to draw
 * wall tiles without coupling to the Tiled API.</p>
 */
public class TiledObjectCollisionManager implements IMapCollision {

    private final Array<Rectangle> walls = new Array<>();
    private boolean[][] wallGrid;          // [row][col], row 0 = top in Tiled coords

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    /**
     * Call once after TmxMapLoader has loaded the map.
     *
     * @param map            the loaded TiledMap
     * @param objectLayerName exact name of your collision object layer in Tiled
     * @param wallLayerName   exact name of the tile layer whose non-empty cells are walls
     */
    public void build(TiledMap map, String objectLayerName, String wallLayerName) {
        buildObjectWalls(map, objectLayerName);
        buildWallGrid(map, wallLayerName);
    }

    /** Convenience overload using the default layer names. */
    public void build(TiledMap map) {
        build(map, "collision", "Walls");
    }

    private void buildObjectWalls(TiledMap map, String layerName) {
        walls.clear();
        if (map.getLayers().get(layerName) == null) {
            Gdx.app.log("TiledObjectCollisionManager", "Object layer not found: " + layerName);
            return;
        }
        for (MapObject obj : map.getLayers().get(layerName).getObjects()) {
            if (obj instanceof RectangleMapObject) {
                walls.add(((RectangleMapObject) obj).getRectangle());
            }
        }
        Gdx.app.log("TiledObjectCollisionManager", "Loaded " + walls.size + " collision rects from layer: " + layerName);
    }

    private void buildWallGrid(TiledMap map, String layerName) {
        wallGrid = new boolean[TileMap.ROWS][TileMap.COLS];
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("TiledObjectCollisionManager", "Tile layer not found: " + layerName);
            return;
        }
        for (int row = 0; row < TileMap.ROWS; row++) {
            for (int col = 0; col < TileMap.COLS; col++) {
                // libGDX tile rows are bottom-up; Tiled rows are top-down
                wallGrid[row][col] = layer.getCell(col, TileMap.ROWS - 1 - row) != null;
            }
        }
    }

    // -------------------------------------------------------------------------
    // IMapCollision
    // -------------------------------------------------------------------------

    /**
     * Pushes a circle out of any overlapping wall rectangles.
     */
    @Override
    public float[] resolveCircleVsWalls(float px, float py, float r) {
        for (Rectangle wall : walls) {
            float nearX = Math.max(wall.x, Math.min(px, wall.x + wall.width));
            float nearY = Math.max(wall.y, Math.min(py, wall.y + wall.height));
            float dx = px - nearX;
            float dy = py - nearY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 0 && dist < r) {
                float push = r - dist;
                px += (dx / dist) * push;
                py += (dy / dist) * push;
            } else if (dist == 0) {
                py += r;
            }
        }
        return new float[]{ px, py };
    }

    /**
     * Ray-marches through the wall grid to check line of sight.
     */
    @Override
    public boolean hasLineOfSight(float x1, float y1, float x2, float y2) {
        if (wallGrid == null) return true;
        float dx = x2 - x1, dy = y2 - y1;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d < 1f) return true;
        float step = TileMap.TILE_SIZE * 0.5f;
        float sx = dx / d * step, sy = dy / d * step;
        int steps = (int) (d / step);
        float cx = x1, cy = y1;
        for (int i = 0; i < steps; i++) {
            cx += sx; cy += sy;
            if (isWall(TileMap.worldToCol(cx), TileMap.worldToRow(cy))) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Grid helpers (used by CyberGameScene for minimap / safe respawn)
    // -------------------------------------------------------------------------

    public boolean isWall(int col, int row) {
        if (col < 0 || col >= TileMap.COLS || row < 0 || row >= TileMap.ROWS) return true;
        return wallGrid != null && wallGrid[row][col];
    }

    /** Returns the raw wall grid for minimap rendering. May be null before build(). */
    public boolean[][] getWallGrid() {
        return wallGrid;
    }
}
