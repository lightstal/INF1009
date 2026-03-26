package io.github.INF1009_P10_Team7.engine.map.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.map.ILevelMapRuntime;

/**
 * Engine-owned tiled level runtime implementation.
 */
public class TiledLevelMapRuntime implements ILevelMapRuntime {
    private final String mapFile;
    private final String collisionLayer;
    private final String wallLayer;
    private final String doorLayer;
    private final String terminalLayer;
    private final int cols;
    private final int rows;
    private final int tileSize;

    private TiledMap tmxMap;
    private OrthogonalTiledMapRenderer tmxRenderer;
    private TiledWorldCollisionQuery collisionMgr;
    private int[][] terminalTiles = new int[0][];
    private float exitX;
    private float exitY;
    private TextureRegion doorClosedRegion;
    private TextureRegion doorOpenedRegion;

    public TiledLevelMapRuntime(
        String mapFile,
        String collisionLayer,
        String wallLayer,
        String doorLayer,
        String terminalLayer,
        int cols,
        int rows,
        int tileSize
    ) {
        this.mapFile = mapFile;
        this.collisionLayer = collisionLayer;
        this.wallLayer = wallLayer;
        this.doorLayer = doorLayer;
        this.terminalLayer = terminalLayer;
        this.cols = cols;
        this.rows = rows;
        this.tileSize = tileSize;
    }

    @Override
    public void load() {
        tmxMap = new TmxMapLoader().load(mapFile);
        tmxRenderer = new OrthogonalTiledMapRenderer(tmxMap);
        collisionMgr = new TiledWorldCollisionQuery(cols, rows, tileSize);
        collisionMgr.build(tmxMap, collisionLayer, wallLayer);
        terminalTiles = loadTerminalsFromTmx(terminalLayer);
        loadDoorFromTmx(doorLayer);
    }

    private int[][] loadTerminalsFromTmx(String layerName) {
        if (tmxMap == null) return new int[0][];
        MapLayer layer = tmxMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("TiledLevelMapRuntime", "Terminal layer '" + layerName + "' not found in TMX");
            return new int[0][];
        }
        java.util.List<int[]> list = new java.util.ArrayList<>();
        for (MapObject obj : layer.getObjects()) {
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            int col = (int) (x / tileSize);
            int row = rows - 1 - (int) (y / tileSize);
            list.add(new int[]{col, row});
        }
        return list.toArray(new int[0][]);
    }

    private void loadDoorFromTmx(String layerName) {
        if (tmxMap == null) return;
        MapLayer layer = tmxMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("TiledLevelMapRuntime", "Door layer '" + layerName + "' not found in TMX");
            return;
        }
        for (MapObject obj : layer.getObjects()) {
            if ("closed door".equals(obj.getName())) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                int col = (int) (x / tileSize);
                int row = rows - 1 - (int) (y / tileSize);
                exitX = tileCentreX(col);
                exitY = tileCentreY(row);
            }
        }
        TiledMapTileSet doorSet = tmxMap.getTileSets().getTileSet("doors");
        if (doorSet != null) {
            TiledMapTile closed = doorSet.getTile(853);
            TiledMapTile opened = doorSet.getTile(854);
            if (closed != null) doorClosedRegion = closed.getTextureRegion();
            if (opened != null) doorOpenedRegion = opened.getTextureRegion();
        }
    }

    @Override
    public IWorldCollisionQuery getCollisionQuery() {
        return collisionMgr;
    }

    @Override
    public int[][] getTerminalTiles() {
        return terminalTiles;
    }

    @Override
    public float getExitX() {
        return exitX;
    }

    @Override
    public float getExitY() {
        return exitY;
    }

    @Override
    public TextureRegion getDoorClosedRegion() {
        return doorClosedRegion;
    }

    @Override
    public TextureRegion getDoorOpenedRegion() {
        return doorOpenedRegion;
    }

    @Override
    public void render(OrthographicCamera camera) {
        if (tmxRenderer == null) return;
        tmxRenderer.setView(camera);
        tmxRenderer.render();
    }

    @Override
    public void dispose() {
        if (tmxRenderer != null) tmxRenderer.dispose();
        if (tmxMap != null) tmxMap.dispose();
    }

    private float tileCentreX(int col) {
        return col * tileSize + tileSize * 0.5f;
    }

    private float tileCentreY(int row) {
        return (rows - 1 - row) * tileSize + tileSize * 0.5f;
    }
}
