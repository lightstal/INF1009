package io.github.INF1009_P10_Team7.engine.map.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;

/**
 * Engine-level tiled-map collision query implementation.
 */
public class TiledWorldCollisionQuery implements IWorldCollisionQuery {
    private final int cols;
    private final int rows;
    private final int tileSize;
    private final Array<Rectangle> walls = new Array<>();
    private boolean[][] wallGrid;

    public TiledWorldCollisionQuery(int cols, int rows, int tileSize) {
        this.cols = cols;
        this.rows = rows;
        this.tileSize = tileSize;
    }

    public void build(TiledMap map, String objectLayerName, String wallLayerName) {
        buildObjectWalls(map, objectLayerName);
        buildWallGrid(map, wallLayerName);
    }

    private void buildObjectWalls(TiledMap map, String layerName) {
        walls.clear();
        if (map.getLayers().get(layerName) == null) {
            Gdx.app.log("TiledWorldCollisionQuery", "Object layer not found: " + layerName);
            return;
        }
        for (MapObject obj : map.getLayers().get(layerName).getObjects()) {
            if (obj instanceof RectangleMapObject) {
                walls.add(((RectangleMapObject) obj).getRectangle());
            }
        }
        Gdx.app.log("TiledWorldCollisionQuery", "Loaded " + walls.size + " collision rects from layer: " + layerName);
    }

    private void buildWallGrid(TiledMap map, String layerName) {
        wallGrid = new boolean[rows][cols];
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("TiledWorldCollisionQuery", "Tile layer not found: " + layerName);
            return;
        }
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                wallGrid[row][col] = layer.getCell(col, rows - 1 - row) != null;
            }
        }
    }

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
        return new float[]{px, py};
    }

    @Override
    public boolean hasLineOfSight(float x1, float y1, float x2, float y2) {
        if (wallGrid == null) return true;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d < 1f) return true;
        float step = tileSize * 0.5f;
        float sx = dx / d * step;
        float sy = dy / d * step;
        int steps = (int) (d / step);
        float cx = x1;
        float cy = y1;
        for (int i = 0; i < steps; i++) {
            cx += sx;
            cy += sy;
            if (isWall(worldToCol(cx), worldToRow(cy))) return false;
        }
        return true;
    }

    @Override
    public boolean isWall(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return true;
        return wallGrid != null && wallGrid[row][col];
    }

    @Override
    public boolean[][] getWallGrid() {
        return wallGrid;
    }

    private int worldToCol(float wx) {
        return Math.max(0, Math.min(cols - 1, (int) (wx / tileSize)));
    }

    private int worldToRow(float wy) {
        return Math.max(0, Math.min(rows - 1, rows - 1 - (int) (wy / tileSize)));
    }
}
