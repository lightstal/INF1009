package io.github.INF1009_P10_Team7.simulation.cyber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * TileMap — 40×22 grid (1280×704 @ 32 px tiles).
 *
 * Three independent levels, each with 5 distinct colour-coded rooms and
 * 5 terminals.  Rooms are connected by 2-tile-wide corridors so navigation
 * is obvious.  The Tech Dungeon tileset (tileset.png, 32 px) is used for
 * wall sprites; a graceful ShapeRenderer fallback covers missing assets.
 *
 * Tile legend
 *  0  FLOOR   — generic corridor (dark steel)
 *  1  WALL    — solid, impassable (tileset sprite)
 *  2  TERMINAL — hackable terminal (green CRT glow)
 *  5  EXIT    — locked/unlocked door
 *  7  ROOM_A  — deep navy blue
 *  8  ROOM_B  — dark teal
 *  9  ROOM_C  — dark emerald
 * 10  ROOM_D  — amber
 * 11  ROOM_E  — deep violet
 */
public class TileMap {

    public static final int   COLS      = 40;
    public static final int   ROWS      = 22;
    public static final int   TILE_SIZE = 32;
    public static final float WORLD_W   = COLS * TILE_SIZE;   // 1280
    public static final float WORLD_H   = ROWS * TILE_SIZE;   // 704

    // Floor palette: index 0=corridor, 1-5=rooms A-E
    private static final float[][] FLOOR_RGB = {
        { 0.06f, 0.07f, 0.10f },   // 0 corridor  – dark steel
        { 0.06f, 0.08f, 0.20f },   // 1 Room A    – deep navy
        { 0.04f, 0.14f, 0.16f },   // 2 Room B    – dark teal
        { 0.05f, 0.14f, 0.08f },   // 3 Room C    – dark emerald
        { 0.17f, 0.10f, 0.03f },   // 4 Room D    – dark amber
        { 0.12f, 0.05f, 0.17f },   // 5 Room E    – deep violet
    };

