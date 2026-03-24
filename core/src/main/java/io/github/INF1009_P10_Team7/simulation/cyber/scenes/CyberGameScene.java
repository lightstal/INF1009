package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.engine.collision.CollisionResolution;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.RenderComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TriangleRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.movement.InputDrivenMovement;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import io.github.INF1009_P10_Team7.simulation.cyber.CyberPlayerMovement;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSprites;
import io.github.INF1009_P10_Team7.simulation.cyber.PlayerInventory;
import io.github.INF1009_P10_Team7.simulation.cyber.PlayerState;
import io.github.INF1009_P10_Team7.simulation.cyber.ClueSystem;
import io.github.INF1009_P10_Team7.simulation.cyber.SpriteAnimator;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import io.github.INF1009_P10_Team7.simulation.cyber.IMapCollision;
import io.github.INF1009_P10_Team7.simulation.cyber.TiledObjectCollisionManager;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.SearchState;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.*;
import io.github.INF1009_P10_Team7.simulation.cyber.observer.GameEventSystem;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;
import io.github.INF1009_P10_Team7.simulation.cyber.LevelConfig;

/**
 * CyberGameScene — main gameplay scene for Levels 1 and 2.
 *
 * <p>Responsibilities: TMX map loading, player movement + collision,
 * drone AI updates, terminal/minigame management, intel clue system,
 * HUD rendering (status panel, minimap, threat indicator, banners),
 * checkpoint/respawn, CCTV detection, particle effects, and
 * scene-entry fade transition.</p>
 *
 * <p>Level-specific configuration is supplied via {@link LevelConfig}
 * (Strategy Pattern) so this class never needs modification when a
 * new level is added (OCP).</p>
 */
public class CyberGameScene extends Scene {

    private final IEntitySystem    entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem  movementSystem;
    private final CyberSceneFactory factory;
    private final LevelConfig config;

    private final CyberSprites sprites = new CyberSprites();
    private SpriteAnimator playerAnimator;

    private ShapeRenderer sr;
    private SpriteBatch   batch;
    private BitmapFont hudFont, hudSmallFont, hudPanelFont, alertFont, promptFont;
    private GlyphLayout layout;
    private CyberHudRenderer   hudRenderer;
    private CyberWorldRenderer worldRenderer;

    private static final float VIEW_W = 640f;
    private static final float VIEW_H = 352f;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private OrthographicCamera hudCamera;
    private Viewport           hudViewport;

    // TMX map support
    private TiledMap                   tmxMap;
    private OrthogonalTiledMapRenderer tmxRenderer;
    private TiledObjectCollisionManager collisionMgr;
    private float tmxExitX, tmxExitY;
    private TextureRegion doorClosedRegion, doorOpenedRegion;
    private float   stateTime  = 0f;
    private float   rotorAngle = 0f;

    private GameEntity playerEntity;
    private static final float PLAYER_RADIUS = 10f;

    private int[][]    terminalTiles;
    private boolean[]  terminalSolved;
    private IMiniGame[] challenges;
    private int        KEYS_REQUIRED;
    private DroneAI[]  drones;
    private int[]      playerStartTile;

    private final TerminalEmulator terminal    = new TerminalEmulator();
    private IMiniGame  activeChallenge         = null;
    private int        activeChallengeIdx      = -1;

    private final GameEventSystem eventSystem  = new GameEventSystem();
    private final PlayerInventory inventory    = new PlayerInventory();

    private boolean gameOver     = false;
    private boolean victory      = false;
    private boolean exitUnlocked = false;
    private int     keysCollected = 0;
    private float   timeRemaining;
    private float   missionElapsed = 0f;
    private float   chaseWarningTimer = 0f;
    private static final float DRONE_CONTACT_RADIUS_BONUS = 0.5f;

    private float checkpointX;
    private float checkpointY;
    private int maxRespawns;
    private int respawnsRemaining;
    private int respawnsUsed = 0;
    private float protectionTimer = 0f;
    private float terminalPingTimer = 0f;
    private float pingFxTimer = 0f;
    private static final float PING_FX_DURATION = 0.85f;
    private static final float PING_REVEAL_RADIUS = TileMap.TILE_SIZE * 9f;
    private float playerFacingAngle = 0f;
    private float cctvAlertCooldown = 0f;
    private static final float CCTV_ALERT_INTERVAL = 5f;
    private boolean[] cctvAlerted;
    private int signalPingsRemaining;
    private int hintsUsed = 0;
    private String bannerTitle = "";
    private String bannerSubtitle = "";
    private float bannerTimer = 0f;

    // ── Scene-entry fade transition ─────────────────────────────────────────
    private float transitionAlpha = 1f;  // starts black, fades in

    // ── Player state system ──────────────────────────────────────────────────
    private PlayerState playerState = PlayerState.IDLE;
    private final ClueSystem clueSystem = new ClueSystem();
    private float scanAnimTimer = 0f;

    // ── Particle effects ────────────────────────────────────────────────────
    private static final int MAX_PARTICLES = 64;
    private final float[] pX = new float[MAX_PARTICLES];
    private final float[] pY = new float[MAX_PARTICLES];
    private final float[] pVX = new float[MAX_PARTICLES];
    private final float[] pVY = new float[MAX_PARTICLES];
    private final float[] pLife = new float[MAX_PARTICLES];
    private final float[] pR = new float[MAX_PARTICLES];
    private final float[] pG = new float[MAX_PARTICLES];
    private final float[] pB = new float[MAX_PARTICLES];
    private int particleCount = 0;

    public CyberGameScene(IInputController input, IAudioController audio,
                          SceneNavigator nav,
                          IEntitySystem entitySystem,
                          ICollisionSystem collisionSystem,
                          IMovementSystem movementSystem,
                          CyberSceneFactory factory,
                          LevelConfig config) {
        super(input, audio, nav);
        this.entitySystem    = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem  = movementSystem;
        this.factory         = factory;
        this.config          = config;
    }

