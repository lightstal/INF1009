package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.engine.collision.CollisionInfo;
import io.github.INF1009_P10_Team7.engine.collision.IContinuousCollisionResponse;
import io.github.INF1009_P10_Team7.engine.collision.ICollisionSystem;
import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.IEntitySystem;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.movement.IMovementSystem;
import io.github.INF1009_P10_Team7.engine.movement.InputDrivenMovement;
import io.github.INF1009_P10_Team7.engine.map.ILevelMapRuntime;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;

import io.github.INF1009_P10_Team7.cyber.CyberPlayerMovement;
import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.clue.ClueSystem;
import io.github.INF1009_P10_Team7.cyber.player.PlayerInventory;
import io.github.INF1009_P10_Team7.cyber.player.PlayerState;
import io.github.INF1009_P10_Team7.cyber.render.CyberSprites;
import io.github.INF1009_P10_Team7.cyber.render.CyberGameRenderer;
import io.github.INF1009_P10_Team7.cyber.level.LevelConfig;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.components.cctv.CctvComponent;
import io.github.INF1009_P10_Team7.cyber.components.cctv.CctvDetectionSystem;
import io.github.INF1009_P10_Team7.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.cyber.components.terminal.TerminalComponent;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneComponent;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAIMovementBehaviour;
import io.github.INF1009_P10_Team7.cyber.components.drone.SearchState;
import io.github.INF1009_P10_Team7.cyber.minigame.*;
import io.github.INF1009_P10_Team7.cyber.observer.GameEventSystem;