    // ── Level 1 – INITIATION: Star layout (5 rooms around center hub) ──────
    private static final int[][] MAP_1 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,7,7,7,7,7,5,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,2,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,7,7,7,7,0,0,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,8,1,9,9,9,9,9,0,0,9,9,9,9,9,1,10,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,8,1,9,9,9,9,9,9,9,9,9,9,9,9,1,10,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,8,1,9,9,9,9,9,9,2,9,9,9,9,9,1,10,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,2,8,8,8,8,8,0,0,0,9,9,9,9,9,9,9,9,9,9,0,0,0,10,10,10,10,2,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,0,0,0,9,9,9,9,9,9,9,9,9,9,0,0,0,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,8,1,9,9,9,9,9,9,9,9,9,9,9,9,1,10,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,8,8,8,8,8,8,8,8,8,8,8,1,9,9,9,9,9,0,0,9,9,9,9,9,1,10,10,10,10,10,10,10,10,10,10,10,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,0,0,11,11,11,11,11,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,2,11,11,11,11,11,11,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    // ── Level 2 – INFILTRATION: Z-shape layout ─────────────────────────────
    private static final int[][] MAP_2 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,5,8,1,1},
        {1,1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,1,1},
        {1,1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,1,1},
        {1,1,7,7,7,7,7,2,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,2,8,8,8,8,8,8,1,1},
        {1,1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,1,1},
        {1,1,7,7,7,7,7,7,0,0,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,0,0,8,8,8,8,8,8,8,8,8,1,1},
        {1,1,7,7,7,7,7,7,0,0,7,7,7,7,7,1,1,1,1,1,1,1,1,8,8,8,8,0,0,8,8,8,8,8,8,8,8,8,1,1},
        {1,1,1,1,1,1,1,1,0,0,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,0,0,9,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,2,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,0,0,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,0,0,9,1,1,1,1,1,1,1,1,1,1},
        {1,1,10,10,10,10,10,10,0,0,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,0,0,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,2,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,2,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    // ── Level 3 – BREACH: 4-corners + central vault ─────────────────────────
    private static final int[][] MAP_3 = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,2,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,8,8,8,8,8,8,2,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,0,0,0,1,1,1,1,1,1,1,1,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,0,0,0,1,1,1,1,1,1,1,1,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,1,1,1,1,1,1,1,1,1,1,1,8,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,7,7,7,7,7,7,7,7,7,7,7,7,7,1,9,9,9,9,5,9,9,9,9,9,1,8,8,8,8,8,8,8,8,8,8,8,8,8,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,9,9,9,9,2,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,9,9,9,9,9,9,9,9,9,9,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,10,10,10,10,10,10,0,0,0,0,0,0,0,0,0,9,9,9,9,9,9,9,9,0,0,0,0,0,0,0,0,0,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,2,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,2,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1},
        {1,10,10,10,10,10,10,10,10,10,10,10,10,10,1,1,1,1,1,1,1,1,1,1,1,1,11,11,11,11,11,11,11,11,11,11,11,11,11,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    private static final int[][][] ALL_MAPS = { MAP_1, MAP_2, MAP_3 };

    private final int[][] map;
    private float stateTime = 0f;

    private Texture       tilesetTex;
    private TextureRegion wallTile;
    private TextureRegion wallCapTile;

    public TileMap(int level) {
        int idx = Math.max(0, Math.min(level - 1, ALL_MAPS.length - 1));
        map = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) map[r] = ALL_MAPS[idx][r].clone();
    }

    public void loadTileset() {
        try {
            tilesetTex = new Texture(Gdx.files.internal("tileset.png"));
            tilesetTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            int ts = TILE_SIZE;
            wallTile    = new TextureRegion(tilesetTex, 8 * ts, 1 * ts, ts, ts);
            wallCapTile = new TextureRegion(tilesetTex, 8 * ts, 0 * ts, ts, ts);
        } catch (Exception e) {
            Gdx.app.error("TileMap", "Tileset load failed: " + e.getMessage());
        }
    }

    public void disposeTileset() {
        if (tilesetTex != null) { tilesetTex.dispose(); tilesetTex = null; }
    }

    public static float tileLeft   (int col) { return col * TILE_SIZE; }
    public static float tileBottom (int row) { return (ROWS - 1 - row) * TILE_SIZE; }
    public static float tileCentreX(int col) { return col * TILE_SIZE + TILE_SIZE * 0.5f; }
    public static float tileCentreY(int row) { return (ROWS - 1 - row) * TILE_SIZE + TILE_SIZE * 0.5f; }
    public static int worldToCol(float wx) { return Math.max(0, Math.min(COLS-1, (int)(wx/TILE_SIZE))); }
    public static int worldToRow(float wy) { return Math.max(0, Math.min(ROWS-1, ROWS-1-(int)(wy/TILE_SIZE))); }

    public TileType getType(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return TileType.WALL;
        return TileType.fromId(map[row][col]);
    }
    public boolean isWall(int col, int row) { return getType(col, row).isSolid(); }
    public void setTile(int col, int row, TileType type) {
        if (col >= 0 && col < COLS && row >= 0 && row < ROWS) map[row][col] = type.id;
    }

    public boolean hasLineOfSight(float x1, float y1, float x2, float y2) {
        float dx = x2-x1, dy = y2-y1;
        float d  = (float)Math.sqrt(dx*dx+dy*dy);
        if (d < 1f) return true;
        float step = TILE_SIZE * 0.5f, sx = dx/d*step, sy = dy/d*step;
        int steps = (int)(d/step);
        float cx = x1, cy = y1;
        for (int i = 0; i < steps; i++) {
            cx += sx; cy += sy;
            if (isWall(worldToCol(cx), worldToRow(cy))) return false;
        }
        return true;
    }

    public float[] resolveCircleVsWalls(float px, float py, float r) {
        int minC = Math.max(0,(int)((px-r)/TILE_SIZE));
        int maxC = Math.min(COLS-1,(int)((px+r)/TILE_SIZE));
        int minR = Math.max(0, ROWS-1-(int)((py+r)/TILE_SIZE));
        int maxR = Math.min(ROWS-1, ROWS-1-(int)((py-r)/TILE_SIZE));
        for (int row=minR; row<=maxR; row++) {
            for (int col=minC; col<=maxC; col++) {
                if (!isWall(col,row)) continue;
                float tl=tileLeft(col), tr=tl+TILE_SIZE;
                float tb=tileBottom(row), tt=tb+TILE_SIZE;
                float nx=Math.max(tl,Math.min(px,tr)), ny=Math.max(tb,Math.min(py,tt));
                float ddx=px-nx, ddy=py-ny;
                float d=(float)Math.sqrt(ddx*ddx+ddy*ddy);
                if (d>0 && d<r){ float push=r-d; px+=(ddx/d)*push; py+=(ddy/d)*push; }
            }
        }
        return new float[]{px,py};
    }

    public void update(float dt) { stateTime += dt; }

    public void render(ShapeRenderer sr, SpriteBatch batch, boolean exitUnlocked) {
        float ts = TILE_SIZE;

        // ── Pass 1: floor fills ─────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.04f, 0.05f, 0.08f, 1f);
        sr.rect(0, 0, WORLD_W, WORLD_H);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                TileType t = getType(col, row);
                if (t.isSolid() || t.isTerminal() || t == TileType.EXIT) continue;
                float x = tileLeft(col), y = tileBottom(row);
                int ri = t.roomIndex();
                float[] rgb = FLOOR_RGB[ri];
                sr.setColor(rgb[0], rgb[1], rgb[2], 1f);
                sr.rect(x, y, ts, ts);
            }
        }

        // ── Terminals (animated CRT screens) ───────────────────────────
        float pulse = 0.6f + 0.4f * (float) Math.sin(stateTime * 3f);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!getType(col, row).isTerminal()) continue;
                float x = tileLeft(col), y = tileBottom(row);
                // Outer glow halo
                sr.setColor(0f, pulse * 0.35f, 0.1f, 0.7f);
                sr.rect(x-3, y-3, ts+6, ts+6);
                // Screen body
                sr.setColor(0.03f, 0.07f, 0.04f, 1f); sr.rect(x, y, ts, ts);
                // Screen bezel
                sr.setColor(0.02f, 0.04f, 0.02f, 1f); sr.rect(x+3, y+3, ts-6, ts-6);
                // Screen glow fill
                sr.setColor(0f, pulse * 0.85f, 0.22f, 1f);
                sr.rect(x+5, y+5, ts-10, ts-10);
                // Scan line
                int scan = ((int)(stateTime * 28f)) % (int)(ts - 14);
                sr.setColor(0.3f, 1f, 0.55f, 0.9f);
                sr.rect(x+5, y+5+scan, ts-10, 2);
                // Corner pixels
                sr.setColor(0f, 1f, 0.5f, 0.9f);
                sr.rect(x+5, y+6, 4, 2); sr.rect(x+ts-9, y+6, 4, 2);
            }
        }

        // ── Exit door ───────────────────────────────────────────────────
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (getType(col, row) != TileType.EXIT) continue;
                float x = tileLeft(col), y = tileBottom(row);
                if (exitUnlocked) {
                    float ep = 0.5f + 0.5f * (float)Math.sin(stateTime * 5f);
                    sr.setColor(ep*0.8f, 0f, ep, 0.9f); sr.rect(x-4, y-4, ts+8, ts+8);
                    sr.setColor(0.45f, 0f, 0.65f, 1f);  sr.rect(x, y, ts, ts);
                    sr.setColor(0.7f+0.3f*ep, 0.15f*ep, 1f, 1f); sr.rect(x+4, y+4, ts-8, ts-8);
                    float cx2=x+ts/2f, cy2=y+ts/2f;
                    sr.setColor(1f, 0.6f*ep, 1f, 1f);
                    sr.triangle(cx2, cy2+8f, cx2-7f, cy2, cx2+7f, cy2);
                    sr.triangle(cx2, cy2-8f, cx2-7f, cy2, cx2+7f, cy2);
                } else {
                    sr.setColor(0.10f, 0.02f, 0.02f, 1f); sr.rect(x, y, ts, ts);
                    sr.setColor(0.25f, 0.05f, 0.05f, 1f); sr.rect(x+4, y+4, ts-8, ts-8);
                    sr.setColor(0.45f, 0.08f, 0.08f, 1f);
                    sr.rect(x+11, y+18, 10, 5); sr.rect(x+11, y+23, 4, 5); sr.rect(x+17, y+23, 4, 5);
                }
            }
        }
        sr.end();

        // ── Pass 2: wall sprites from tileset ───────────────────────────
        if (wallTile != null) {
            batch.begin();
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (!isWall(col, row)) continue;
                    float x = tileLeft(col), y = tileBottom(row);
                    // Cap tile faces the player when there's walkable floor below
                    boolean floorBelow = row+1 < ROWS && !isWall(col, row+1);
                    batch.draw(floorBelow ? wallCapTile : wallTile, x, y, ts, ts);
                }
            }
            batch.end();
        } else {
            // Fallback coloured wall
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (!isWall(col, row)) continue;
                    float x = tileLeft(col), y = tileBottom(row);
                    sr.setColor(0.22f, 0.28f, 0.36f, 1f); sr.rect(x, y, ts, ts);
                    sr.setColor(0.28f, 0.36f, 0.46f, 1f); sr.rect(x+2, y+2, ts-4, ts-4);
                }
            }
            sr.end();
        }

        // ── Wall edge accent lines (crisp, no scanlines) ────────────────
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.28f, 0.48f, 0.60f, 0.45f);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!isWall(col, row)) continue;
                float x = tileLeft(col), y = tileBottom(row);
                if (row > 0       && !isWall(col, row-1)) sr.line(x, y+ts, x+ts, y+ts);
                if (col > 0       && !isWall(col-1, row)) sr.line(x, y, x, y+ts);
                if (col < COLS-1  && !isWall(col+1, row)) sr.line(x+ts, y, x+ts, y+ts);
                if (row < ROWS-1  && !isWall(col, row+1)) sr.line(x, y, x+ts, y);
            }
        }
        sr.end();
    }
}