    // =========================================================================
    @Override
    protected void onLoad() {
        camera   = new OrthographicCamera();
        viewport = new StretchViewport(VIEW_W, VIEW_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();

        hudCamera   = new OrthographicCamera();
        hudViewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, hudCamera);
        hudViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hudCamera.position.set(TileMap.WORLD_W / 2f, TileMap.WORLD_H / 2f, 0);
        hudCamera.update();

        sr     = new ShapeRenderer();
        batch  = new SpriteBatch();
        layout = new GlyphLayout();

        hudFont        = makeBitmapFont(1.0f);
        hudSmallFont   = makeBitmapFont(0.48f);
        hudPanelFont   = makeBitmapFont(0.80f);
        alertFont      = makeBitmapFont(1.28f);
        promptFont     = makeBitmapFont(0.46f);

        tmxMap       = new TmxMapLoader().load(config.getMapFile());
        tmxRenderer  = new OrthogonalTiledMapRenderer(tmxMap);
        collisionMgr = new TiledObjectCollisionManager();
        collisionMgr.build(tmxMap, config.getCollisionLayer(), config.getWallLayer());
        loadDoorFromTmx(config.getDoorLayer());

        sprites.load();
        // HUD renderer receives shared font/batch/config references — no duplication (SRP)
        hudRenderer = new CyberHudRenderer(sr, batch,
            hudFont, hudSmallFont, hudPanelFont, alertFont, promptFont,
            layout, sprites, config);
        worldRenderer = new CyberWorldRenderer(sr, batch,
            sprites, input, hudSmallFont, promptFont, layout);
        playerAnimator = new SpriteAnimator("niceguy.png", 9, 4, 64, 64, 0.10f);
        eventSystem.addObserver(inventory);
        initLevelConfig();
        createPlayer();
        setupSupportSystems();
        audio.setMusic("audio/Music_Game.mp3");

        transitionAlpha = 1f;  // fade-in from black
        missionElapsed = 0f;
    }

    private BitmapFont makeBitmapFont(float scale) {
        return FontManager.create(scale);
    }

    private IMapCollision getMapCollision() {
        return collisionMgr;
    }

    private int[][] getLightPositions()  { return config.getLightPositions(); }
    private int[][] getCameraPositions() { return config.getCameraPositions(); }

    /** Returns the title of each challenge for HUD display. */
    private String[] buildChallengeTitles() {
        if (challenges == null) return new String[0];
        String[] titles = new String[challenges.length];
        for (int i = 0; i < challenges.length; i++) {
            titles[i] = challenges[i].getTitle();
        }
        return titles;
    }

    // =========================================================================
    // LEVEL CONFIG  -  reads terminal, drone, and challenge config from LevelConfig
    // =========================================================================