/**
 * CyberGameScene, main gameplay scene for Levels 1 and 2.
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
    private final ILevelMapRuntime mapRuntime;

    private final CyberSprites sprites = new CyberSprites();
    private CyberGameRenderer renderer;

    // Map runtime support
    private IWorldCollisionQuery collisionMgr;
    private float tmxExitX, tmxExitY;
    private float   stateTime  = 0f;

    private GameEntity playerEntity;
    private static final float PLAYER_RADIUS = 10f;

    private int[][]    terminalTiles;
    private boolean[]  terminalSolved;
    private IMiniGame[] challenges;
    private int        KEYS_REQUIRED;
    private final java.util.List<GameEntity> droneEntities = new java.util.ArrayList<>();
    private final java.util.List<DroneAI> newlyChasingDrones = new java.util.ArrayList<>();
    private final java.util.List<GameEntity> terminalEntities = new java.util.ArrayList<>();
    private final java.util.List<GameEntity> cctvEntities = new java.util.ArrayList<>();
    private final CctvDetectionSystem cctvDetectionSystem = new CctvDetectionSystem();
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
    private float   frameDelta = 0f;
    private float   chaseWarningTimer = 0f;

    // Snapshot used by drone movement behaviours (MovementSystem runs after onUpdate).
    private final Vector2 playerPosSnapshot = new Vector2(0f, 0f);
    private boolean pendingDroneCatch = false;
    private boolean interactPressedThisFrame = false;

    // Collision-driven interaction proximity (updated during CollisionSystem step)
    private int nearbyTerminalIdx = -1;
    private float nearbyTerminalDist2 = Float.POSITIVE_INFINITY;
    private GameEntity nearbyClueEntity = null;
    private boolean overlappingExit = false;

    private GameEntity exitTriggerEntity = null;
    private final java.util.List<GameEntity> clueEntities = new java.util.ArrayList<>();
    private final java.util.Map<GameEntity, ClueSystem.ClueObject> clueByEntity = new java.util.HashMap<>();

    private float checkpointX;
    private float checkpointY;
    private int maxRespawns;
    private int respawnsRemaining;
    private int respawnsUsed = 0;
    private float protectionTimer = 0f;
    // Continuous response: keeps proximity state fresh every frame while overlapping.
    private final IContinuousCollisionResponse cyberCollisionResponse = new IContinuousCollisionResponse() {
        @Override
        public void resolve(io.github.INF1009_P10_Team7.engine.collision.ICollidable obj1,
                            io.github.INF1009_P10_Team7.engine.collision.ICollidable obj2,
                            CollisionInfo info) {
            if (playerEntity == null) return;

            boolean aIsPlayer = obj1 == playerEntity;
            boolean bIsPlayer = obj2 == playerEntity;
            if (!aIsPlayer && !bIsPlayer) return;

            io.github.INF1009_P10_Team7.engine.collision.ICollidable other = aIsPlayer ? obj2 : obj1;
            if (!(other instanceof GameEntity)) return;
            GameEntity otherEntity = (GameEntity) other;

            // Drone catch
            if (protectionTimer <= 0f && otherEntity.getComponent(DroneComponent.class) != null) {
                pendingDroneCatch = true;
                return;
            }

            // Terminal proximity
            TerminalComponent terminalComponent = otherEntity.getComponent(TerminalComponent.class);
            if (terminalComponent != null) {
                TransformComponent pt = playerEntity.getComponent(TransformComponent.class);
                TransformComponent tt = otherEntity.getComponent(TransformComponent.class);
                if (pt != null && tt != null) {
                    float dx = pt.getPosition().x - tt.getPosition().x;
                    float dy = pt.getPosition().y - tt.getPosition().y;
                    float d2 = dx * dx + dy * dy;
                    if (d2 < nearbyTerminalDist2) {
                        nearbyTerminalDist2 = d2;
                        nearbyTerminalIdx = terminalComponent.getTerminalIndex();
                    }
                }
                return;
            }

            // Clue proximity
            if (clueByEntity.containsKey(otherEntity)) {
                nearbyClueEntity = otherEntity;
                return;
            }

            // Exit trigger proximity
            if (otherEntity == exitTriggerEntity) {
                overlappingExit = true;
            }
        }
    };
    private float terminalPingTimer = 0f;
    private float pingFxTimer = 0f;
    private static final float PING_FX_DURATION = 0.85f;
    private static final float PING_REVEAL_RADIUS = TileMap.TILE_SIZE * 9f;
    private float cctvAlertCooldown = 0f;
    private static final float CCTV_ALERT_INTERVAL = 5f;
    private boolean[] cctvAlerted;
    private int signalPingsRemaining;
    private int hintsUsed = 0;
    private String bannerTitle = "";
    private String bannerSubtitle = "";
    private float bannerTimer = 0f;
    private float bannerDuration = 0f;

    // Scene-entry fade transition
    private float transitionAlpha = 1f;  // starts black, fades in

    // Player state system
    private PlayerState playerState = PlayerState.IDLE;
    private final ClueSystem clueSystem = new ClueSystem();
    private float scanAnimTimer = 0f;

    // Particle effects
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
                          LevelConfig config,
                          ILevelMapRuntime mapRuntime) {
        super(input, audio, nav);
        this.entitySystem    = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem  = movementSystem;
        this.factory         = factory;
        this.config          = config;
        this.mapRuntime      = mapRuntime;
    }

    // =========================================================================
    @Override
    protected void onLoad() {
        mapRuntime.load();
        collisionMgr = mapRuntime.getCollisionQuery();
        terminalTiles = mapRuntime.getTerminalTiles();
        tmxExitX = mapRuntime.getExitX();
        tmxExitY = mapRuntime.getExitY();

        sprites.load();

        renderer = new CyberGameRenderer(input, mapRuntime, config, sprites);
        renderer.load();

        eventSystem.addObserver(inventory);
        initLevelConfig();
        createPlayer();
        createExitTrigger();
        setupSupportSystems();
        audio.setMusic("audio/Music_Game.mp3");

        transitionAlpha = 1f;  // fade-in from black
        missionElapsed = 0f;
    }

    private void createExitTrigger() {
        // Trigger volume at the extraction point; victory is handled in onLateUpdate.
        exitTriggerEntity = new GameEntity("ExitTrigger");
        exitTriggerEntity.addComponent(new TransformComponent(tmxExitX, tmxExitY));
        exitTriggerEntity.setCollisionRadius(TileMap.TILE_SIZE * 1.5f);
        entitySystem.addEntity(exitTriggerEntity);
        collisionSystem.registerCollidable(exitTriggerEntity, cyberCollisionResponse);
    }

    private IWorldCollisionQuery getMapCollision() {
        return collisionMgr;
    }

    private int[][] getLightPositions()  { return config.getLightPositions(); }
    private int[][] getCameraPositions() {
        int[][] cameraPositions = new int[cctvEntities.size()][3];
        for (GameEntity cctvEntity : cctvEntities) {
            CctvComponent cctv = cctvEntity.getComponent(CctvComponent.class);
            if (cctv == null) continue;
            int i = cctv.getCameraIndex();
            if (i < 0 || i >= cameraPositions.length) continue;
            cameraPositions[i][0] = cctv.getTileCol();
            cameraPositions[i][1] = cctv.getTileRow();
            cameraPositions[i][2] = (int) cctv.getBaseAngle();
        }
        return cameraPositions;
    }

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
    // LEVEL CONFIG - reads terminal, drone, and challenge config from LevelConfig
    // =========================================================================

    private void initLevelConfig() {
        challenges      = config.createChallenges(terminal);
        KEYS_REQUIRED   = config.getKeysRequired();
        timeRemaining   = config.getTimeLimit();
        spawnDroneEntities(config.createDrones());
        spawnCctvEntities(config.getCameraPositions());
        playerStartTile = config.getPlayerStartTile();
        terminalSolved  = new boolean[terminalTiles.length];
        spawnTerminalEntities();
    }

    private void spawnDroneEntities(DroneAI[] sourceDrones) {
        droneEntities.clear();
        if (sourceDrones == null || sourceDrones.length == 0) return;

        for (int i = 0; i < sourceDrones.length; i++) {
            DroneAI drone = sourceDrones[i];
            if (drone == null) continue;

            GameEntity droneEntity = new GameEntity("Drone-" + i);
            droneEntity.addComponent(new TransformComponent(
                drone.getPosition().x, drone.getPosition().y));
            droneEntity.addComponent(new DroneComponent(drone));
            droneEntity.setCollisionRadius(drone.getRadius());
            entitySystem.addEntity(droneEntity);
            collisionSystem.registerCollidable(droneEntity, cyberCollisionResponse);
            movementSystem.addEntity(droneEntity,
                new DroneAIMovementBehaviour(getMapCollision(),
                    playerPosSnapshot, newlyChasingDrones));
            droneEntities.add(droneEntity);
        }
    }

    private void spawnTerminalEntities() {
        terminalEntities.clear();
        if (terminalTiles == null || terminalTiles.length == 0) return;

        for (int i = 0; i < terminalTiles.length; i++) {
            int tileCol = terminalTiles[i][0];
            int tileRow = terminalTiles[i][1];
            float worldX = TileMap.tileCentreX(tileCol);
            float worldY = TileMap.tileCentreY(tileRow);

            GameEntity terminalEntity = new GameEntity("Terminal-" + i);
            terminalEntity.addComponent(new TransformComponent(worldX, worldY));
            terminalEntity.addComponent(new TerminalComponent(i, tileCol, tileRow));
            terminalEntity.setCollisionRadius(TileMap.TILE_SIZE * 1.6f);
            entitySystem.addEntity(terminalEntity);
            terminalEntities.add(terminalEntity);

            collisionSystem.registerCollidable(terminalEntity, cyberCollisionResponse);
        }
    }

    private void spawnCctvEntities(int[][] cameraPositions) {
        cctvEntities.clear();
        if (cameraPositions == null || cameraPositions.length == 0) return;

        for (int i = 0; i < cameraPositions.length; i++) {
            int[] cam = cameraPositions[i];
            if (cam.length < 3) continue;
            int tileCol = cam[0];
            int tileRow = cam[1];
            float baseAngle = cam[2];

            GameEntity cctvEntity = new GameEntity("CCTV-" + i);
            cctvEntity.addComponent(new TransformComponent(
                TileMap.tileCentreX(tileCol), TileMap.tileCentreY(tileRow)));
            cctvEntity.addComponent(new CctvComponent(i, tileCol, tileRow, baseAngle));
            entitySystem.addEntity(cctvEntity);
            cctvEntities.add(cctvEntity);
        }
    }

    private java.util.List<DroneAI> getDrones() {
        java.util.List<DroneAI> drones = new java.util.ArrayList<>(droneEntities.size());
        for (GameEntity droneEntity : droneEntities) {
            DroneComponent droneComponent = droneEntity.getComponent(DroneComponent.class);
            if (droneComponent == null || droneComponent.getDrone() == null) continue;
            drones.add(droneComponent.getDrone());
        }
        return drones;
    }

    private DroneAI[] getDroneArray() {
        java.util.List<DroneAI> drones = getDrones();
        return drones.toArray(new DroneAI[0]);
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
        playerEntity.setCollisionRadius(PLAYER_RADIUS);

        entitySystem.addEntity(playerEntity);
        collisionSystem.registerCollidable(playerEntity, cyberCollisionResponse);
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
        cctvAlerted = new boolean[cctvEntities.size()];
        resetDroneAwareness(2.6f);
        setupClueObjects();
        showBanner(config.getLevelName(), config.getIntroSubtitle(), 5.8f);
    }

    /**
     * Set up clue objects scattered around the level.
     * Players must collect these intel fragments before accessing
     * later terminals, turning the game into a multi-room exploration.
     */
    private void setupClueObjects() {
        clueSystem.reset();
        // Reset + unregister old clue trigger entities (scene reload safety)
        for (GameEntity e : clueEntities) {
            collisionSystem.unregisterCollidable(e);
        }
        clueEntities.clear();
        clueByEntity.clear();

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

        // Build collision trigger entities for each clue object so interactions are driven
        // by the CollisionSystem rather than manual distance checks.
        for (ClueSystem.ClueObject clueObj : clueSystem.getClueObjects()) {
            GameEntity clueEntity = new GameEntity("Clue-" + clueObj.clueId);
            clueEntity.addComponent(new TransformComponent(
                TileMap.tileCentreX(clueObj.tileCol),
                TileMap.tileCentreY(clueObj.tileRow)
            ));
            clueEntity.setCollisionRadius(TileMap.TILE_SIZE * 2.0f);
            entitySystem.addEntity(clueEntity);
            collisionSystem.registerCollidable(clueEntity, cyberCollisionResponse);
            clueEntities.add(clueEntity);
            clueByEntity.put(clueEntity, clueObj);
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
        java.util.List<DroneAI> drones = getDrones();
        if (!drones.isEmpty()) {
            for (DroneAI drone : drones) {
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
            if (getMapCollision() != null && getMapCollision().isWall(col, row)) continue;
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
                    if (getMapCollision() != null && getMapCollision().isWall(col, row)) continue;
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
        if (getMapCollision() == null) return true;
        if (getMapCollision().isWall(targetCol, targetRow)) return false;

        int startCol = playerStartTile[0];
        int startRow = playerStartTile[1];
        if (getMapCollision().isWall(startCol, startRow)) return false;
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
                if (getMapCollision().isWall(nextCol, nextRow)) continue;
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
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void resetDroneAwareness(float suppressSeconds) {
        for (DroneAI drone : getDrones()) {
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
        if (duration >= bannerTimer) {
            bannerDuration = duration;
            bannerTimer = duration;
        }
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
        if (renderer != null) renderer.followCamera(checkpointX, checkpointY);
    }

    private float[] findSafeRespawnPoint(float desiredX, float desiredY) {
        int startCol = TileMap.worldToCol(desiredX);
        int startRow = TileMap.worldToRow(desiredY);
        float[] best = new float[]{TileMap.tileCentreX(startCol), TileMap.tileCentreY(startRow)};
        float bestScore = -Float.MAX_VALUE;

        for (int radius = 0; radius <= 6; radius++) {
            for (int row = Math.max(0, startRow - radius); row <= Math.min(TileMap.ROWS - 1, startRow + radius); row++) {
                for (int col = Math.max(0, startCol - radius); col <= Math.min(TileMap.COLS - 1, startCol + radius); col++) {
                    if (getMapCollision().isWall(col, row)) continue;
                    boolean terminalTile = false;
                    for (GameEntity terminalEntity : terminalEntities) {
                        TerminalComponent terminalComponent = terminalEntity.getComponent(TerminalComponent.class);
                        if (terminalComponent != null
                            && terminalComponent.getTileCol() == col
                            && terminalComponent.getTileRow() == row) {
                            terminalTile = true;
                            break;
                        }
                    }
                    if (terminalTile) continue;

                    float worldX = TileMap.tileCentreX(col);
                    float worldY = TileMap.tileCentreY(row);
                    float[] resolved = getMapCollision().resolveCircleVsWalls(worldX, worldY, PLAYER_RADIUS);
                    if (Math.abs(resolved[0] - worldX) > 0.75f || Math.abs(resolved[1] - worldY) > 0.75f) continue;

                    float droneSeparation = Float.MAX_VALUE;
                    float losPenalty = 0f;
                    java.util.List<DroneAI> drones = getDrones();
                    for (DroneAI drone : drones) {
                        droneSeparation = Math.min(droneSeparation, dist(worldX, worldY, drone.getPosition().x, drone.getPosition().y));
                        if (getMapCollision().hasLineOfSight(worldX, worldY, drone.getPosition().x, drone.getPosition().y)) {
                            losPenalty += 90f;
                        }
                    }
                    if (drones.isEmpty()) droneSeparation = 9999f;

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
        frameDelta = delta;
        stateTime += delta;
        if (!gameOver && !victory) missionElapsed += delta;
        newlyChasingDrones.clear();
        pendingDroneCatch = false;
        interactPressedThisFrame = input.isActionJustPressed("INTERACT");
        // Reset collision-driven proximity state for this frame. It will be
        // repopulated during the CollisionSystem step before onLateUpdate.
        nearbyTerminalIdx = -1;
        nearbyTerminalDist2 = Float.POSITIVE_INFINITY;
        nearbyClueEntity = null;
        overlappingExit = false;

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
                
                // Tells the engine to stop routing typing to the minigame
                input.clearTextInputListener();
                
                if (activeChallenge.isSolved()) {
                    terminalSolved[activeChallengeIdx] = true;
                    if (activeChallengeIdx >= 0 && activeChallengeIdx < terminalEntities.size()) {
                        TerminalComponent terminalComponent =
                            terminalEntities.get(activeChallengeIdx).getComponent(TerminalComponent.class);
                        if (terminalComponent != null) terminalComponent.setSolved(true);
                    }
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

        // Player state tracking
        if (tc != null) {
            PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
            float vx = phys != null ? phys.getVelocity().x : 0f;
            float vy = phys != null ? phys.getVelocity().y : 0f;
            boolean isMoving = Math.abs(vx) > 0.5f || Math.abs(vy) > 0.5f;

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

        // INTERACT key handling moved to onLateUpdate (collision-driven)

        if (tc != null) {
            // Snapshot used by drone movement behaviours.
            // MovementSystem updates *after* this onUpdate, so this must reflect
            // the player's pre-movement position for frame parity.
            playerPosSnapshot.set(tc.getPosition().x, tc.getPosition().y);

            // CCTV detection: camera entities spot the player and attract drones
            cctvAlertCooldown = Math.max(0f, cctvAlertCooldown - delta);
            boolean anyCameraVisible = cctvDetectionSystem.updateAlerts(
                cctvEntities, cctvAlerted, tc.getPosition(), getMapCollision(),
                TileMap.TILE_SIZE, stateTime);
            if (anyCameraVisible && cctvAlertCooldown <= 0f) {
                boolean anyDispatched = false;
                for (DroneAI d : getDrones()) {
                    float offsetX = (float) (Math.random() * 128 - 64);
                    float offsetY = (float) (Math.random() * 128 - 64);
                    d.transitionTo(new SearchState(
                        tc.getPosition().x + offsetX,
                        tc.getPosition().y + offsetY,
                        4.5f
                    ));
                    anyDispatched = true;
                }
                if (anyDispatched) {
                    cctvAlertCooldown = CCTV_ALERT_INTERVAL;
                    showBanner("SURVEILLANCE TRIGGERED",
                        "Camera feed compromised. All drone units converging on your position.",
                        2.2f);
                }
            }
        }

        // Exit trigger handled in onLateUpdate (collision-driven)

        if (tc != null && renderer != null) {
            renderer.followCamera(tc.getPosition().x, tc.getPosition().y);
        }
    }

    // =========================================================================
    // RENDER
    // =========================================================================
    @Override
    protected void onRender() {
        if (renderer == null) return;

        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;

        DroneAI[] currentDrones = getDroneArray();
        int[][] cameraPositions = getCameraPositions();

        String[] challengeTitles = buildChallengeTitles();
        int nearbyIdx = nearbyTerminalIdx;

        renderer.render(
            stateTime,
            missionElapsed,
            timeRemaining,
            gameOver,
            victory,
            exitUnlocked,
            chaseWarningTimer,
            bannerTimer,
            bannerDuration,
            bannerTitle,
            bannerSubtitle,
            transitionAlpha,
            keysCollected,
            KEYS_REQUIRED,
            respawnsRemaining,
            maxRespawns,
            signalPingsRemaining,
            clueSystem,
            playerState,
            terminalPingTimer,
            checkpointX,
            checkpointY,
            tmxExitX,
            tmxExitY,
            PING_REVEAL_RADIUS,
            terminalSolved,
            terminalTiles,
            cctvAlerted,
            cameraPositions,
            currentDrones,
            playerEntity,
            tc,
            getMapCollision(),
            pX,
            pY,
            pR,
            pG,
            pB,
            pLife,
            particleCount,
            frameDelta,
            activeChallenge,
            nearbyIdx,
            challengeTitles
        );
    }

    // Particles
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

    @Override
    protected void onLateUpdate(float delta) {
        // Process drone state transitions (MovementSystem ran already).
        if (!gameOver && !victory) {
            for (DroneAI drone : newlyChasingDrones) {
                spawnParticles(drone.getPosition().x, drone.getPosition().y,
                    1f, 0.1f, 0f, 12);
                chaseWarningTimer = 1.2f;
            }
            newlyChasingDrones.clear();

            // Process collision-based catch after CollisionSystem ran already.
            if (pendingDroneCatch) {
                pendingDroneCatch = false;
                handleDroneCatch();
            }
        } else {
            newlyChasingDrones.clear();
            pendingDroneCatch = false;
        }

        // Collision-driven interactions (CollisionSystem ran already).
        if (!gameOver && !victory && interactPressedThisFrame) {
            boolean interacted = false;

            // Clue pickup has priority.
            if (nearbyClueEntity != null) {
                ClueSystem.ClueObject clueObj = clueByEntity.get(nearbyClueEntity);
                if (clueObj != null && !clueObj.collected) {
                    clueObj.collected = true;
                    clueSystem.collectClue(clueObj.clueId, clueObj.clueTitle,
                        clueObj.clueDescription, missionElapsed);
                    scanAnimTimer = 1.2f;
                    float cx = TileMap.tileCentreX(clueObj.tileCol);
                    float cy = TileMap.tileCentreY(clueObj.tileRow);
                    spawnParticles(cx, cy, 0.1f, 0.85f, 1f, 12);
                    showBanner("INTEL ACQUIRED", clueObj.objectName + ": " + clueObj.clueTitle, 2.5f);

                    // Remove trigger so it no longer participates in collisions.
                    collisionSystem.unregisterCollidable(nearbyClueEntity);
                    nearbyClueEntity.deactivate();
                    interacted = true;
                }
            }

            // Terminal interaction.
            if (!interacted && nearbyTerminalIdx >= 0 && nearbyTerminalIdx < terminalEntities.size()) {
                int i = nearbyTerminalIdx;
                if (terminalSolved != null && i < terminalSolved.length && terminalSolved[i]) {
                    // already solved; no-op
                } else if (!clueSystem.canAccessTerminal(i, terminalTiles != null ? terminalTiles.length : 0)) {
                    String hint = clueSystem.getTerminalLockHint(i);
                    showBanner("ACCESS DENIED", hint != null ? hint : "Insufficient intel clearance.", 2.0f);
                } else {
                    activeChallenge = challenges[i];
                    activeChallengeIdx = i;
                    activeChallenge.open();
                    input.setTextInputListener(activeChallenge);
                }
            }
        }
        interactPressedThisFrame = false;

        // Exit trigger -> victory.
        if (!gameOver && !victory && exitUnlocked && overlappingExit) {
            victory = true;
            return;
        }

        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;
        float r = PLAYER_RADIUS;
        tc.getPosition().set(
            Math.max(TileMap.TILE_SIZE + r, Math.min(TileMap.WORLD_W - TileMap.TILE_SIZE - r, tc.getPosition().x)),
            Math.max(TileMap.TILE_SIZE + r, Math.min(TileMap.WORLD_H - TileMap.TILE_SIZE - r, tc.getPosition().y)));
    }

    @Override public void resize(int w, int h) {
        if (renderer != null) renderer.resize(w, h);
    }

    @Override protected void onUnload() {
        System.out.println("CyberGame unloading level " + config.getLevelNumber());
    }
    
    @Override protected void onDispose() {
        if (activeChallenge != null && activeChallenge.isOpen()) {
            activeChallenge.close();
            input.clearTextInputListener(); // Ensure cleanup on scene destruction
        }
        if (renderer != null) renderer.dispose();
        if (mapRuntime != null) mapRuntime.dispose();
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