    /**
     * Reads terminal tile positions from a Tiled object layer.
     * Tile objects in TMX store y as the bottom of the tile in LibGDX world space (y-up).
     * Conversion: col = x/TILE_SIZE,  row = ROWS-1 - y/TILE_SIZE  (row 0 = top of map).
     */
    private int[][] loadTerminalsFromTmx(String layerName) {
        if (tmxMap == null) return new int[0][];
        MapLayer layer = tmxMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("CyberGameScene", "Terminal layer '" + layerName + "' not found in TMX");
            return new int[0][];
        }
        java.util.List<int[]> list = new java.util.ArrayList<>();
        for (MapObject obj : layer.getObjects()) {
            float x = obj.getProperties().get("x", Float.class);
            float y = obj.getProperties().get("y", Float.class);
            int col = (int)(x / TileMap.TILE_SIZE);
            int row = TileMap.ROWS - 1 - (int)(y / TileMap.TILE_SIZE);
            list.add(new int[]{ col, row });
        }
        return list.toArray(new int[0][]);
    }

    private void loadDoorFromTmx(String layerName) {
        if (tmxMap == null) return;
        MapLayer layer = tmxMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("CyberGameScene", "Door layer '" + layerName + "' not found in TMX");
            return;
        }
        for (MapObject obj : layer.getObjects()) {
            if ("closed door".equals(obj.getName())) {
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                int col = (int)(x / TileMap.TILE_SIZE);
                int row = TileMap.ROWS - 1 - (int)(y / TileMap.TILE_SIZE);
                tmxExitX = TileMap.tileCentreX(col);
                tmxExitY = TileMap.tileCentreY(row);
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

    private void initLevelConfig() {
        terminalTiles   = loadTerminalsFromTmx("terminal");
        challenges      = config.createChallenges(terminal);
        KEYS_REQUIRED   = config.getKeysRequired();
        timeRemaining   = config.getTimeLimit();
        drones          = config.createDrones();
        playerStartTile = config.getPlayerStartTile();
        terminalSolved  = new boolean[terminalTiles.length];
    }

    private void createPlayer() {
        playerEntity = new GameEntity("CyberPlayer");
        float startX = TileMap.tileCentreX(playerStartTile[0]);
        float startY = TileMap.tileCentreY(playerStartTile[1]);
        float[] safeStart = getMapCollision().resolveCircleVsWalls(startX, startY, PLAYER_RADIUS);
        startX = safeStart[0];
        startY = safeStart[1];
        playerEntity.addComponent(new TransformComponent(startX, startY));
        playerEntity.addComponent(new PhysicComponent(new Vector2(0f, 0f), 1.0f));
        playerEntity.addComponent(new RenderComponent(
            new TriangleRenderer(PLAYER_RADIUS),
            new Color(0.3f, 0.8f, 1.0f, 1f)));
        playerEntity.setCollisionRadius(PLAYER_RADIUS);

        entitySystem.addEntity(playerEntity);
        collisionSystem.registerCollidable(playerEntity, CollisionResolution.BOUNCE);
        movementSystem.addEntity(playerEntity,
            new InputDrivenMovement(new CyberPlayerMovement(), input));
    }

    private void setupSupportSystems() {
        checkpointX = TileMap.tileCentreX(playerStartTile[0]);
        checkpointY = TileMap.tileCentreY(playerStartTile[1]);
        maxRespawns = 5;
        respawnsRemaining = maxRespawns;
        signalPingsRemaining = 4;
        protectionTimer = 2.6f;
        cctvAlerted = new boolean[getCameraPositions().length];
        resetDroneAwareness(2.6f);
        setupClueObjects();
        showBanner(config.getLevelName(), config.getIntroSubtitle(), 5.8f);
    }

    /**
     * Set up clue objects scattered around the level.
     * Players must collect these intel fragments before accessing
     * later terminals — turning the game into a multi-room exploration.
     */
    private void setupClueObjects() {
        clueSystem.reset();
        int level = config.getLevelNumber();
        java.util.List<int[]> anchors = buildClueAnchors();

        if (level < 1 || anchors.isEmpty()) return;

        int clueCount = Math.min(3, anchors.size());
        String[][] clueData = {
            { "server_log",    "Server Log",    "Access log entry",     "Suspicious SSH login from 10.0.0.42 at 03:17 AM" },
            { "usb_fragment",  "USB Device",    "Encrypted fragment",   "Partial key: xK7...j9Q — combine with vault data" },
            { "keycard_data",  "Keycard",       "Access credentials",   "Clearance badge for Sector-C terminal access" },
        };

        java.util.List<int[]> selectedAnchors = new java.util.ArrayList<>();
        java.util.List<int[]> placedClues = new java.util.ArrayList<>();

        for (int i = 0; i < clueCount; i++) {
            int[] anchor = selectNextClueAnchor(anchors, selectedAnchors);
            if (anchor == null) break;
            selectedAnchors.add(anchor);

            int[] tile = chooseCluePlacement(anchor, placedClues);
            placedClues.add(tile);

            clueSystem.addClueObject(new ClueSystem.ClueObject(
                tile[0], tile[1],
                clueData[i][0], clueData[i][1],
                clueData[i][2], clueData[i][3]
            ));
        }
    }

    private java.util.List<int[]> buildClueAnchors() {
        java.util.List<int[]> anchors = new java.util.ArrayList<>();

        for (int[] light : getLightPositions()) {
            if (light.length >= 2) anchors.add(new int[] { light[0], light[1] });
        }
        for (int[] cam : getCameraPositions()) {
            if (cam.length >= 2) anchors.add(new int[] { cam[0], cam[1] });
        }
        if (drones != null) {
            for (io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI drone : drones) {
                int col = Math.max(1, Math.min(TileMap.COLS - 2, (int)(drone.getSpawnX() / TileMap.TILE_SIZE)));
                int row = Math.max(1, Math.min(TileMap.ROWS - 2, (int)(drone.getSpawnY() / TileMap.TILE_SIZE)));
                anchors.add(new int[] { col, row });
            }
        }
        return anchors;
    }

    private int[] selectNextClueAnchor(java.util.List<int[]> anchors, java.util.List<int[]> selectedAnchors) {
        int[] best = null;
        float bestScore = -Float.MAX_VALUE;

        for (int[] anchor : anchors) {
            boolean alreadyUsed = false;
            for (int[] selected : selectedAnchors) {
                if (selected[0] == anchor[0] && selected[1] == anchor[1]) {
                    alreadyUsed = true;
                    break;
                }
            }
            if (alreadyUsed) continue;

            float nearestTerminal = nearestTerminalTileDistance(anchor[0], anchor[1]);
            float nearestSelected = selectedAnchors.isEmpty() ? 10f : nearestPlacedDistance(anchor[0], anchor[1], selectedAnchors);
            float score = nearestSelected * 1.9f + nearestTerminal * 0.8f;
            if (score > bestScore) {
                bestScore = score;
                best = anchor;
            }
        }
        return best;
    }

    private int[] chooseCluePlacement(int[] anchor, java.util.List<int[]> placedClues) {
        int[][] offsets = {
            { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
            { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 },
            { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 },
            { 2, 1 }, { -2, 1 }, { 2, -1 }, { -2, -1 },
            { 1, 2 }, { -1, 2 }, { 1, -2 }, { -1, -2 },
            { 2, 2 }, { -2, 2 }, { 2, -2 }, { -2, -2 },
            { 3, 0 }, { -3, 0 }, { 0, 3 }, { 0, -3 },
            { 0, 0 }
        };

        int[] best = null;
        float bestScore = -Float.MAX_VALUE;

        for (int[] offset : offsets) {
            int col = Math.max(1, Math.min(TileMap.COLS - 2, anchor[0] + offset[0]));
            int row = Math.max(1, Math.min(TileMap.ROWS - 2, anchor[1] + offset[1]));
            if (collisionMgr != null && collisionMgr.isWall(col, row)) continue;
            if (!isTileReachableFromPlayerStart(col, row)) continue;

            float terminalDist = nearestTerminalTileDistance(col, row);
            if (terminalDist < 3.6f) continue;

            float placedDist = placedClues.isEmpty() ? 10f : nearestPlacedDistance(col, row, placedClues);
            float anchorDist = tileDistance(col, row, anchor[0], anchor[1]);
            float score = placedDist * 1.2f + terminalDist * 0.85f - anchorDist * 1.65f;

            if (score > bestScore) {
                bestScore = score;
                best = new int[] { col, row };
            }
        }

        if (best != null) return best;

        for (int radius = 1; radius <= 10; radius++) {
            for (int dc = -radius; dc <= radius; dc++) {
                for (int dr = -radius; dr <= radius; dr++) {
                    int col = Math.max(1, Math.min(TileMap.COLS - 2, anchor[0] + dc));
                    int row = Math.max(1, Math.min(TileMap.ROWS - 2, anchor[1] + dr));
                    if (collisionMgr != null && collisionMgr.isWall(col, row)) continue;
                    if (!isTileReachableFromPlayerStart(col, row)) continue;
                    return new int[] { col, row };
                }
            }
        }

        int fallbackCol = Math.max(1, Math.min(TileMap.COLS - 2, playerStartTile[0] + 2));
        int fallbackRow = Math.max(1, Math.min(TileMap.ROWS - 2, playerStartTile[1]));
        return new int[] { fallbackCol, fallbackRow };
    }

    private boolean isTileReachableFromPlayerStart(int targetCol, int targetRow) {
        if (playerStartTile == null) return true;
        if (collisionMgr == null) return true;
        if (collisionMgr.isWall(targetCol, targetRow)) return false;

        int startCol = playerStartTile[0];
        int startRow = playerStartTile[1];
        if (collisionMgr.isWall(startCol, startRow)) return false;
        if (startCol == targetCol && startRow == targetRow) return true;

        boolean[][] visited = new boolean[TileMap.ROWS][TileMap.COLS];
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        queue.add(new int[] { startCol, startRow });
        visited[startRow][startCol] = true;

        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            for (int[] dir : dirs) {
                int nextCol = cur[0] + dir[0];
                int nextRow = cur[1] + dir[1];
                if (nextCol < 0 || nextCol >= TileMap.COLS || nextRow < 0 || nextRow >= TileMap.ROWS) continue;
                if (visited[nextRow][nextCol]) continue;
                if (collisionMgr.isWall(nextCol, nextRow)) continue;
                if (nextCol == targetCol && nextRow == targetRow) return true;
                visited[nextRow][nextCol] = true;
                queue.addLast(new int[] { nextCol, nextRow });
            }
        }
        return false;
    }

    private float nearestTerminalTileDistance(int col, int row) {
        float best = Float.MAX_VALUE;
        if (terminalTiles == null) return best;
        for (int[] terminalTile : terminalTiles) {
            best = Math.min(best, tileDistance(col, row, terminalTile[0], terminalTile[1]));
        }
        return best == Float.MAX_VALUE ? 10f : best;
    }

    private float nearestPlacedDistance(int col, int row, java.util.List<int[]> positions) {
        float best = Float.MAX_VALUE;
        for (int[] pos : positions) {
            best = Math.min(best, tileDistance(col, row, pos[0], pos[1]));
        }
        return best == Float.MAX_VALUE ? 10f : best;
    }

    private float tileDistance(int colA, int rowA, int colB, int rowB) {
        float dx = colA - colB;
        float dy = rowA - rowB;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    private void resetDroneAwareness(float suppressSeconds) {
        for (DroneAI drone : drones) {
            drone.resetToPatrolAtSpawn(suppressSeconds);
        }
    }

    private void setCheckpoint(float x, float y) {
        float[] safePoint = findSafeRespawnPoint(x, y);
        checkpointX = safePoint[0];
        checkpointY = safePoint[1];
        showBanner("SYNC POINT LOCKED", "Progress cached. Respawn node updated to nearest safe sector.", 2.8f);
    }

    private void showBanner(String title, String subtitle, float duration) {
        bannerTitle = title != null ? title : "";
        bannerSubtitle = subtitle != null ? subtitle : "";
        bannerTimer = Math.max(bannerTimer, duration);
    }

    private void triggerSignalPing() {
        if (signalPingsRemaining <= 0) {
            showBanner("SIGNAL DEPLETED", "No scans left. Search manually or reach another node.", 2.1f);
            return;
        }

        signalPingsRemaining--;
        hintsUsed++;
        terminalPingTimer = 6.5f;
        pingFxTimer = PING_FX_DURATION;

        TransformComponent tc = playerEntity != null ? playerEntity.getComponent(TransformComponent.class) : null;
        int revealed = 0;
        if (tc != null) {
            Vector2 pp = tc.getPosition();
            revealed = clueSystem.revealObjectsWithinRadius(pp.x, pp.y, PING_REVEAL_RADIUS, 3.5f);
            spawnParticles(pp.x, pp.y, 0.1f, 0.85f, 1f, 18);
        }

        String subtitle = revealed > 0
            ? "Scan wave exposed " + revealed + " hidden intel source" + (revealed > 1 ? "s" : "") + "."
            : "No hidden intel in range. Objective vector refreshed.";
        showBanner("SIGNAL PING ACTIVE", subtitle, 2.8f);
    }

    private void respawnAtCheckpoint() {
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;

        float[] safePoint = findSafeRespawnPoint(checkpointX, checkpointY);
        checkpointX = safePoint[0];
        checkpointY = safePoint[1];

        tc.getPosition().set(checkpointX, checkpointY);
        float[] resolved = getMapCollision().resolveCircleVsWalls(tc.getPosition().x, tc.getPosition().y, PLAYER_RADIUS);
        tc.getPosition().set(resolved[0], resolved[1]);

        PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
        if (phys != null) phys.getVelocity().set(0f, 0f);

        protectionTimer = 3.25f;
        resetDroneAwareness(protectionTimer);
        spawnParticles(checkpointX, checkpointY, 0.2f, 0.8f, 1f, 24);
        followCamera(checkpointX, checkpointY);
    }

    private float[] findSafeRespawnPoint(float desiredX, float desiredY) {
        int startCol = TileMap.worldToCol(desiredX);
        int startRow = TileMap.worldToRow(desiredY);
        float[] best = new float[]{TileMap.tileCentreX(startCol), TileMap.tileCentreY(startRow)};
        float bestScore = -Float.MAX_VALUE;

        for (int radius = 0; radius <= 6; radius++) {
            for (int row = Math.max(0, startRow - radius); row <= Math.min(TileMap.ROWS - 1, startRow + radius); row++) {
                for (int col = Math.max(0, startCol - radius); col <= Math.min(TileMap.COLS - 1, startCol + radius); col++) {
                    if (collisionMgr.isWall(col, row)) continue;
                    boolean terminalTile = false;
                    for (int[] terminalTilePos : terminalTiles) {
                        if (terminalTilePos[0] == col && terminalTilePos[1] == row) { terminalTile = true; break; }
                    }
                    if (terminalTile) continue;

                    float worldX = TileMap.tileCentreX(col);
                    float worldY = TileMap.tileCentreY(row);
                    float[] resolved = getMapCollision().resolveCircleVsWalls(worldX, worldY, PLAYER_RADIUS);
                    if (Math.abs(resolved[0] - worldX) > 0.75f || Math.abs(resolved[1] - worldY) > 0.75f) continue;

                    float droneSeparation = Float.MAX_VALUE;
                    float losPenalty = 0f;
                    for (DroneAI drone : drones) {
                        droneSeparation = Math.min(droneSeparation, dist(worldX, worldY, drone.getPosition().x, drone.getPosition().y));
                        if (getMapCollision().hasLineOfSight(worldX, worldY, drone.getPosition().x, drone.getPosition().y)) {
                            losPenalty += 90f;
                        }
                    }
                    if (drones.length == 0) droneSeparation = 9999f;

                    float centerPenalty = Math.abs(col - startCol) + Math.abs(row - startRow);
                    float score = droneSeparation - centerPenalty * 18f - losPenalty;
                    if (score > bestScore) {
                        bestScore = score;
                        best[0] = worldX;
                        best[1] = worldY;
                    }
                }
            }
            if (bestScore > -Float.MAX_VALUE / 2f) break;
        }

        return best;
    }

    private void handleDroneCatch() {
        if (respawnsRemaining > 1) {
            respawnsRemaining--;
            respawnsUsed++;
            respawnAtCheckpoint();
            showBanner("INTEGRITY BREACH", "Respawned at last sync point. Cloak engaged \u2014 re-route and evade.", 2.9f);
            return;
        }
        respawnsRemaining = 0;
        gameOver = true;
    }

    private int getNearestTerminalIndex(Vector2 from) {
        int bestIdx = -1;
        float best = Float.MAX_VALUE;
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            float d = dist(from.x, from.y, tx, ty);
            if (d < best) {
                best = d;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private int getNearbyTerminalIndex(Vector2 from, float radius) {
        int bestIdx = -1;
        float best = radius;
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            float d = dist(from.x, from.y, tx, ty);
            if (d < best) {
                best = d;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private float[] getExitCentre() {
        return new float[]{tmxExitX, tmxExitY};
    }

    private void maybeRestoreIntegrity() {
        if (respawnsRemaining < maxRespawns && keysCollected > 0 && keysCollected % 2 == 0) {
            respawnsRemaining++;
            spawnParticles(checkpointX, checkpointY, 0.25f, 1f, 0.85f, 18);
            showBanner("SYSTEM PATCHED", "+1 integrity restored for dual-node compromise.", 2.5f);
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================
    @Override
    protected void onUpdate(float delta) {
        stateTime += delta;
        rotorAngle += delta * 200f;
        if (!gameOver && !victory) missionElapsed += delta;

        // Time limit enforcement
        if (!gameOver && !victory) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                showBanner("TIME'S UP", "Mission failed. Time limit exceeded.", 2.5f);
                handleDroneCatch();
                return;
            }
        }

        // Screen transition fade-in
        if (transitionAlpha > 0f) transitionAlpha = Math.max(0f, transitionAlpha - delta * 1.8f);

        // Particle update
        updateParticles(delta);
        if (chaseWarningTimer > 0f) chaseWarningTimer = Math.max(0f, chaseWarningTimer - delta);
        if (protectionTimer > 0f) protectionTimer = Math.max(0f, protectionTimer - delta);
        if (terminalPingTimer > 0f) terminalPingTimer = Math.max(0f, terminalPingTimer - delta);
        if (pingFxTimer > 0f) pingFxTimer = Math.max(0f, pingFxTimer - delta);
        if (bannerTimer > 0f) bannerTimer = Math.max(0f, bannerTimer - delta);
        clueSystem.update(delta);

        if (gameOver || victory) {
            if (input.isActionJustPressed("INTERACT") || input.isActionJustPressed("MENU_CONFIRM") || input.isActionJustPressed("START_GAME")) {
                if (victory)
                    nav.requestScene(factory.createVictoryScene(keysCollected, KEYS_REQUIRED, (int) missionElapsed, config.getLevelNumber(), respawnsUsed, hintsUsed));
                else
                    nav.requestScene(factory.createGameOverScene(config.getLevelNumber()));
            }
            return;
        }

        if (activeChallenge != null) {
            if (activeChallenge.isOpen()) {
                activeChallenge.update(delta);
            }
            if (!activeChallenge.isOpen()) {
                if (activeChallenge.isSolved()) {
                    terminalSolved[activeChallengeIdx] = true;
                    keysCollected++;
                    eventSystem.notifyKeyCollected(keysCollected, KEYS_REQUIRED);
                    // Particle burst: green sparks
                    spawnParticles(TileMap.tileCentreX(terminalTiles[activeChallengeIdx][0]),
                                   TileMap.tileCentreY(terminalTiles[activeChallengeIdx][1]),
                                   0f, 1f, 0.4f, 16);
                    setCheckpoint(TileMap.tileCentreX(terminalTiles[activeChallengeIdx][0]),
                                   TileMap.tileCentreY(terminalTiles[activeChallengeIdx][1]));
                    protectionTimer = Math.max(protectionTimer, 1.75f);
                    resetDroneAwareness(protectionTimer);
                    showBanner("NODE COMPROMISED", challenges[activeChallengeIdx].getTitle() + " breached. Access key " + keysCollected + " / " + KEYS_REQUIRED + " extracted.", 2.3f);
                    maybeRestoreIntegrity();
                    if (keysCollected >= KEYS_REQUIRED) {
                        exitUnlocked = true;
                        showBanner("FIREWALL BYPASSED", "All nodes compromised. Proceed to extraction point.", 3.0f);
                        // Purple energy burst at exit
                        spawnParticles(tmxExitX, tmxExitY, 0.7f, 0f, 1f, 20);
                    }
                } else {
                    String msg = activeChallenge.wasPanicked() ? "Network trace detected. Reconnect when safe." : "Session idle. Re-enter to resume decryption.";
                    showBanner("CONNECTION TERMINATED", msg, 1.8f);
                }
                activeChallenge = null; activeChallengeIdx = -1;
            } else {
                return;
            }
        }

        if (input.isActionJustPressed("SETTINGS")) {
            nav.pushScene(factory.createSettingsScene()); return;
        }
        if (input.isActionJustPressed("HELP")) {
            triggerSignalPing();
        }

        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            float[] resolved = getMapCollision().resolveCircleVsWalls(
                tc.getPosition().x, tc.getPosition().y, PLAYER_RADIUS);
            tc.getPosition().set(resolved[0], resolved[1]);
        }

        // ── Player state tracking ────────────────────────────────────────
        if (tc != null) {
            PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
            float vx = phys != null ? phys.getVelocity().x : 0f;
            float vy = phys != null ? phys.getVelocity().y : 0f;
            boolean isMoving = Math.abs(vx) > 0.5f || Math.abs(vy) > 0.5f;
            if (isMoving) {
                playerFacingAngle = (float) Math.toDegrees(Math.atan2(vy, vx));
            }

            if (activeChallenge != null && activeChallenge.isOpen()) {
                playerState = PlayerState.HACKING;
            } else if (scanAnimTimer > 0f) {
                playerState = PlayerState.SCANNING;
                scanAnimTimer -= delta;
            } else if (isMoving) {
                playerState = PlayerState.MOVING;
            } else {
                playerState = PlayerState.IDLE;
            }
        }

        // ── INTERACT key — clue objects first, then terminals ────────────
        if (input.isActionJustPressed("INTERACT") && tc != null) {
            Vector2 pp = tc.getPosition();

            // Check clue objects first
            boolean interactedClue = false;
            for (ClueSystem.ClueObject clueObj : clueSystem.getClueObjects()) {
                if (clueObj.collected) continue;
                float cx = TileMap.tileCentreX(clueObj.tileCol);
                float cy = TileMap.tileCentreY(clueObj.tileRow);
                if (dist(pp.x, pp.y, cx, cy) < TileMap.TILE_SIZE * 2.0f) {
                    clueObj.collected = true;
                    clueSystem.collectClue(clueObj.clueId, clueObj.clueTitle,
                        clueObj.clueDescription, missionElapsed);
                    scanAnimTimer = 1.2f; // trigger scanning animation
                    spawnParticles(cx, cy, 0.1f, 0.85f, 1f, 12);
                    showBanner("INTEL ACQUIRED", clueObj.objectName + ": " + clueObj.clueTitle, 2.5f);
                    interactedClue = true;
                    break;
                }
            }

            // Then check terminals
            if (!interactedClue) {
                for (int i = 0; i < terminalTiles.length; i++) {
                    if (terminalSolved[i]) continue;
                    float tx = TileMap.tileCentreX(terminalTiles[i][0]);
                    float ty = TileMap.tileCentreY(terminalTiles[i][1]);
                    if (dist(pp.x, pp.y, tx, ty) < TileMap.TILE_SIZE * 2.2f) {
                        // Check if terminal requires clues
                        if (!clueSystem.canAccessTerminal(i, terminalTiles.length)) {
                            String hint = clueSystem.getTerminalLockHint(i);
                            showBanner("ACCESS DENIED", hint != null ? hint : "Insufficient intel clearance.", 2.0f);
                            break;
                        }
                        activeChallenge = challenges[i];
                        activeChallengeIdx = i;
                        activeChallenge.open();
                        break;
                    }
                }
            }
        }

        if (tc != null) {
            for (DroneAI drone : drones) {
                boolean wasChasing = "CHASE".equals(drone.getStateName());
                drone.update(getMapCollision(), tc.getPosition(), delta);

                boolean nowChasing = "CHASE".equals(drone.getStateName());
                if (!wasChasing && nowChasing) {
                    spawnParticles(drone.getPosition().x, drone.getPosition().y, 1f, 0.1f, 0f, 12);
                    chaseWarningTimer = 1.2f;
                }

                // More forgiving stealth loop: spotting starts a chase, but the player only loses
                // when the drone actually closes the distance and physically catches them.
                if (protectionTimer <= 0f && drone.isCatchingPlayer(tc.getPosition(), PLAYER_RADIUS + DRONE_CONTACT_RADIUS_BONUS)) {
                    handleDroneCatch();
                    return;
                }
            }

            // CCTV detection: cameras spot the player and attract the nearest drone
            cctvAlertCooldown = Math.max(0f, cctvAlertCooldown - delta);
            {
                int[][] camPositions = getCameraPositions();
                float ts = TileMap.TILE_SIZE;
                Vector2 pp = tc.getPosition();
                for (int ci = 0; ci < camPositions.length; ci++) {
                    int[] cam = camPositions[ci];
                    float cx = TileMap.tileCentreX(cam[0]);
                    float cy = TileMap.tileCentreY(cam[1]);
                    float baseAng = cam[2];
                    float phase = ci * 1.3f;
                    float panAng = (float) Math.sin(stateTime * 0.7f + phase) * 40f;
                    float totalAng = baseAng + panAng;
                    float coneLen = ts * 2.6f;
                    float halfFov = 28f;

                    float pdx = pp.x - cx;
                    float pdy = pp.y - cy;
                    float pDist = (float) Math.sqrt(pdx * pdx + pdy * pdy);
                    if (pDist > coneLen) { cctvAlerted[ci] = false; continue; }

                    float angleToPlayer = (float) Math.toDegrees(Math.atan2(pdy, pdx));
                    float angleDiff = angleToPlayer - totalAng;
                    while (angleDiff > 180f) angleDiff -= 360f;
                    while (angleDiff < -180f) angleDiff += 360f;
                    if (Math.abs(angleDiff) > halfFov) { cctvAlerted[ci] = false; continue; }

                    if (!getMapCollision().hasLineOfSight(cx, cy, pp.x, pp.y)) { cctvAlerted[ci] = false; continue; }

                    // Player is visible to this camera
                    cctvAlerted[ci] = true;

                    if (cctvAlertCooldown > 0f) continue;

                    // Camera spotted player — ALL drones converge on the area around the player.
                    // Scatter offset is ±2 tiles so each drone approaches from a different angle
                    // and they don't all pile into the same wall tile near the camera.
                    boolean anyDispatched = false;
                    for (DroneAI d : drones) {
                        float offsetX = (float)(Math.random() * 128 - 64);
                        float offsetY = (float)(Math.random() * 128 - 64);
                        d.transitionTo(new SearchState(pp.x + offsetX, pp.y + offsetY, 4.5f));
                        anyDispatched = true;
                    }
                    if (anyDispatched) {
                        cctvAlertCooldown = CCTV_ALERT_INTERVAL;
                        showBanner("SURVEILLANCE TRIGGERED", "Camera feed compromised. All drone units converging on your position.", 2.2f);
                    }
                }
            }

        }

        if (exitUnlocked && tc != null) {
            if (dist(tc.getPosition().x, tc.getPosition().y, tmxExitX, tmxExitY)
                    < TileMap.TILE_SIZE * 1.5f) {
                victory = true; return;
            }
        }

        if (tc != null) followCamera(tc.getPosition().x, tc.getPosition().y);
    }

    private void followCamera(float px, float py) {
        float hw = VIEW_W / 2f, hh = VIEW_H / 2f;
        float cx = Math.max(hw, Math.min(TileMap.WORLD_W - hw, px));
        float cy = Math.max(hh, Math.min(TileMap.WORLD_H - hh, py));
        camera.position.set(cx, cy, 0);
        camera.update();
    }

    // =========================================================================
    // RENDER
    // =========================================================================
    @Override
    protected void onRender() {
        Gdx.gl.glClearColor(0f, 0f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        tmxRenderer.setView(camera);
        tmxRenderer.render();
        worldRenderer.renderTmxExitDoor(tmxExitX, tmxExitY, exitUnlocked,
            doorClosedRegion, doorOpenedRegion);
        worldRenderer.renderRoomProps(stateTime, terminalTiles,
            getCameraPositions(), drones, cctvAlerted, playerEntity, collisionMgr);
        worldRenderer.renderCheckpointBeacon(stateTime, checkpointX, checkpointY);
        worldRenderer.renderTerminalGlow(terminalTiles, terminalSolved);
        worldRenderer.renderClueObjects(stateTime, clueSystem, terminalTiles,
            terminalSolved, playerEntity, terminalPingTimer);
        worldRenderer.renderSignalPingEffect();
        worldRenderer.renderTerminalHints(stateTime, terminalPingTimer,
            terminalTiles, terminalSolved, clueSystem, playerEntity, PING_REVEAL_RADIUS);
        worldRenderer.renderExitGuidance(stateTime, exitUnlocked, playerEntity,
            tmxExitX, tmxExitY);

        for (DroneAI drone : drones) drone.render(sr);

        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        renderPlayer(tc);
        renderParticles();
        worldRenderer.renderAtmosphere();

        if (activeChallenge != null && activeChallenge.isOpen()) {
            hudViewport.apply();
            sr.setProjectionMatrix(hudCamera.combined);
            batch.setProjectionMatrix(hudCamera.combined);
            activeChallenge.render(sr, batch, hudFont);
            return;
        }

        hudViewport.apply();
        sr.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);

        // Delegate all HUD rendering to CyberHudRenderer (SRP)
        String[] challengeTitles = buildChallengeTitles();
        int nearbyIdx = tc != null ? getNearbyTerminalIndex(tc.getPosition(), TileMap.TILE_SIZE * 1.6f) : -1;

        hudRenderer.renderHUD(stateTime, timeRemaining, missionElapsed,
            keysCollected, KEYS_REQUIRED, respawnsRemaining, maxRespawns,
            signalPingsRemaining, clueSystem, playerState, exitUnlocked,
            chaseWarningTimer, bannerTimer,
            activeChallenge != null && activeChallenge.isOpen(),
            drones, tc, nearbyIdx, null, challengeTitles);

        hudRenderer.renderMinimap(tc, collisionMgr.getWallGrid(),
            terminalTiles, terminalSolved, clueSystem,
            tmxExitX, tmxExitY, exitUnlocked, checkpointX, checkpointY,
            drones, stateTime);

        hudRenderer.renderThreatIndicator(tc, drones, stateTime);
        hudRenderer.renderChaseWarning(stateTime, chaseWarningTimer);

        if (bannerTimer > 0f && (activeChallenge == null || !activeChallenge.isOpen())) {
            hudRenderer.renderObjectiveBanner(stateTime, bannerTimer,
                bannerTitle, bannerSubtitle, chaseWarningTimer);
        }

        if (gameOver || victory) {
            hudRenderer.renderEndScreen(victory, stateTime,
                keysCollected, KEYS_REQUIRED, missionElapsed, respawnsRemaining);
        }

        // Screen transition overlay (fade from black on level entry)
        if (transitionAlpha > 0.01f) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0f, 0f, 0f, transitionAlpha);
            sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
            sr.end();
        }
    }

    // ── Player ───────────────────────────────────────────────────────────────
    private void renderPlayer(TransformComponent tc) {
        if (tc == null) return;
        float px = tc.getPosition().x, py = tc.getPosition().y;
        float r = PLAYER_RADIUS;

        if (playerAnimator != null) {
            PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
            float vx = phys != null ? phys.getVelocity().x : 0f;
            float vy = phys != null ? phys.getVelocity().y : 0f;
            playerAnimator.update(Gdx.graphics.getDeltaTime(), vx, vy);
            batch.begin();
            playerAnimator.drawCentered(batch, px, py, r * 2.8f);
            batch.end();
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.3f, 0.8f, 1.0f, 1f);
            sr.triangle(px, py + r, px - r * 0.85f, py - r * 0.6f, px + r * 0.85f, py - r * 0.6f);
            sr.end();
        }
    }

    // ── Room props ────────────────────────────────────────────────────────────
    /**
     * Renders world-space room props that sit on top of the TMX tile layer:
     *
     *  ceiling_light.png  – Soft flickering glow pool on the floor beneath each
     *                       ceiling fixture, plus the fixture sprite drawn above it.
     *
     *  sec_camera.png     – Security camera sprite rotated ±40° like a real CCTV
     *                       pan; a translucent scan-cone is drawn in front of it.
     *                       Tinted red when the camera has spotted the player.
     *
     *  drone sprites      – Patrol/detect/damaged drone sheet frames drawn rotated
     *                       to match drone facing; tinted by alert state.
     *                       Also renders a soft vision cone in front of each drone.
     *
     *  (wifi badge intentionally omitted — world-prompt card handles proximity UI)
     */

    // ── Particles ─────────────────────────────────────────────────────────────
    private void spawnParticles(float x, float y, float r, float g, float b, int count) {
        for (int i = 0; i < count && particleCount < MAX_PARTICLES; i++) {
            int idx = particleCount++;
            pX[idx] = x; pY[idx] = y;
            float angle = (float)(Math.random() * Math.PI * 2);
            float speed = 30f + (float)(Math.random() * 80f);
            pVX[idx] = (float)Math.cos(angle) * speed;
            pVY[idx] = (float)Math.sin(angle) * speed;
            pLife[idx] = 0.5f + (float)(Math.random() * 0.5f);
            pR[idx] = r; pG[idx] = g; pB[idx] = b;
        }
    }

    private void updateParticles(float dt) {
        for (int i = 0; i < particleCount; i++) {
            pLife[i] -= dt;
            if (pLife[i] <= 0f) {
                particleCount--;
                pX[i] = pX[particleCount]; pY[i] = pY[particleCount];
                pVX[i] = pVX[particleCount]; pVY[i] = pVY[particleCount];
                pLife[i] = pLife[particleCount];
                pR[i] = pR[particleCount]; pG[i] = pG[particleCount]; pB[i] = pB[particleCount];
                i--;
                continue;
            }
            pX[i] += pVX[i] * dt;
            pY[i] += pVY[i] * dt;
            pVX[i] *= 0.96f;
            pVY[i] *= 0.96f;
        }
    }

    private void renderParticles() {
        if (particleCount == 0) return;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < particleCount; i++) {
            float alpha = Math.min(1f, pLife[i] * 2f);
            sr.setColor(pR[i], pG[i], pB[i], alpha);
            sr.circle(pX[i], pY[i], 2f + pLife[i] * 3f, 6);
        }
        sr.end();
    }

    @Override
    protected void onLateUpdate(float delta) {
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;
        float r = PLAYER_RADIUS;
        tc.getPosition().set(
            Math.max(TileMap.TILE_SIZE + r, Math.min(TileMap.WORLD_W - TileMap.TILE_SIZE - r, tc.getPosition().x)),
            Math.max(TileMap.TILE_SIZE + r, Math.min(TileMap.WORLD_H - TileMap.TILE_SIZE - r, tc.getPosition().y)));
    }

    @Override public void resize(int w, int h) {
        if (viewport    != null) viewport.update(w, h, true);
        if (hudViewport != null) hudViewport.update(w, h, true);
    }
    @Override protected void onUnload() { Gdx.app.log("CyberGame", "unloading level " + config.getLevelNumber()); }
    @Override protected void onDispose() {
        if (activeChallenge != null && activeChallenge.isOpen()) activeChallenge.close();
        if (playerAnimator != null) playerAnimator.dispose();
        if (sr     != null) sr.dispose();
        if (batch  != null) batch.dispose();
        if (hudFont        != null) hudFont.dispose();
        if (hudSmallFont   != null) hudSmallFont.dispose();
        if (hudPanelFont   != null) hudPanelFont.dispose();
        if (alertFont      != null) alertFont.dispose();
        if (promptFont     != null) promptFont.dispose();
        if (tmxRenderer != null) tmxRenderer.dispose();
        if (tmxMap     != null) tmxMap.dispose();
        sprites.dispose();
    }

    @Override
    public boolean blocksWorldUpdate() {
        return activeChallenge != null && activeChallenge.isOpen();
    }




    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
