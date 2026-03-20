package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import io.github.INF1009_P10_Team7.simulation.cyber.SpriteAnimator;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.simulation.cyber.TileType;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.NmapReconChallenge;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.SqlInjectionChallenge;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.*;
import io.github.INF1009_P10_Team7.simulation.cyber.observer.GameEventSystem;
import io.github.INF1009_P10_Team7.simulation.cyber.FontManager;

/**
 * CyberGameScene  -  main gameplay scene for all 5 levels.
 *
 * Fixes applied:
 *  BUG-3: createVictoryScene now passes actual KEYS_REQUIRED.
 *  BUG-4: Drones receive level-appropriate waypoints.
 *  BUG-5: initLevelConfig handles cases 4 and 5.
 *
 * Improvements:
 *  6. Levels 4 & 5 with unique maps, drone configs.
 *  7. PacketSnifferGame and FirewallACLGame replace duplicates.
 *  8. Screen transition effect on scene entry.
 *  9. HUD minimap showing player, terminals, exit.
 * 10. Directional threat indicator toward nearest drone.
 * 11. Particle effects for hack success, drone alert, exit unlock.
 */
public class CyberGameScene extends Scene {

    private final IEntitySystem    entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem  movementSystem;
    private final CyberSceneFactory factory;
    private final int level;

    private final CyberSprites sprites = new CyberSprites();
    private SpriteAnimator playerAnimator;

    private ShapeRenderer sr;
    private SpriteBatch   batch;
    private BitmapFont hudFont, alertFont, promptFont;
    private GlyphLayout layout;

    private static final float VIEW_W = 640f;
    private static final float VIEW_H = 352f;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private OrthographicCamera hudCamera;
    private Viewport           hudViewport;

    private TileMap tileMap;
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
    private int signalPingsRemaining;
    private int hintsUsed = 0;
    private String bannerTitle = "";
    private String bannerSubtitle = "";
    private float bannerTimer = 0f;

    // ── Screen transition (Improvement 8) ────────────────────────────────────
    private float transitionAlpha = 1f;  // starts black, fades in

    // ── Particle effects (Improvement 11) ────────────────────────────────────
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

    private static final String[] LEVEL_NAMES = {
        "LEVEL 1  -  RECON LAB",
        "LEVEL 2  -  NETWORK HUB",
        "LEVEL 3  -  SERVER FARM",
        "LEVEL 4  -  DATA CENTER",
        "LEVEL 5  -  BLACK SITE",
    };

    public CyberGameScene(IInputController input, IAudioController audio,
                          SceneNavigator nav,
                          IEntitySystem entitySystem,
                          ICollisionSystem collisionSystem,
                          IMovementSystem movementSystem,
                          CyberSceneFactory factory,
                          int level) {
        super(input, audio, nav);
        this.entitySystem    = entitySystem;
        this.collisionSystem = collisionSystem;
        this.movementSystem  = movementSystem;
        this.factory         = factory;
        this.level           = Math.max(1, Math.min(level, 5));
    }

    public CyberGameScene(IInputController input, IAudioController audio,
                          SceneNavigator nav,
                          IEntitySystem entitySystem,
                          ICollisionSystem collisionSystem,
                          IMovementSystem movementSystem,
                          CyberSceneFactory factory) {
        this(input, audio, nav, entitySystem, collisionSystem, movementSystem, factory, 1);
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

        hudFont    = makeBitmapFont(1.1f);
        alertFont  = makeBitmapFont(1.5f);
        promptFont = makeBitmapFont(1.0f);

        tileMap = new TileMap(level);
        tileMap.loadTileset();

        sprites.load();
        playerAnimator = new SpriteAnimator("niceguy.png", 9, 4, 64, 64, 0.10f);
        eventSystem.addObserver(inventory);
        initLevelConfig();
        createPlayer();
        setupSupportSystems();
        audio.setMusic("Music_Game.mp3");

        transitionAlpha = 1f;  // fade-in from black
        missionElapsed = 0f;
    }

    private BitmapFont makeBitmapFont(float scale) {
        return FontManager.create(scale);
    }

    // =========================================================================
    // LEVEL CONFIG  -  all 5 levels with unique minigame mixes and drone waypoints
    // =========================================================================
    private void initLevelConfig() {
        switch (level) {

            case 1:
                terminalTiles  = new int[][]{ {19,4}, {6,12}, {20,11}, {32,12}, {19,18} };
                challenges     = new IMiniGame[]{
                    new BinaryDecodeGame(),
                    new CaesarCipherGame(),
                    new PortMatchGame(),
                    new LogAnalysisGame(),
                    new TerminalMiniGame(new NmapReconChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 360f;
                drones         = new DroneAI[]{};
                playerStartTile = new int[]{ 19, 12 };
                break;

            case 2:
                terminalTiles  = new int[][]{ {7,5}, {31,5}, {19,11}, {7,17}, {31,17} };
                challenges     = new IMiniGame[]{
                    new BinaryDecodeGame(),
                    new PacketSnifferGame(),
                    new PortMatchGame(),
                    new LogAnalysisGame(),
                    new TerminalMiniGame(new SqlInjectionChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 390f;
                // BUG-4 FIX: level-appropriate waypoints
                // DRONE-WALL FIX: waypoints moved into open corridor tiles (cols 11-28, rows 10-12)
                // MAP_2 corridor (room 9) runs cols 10-29, rows 9-13.
                // Spawn drones at tile centres well inside open floor to avoid wall embedding.
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(13),  TileMap.tileCentreY(11),
                        new float[][]{ {11,10}, {17,10}, {17,12}, {11,12} }),
                    new DroneAI(TileMap.tileCentreX(25), TileMap.tileCentreY(11),
                        new float[][]{ {22,10}, {28,10}, {28,12}, {22,12} })
                };
                playerStartTile = new int[]{ 19, 11 };
                break;

            case 3:
                terminalTiles  = new int[][]{ {6,4}, {32,4}, {19,10}, {6,16}, {32,16} };
                challenges     = new IMiniGame[]{
                    new CaesarCipherGame(),
                    new PacketSnifferGame(),
                    new FirewallACLGame(),
                    new LogAnalysisGame(),
                    new TerminalMiniGame(new NmapReconChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 420f;
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(7),  TileMap.tileCentreY(6),
                        new float[][]{ {3,2}, {12,2}, {12,7}, {3,7} }),
                    new DroneAI(TileMap.tileCentreX(32), TileMap.tileCentreY(6),
                        new float[][]{ {27,2}, {37,2}, {37,7}, {27,7} }),
                    new DroneAI(TileMap.tileCentreX(19), TileMap.tileCentreY(14),
                        new float[][]{ {8,13}, {16,13}, {24,13}, {32,13} })
                };
                playerStartTile = new int[]{ 19, 10 };
                break;

            // ── Level 4: DATA CENTER  -  H-layout ────────────────────────────
            case 4:
                terminalTiles  = new int[][]{ {5,3}, {36,3}, {19,10}, {5,16}, {36,16} };
                challenges     = new IMiniGame[]{
                    new PacketSnifferGame(),
                    new FirewallACLGame(),
                    new CaesarCipherGame(),
                    new OsintHuntGame("OSINT: CVE LOOKUP",
                        "https://nvd.nist.gov/vuln/detail/CVE-2017-0144",
                        "What Microsoft Security Bulletin number covers EternalBlue?",
                        "Look at the 'References to Advisories' section",
                        "MS17-010"),
                    new TerminalMiniGame(new SqlInjectionChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 450f;
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(19), TileMap.tileCentreY(4),
                        new float[][]{ {18,2}, {21,2}, {21,7}, {18,7} },
                        42f, 64f, 116f, 82f),
                    new DroneAI(TileMap.tileCentreX(9),  TileMap.tileCentreY(10),
                        new float[][]{ {9,9}, {9,12}, {19,12}, {19,9} },
                        41f, 62f, 112f, 80f),
                    new DroneAI(TileMap.tileCentreX(19), TileMap.tileCentreY(17),
                        new float[][]{ {18,15}, {21,15}, {21,20}, {18,20} },
                        44f, 68f, 120f, 84f)
                };
                playerStartTile = new int[]{ 19, 10 };
                break;

            // ── Level 5: BLACK SITE  -  Ring layout ──────────────────────────
            case 5:
                terminalTiles  = new int[][]{ {5,3}, {35,3}, {19,9}, {5,17}, {35,17} };
                challenges     = new IMiniGame[]{
                    new FirewallACLGame(),
                    new PacketSnifferGame(),
                    new BinaryDecodeGame(),
                    new OsintHuntGame("OSINT: OWASP",
                        "https://owasp.org/Top10",
                        "What is the #1 vulnerability in the OWASP Top 10 (2021)?",
                        "Check the numbered list on the OWASP Top 10 page",
                        "broken access control"),
                    new TerminalMiniGame(new NmapReconChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 480f;
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(5),  TileMap.tileCentreY(1),
                        new float[][]{ {1,1}, {9,1}, {9,5}, {1,5} },
                        42f, 64f, 116f, 80f),
                    new DroneAI(TileMap.tileCentreX(35), TileMap.tileCentreY(1),
                        new float[][]{ {31,1}, {38,1}, {38,5}, {31,5} },
                        42f, 64f, 116f, 80f),
                    new DroneAI(TileMap.tileCentreX(5),  TileMap.tileCentreY(15),
                        new float[][]{ {1,15}, {9,15}, {9,20}, {1,20} },
                        43f, 66f, 118f, 80f),
                    new DroneAI(TileMap.tileCentreX(35), TileMap.tileCentreY(15),
                        new float[][]{ {31,15}, {38,15}, {38,20}, {31,20} },
                        48f, 78f, 130f, 86f)
                };
                playerStartTile = new int[]{ 19, 9 };
                break;

            default:
                // Shouldn't happen  -  level is clamped 1-5, but fallback to level 1
                terminalTiles  = new int[][]{ {19,4}, {6,12}, {20,11}, {32,12}, {19,18} };
                challenges     = new IMiniGame[]{ new BinaryDecodeGame(), new CaesarCipherGame(),
                    new PortMatchGame(), new LogAnalysisGame(),
                    new TerminalMiniGame(new NmapReconChallenge(), terminal) };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 240f;
                drones         = new DroneAI[]{};
                playerStartTile = new int[]{ 19, 12 };
                break;
        }

        terminalSolved = new boolean[terminalTiles.length];
        for (int i = 0; i < terminalSolved.length; i++) terminalSolved[i] = false;
    }

    private void createPlayer() {
        playerEntity = new GameEntity("CyberPlayer");
        float startX = TileMap.tileCentreX(playerStartTile[0]);
        float startY = TileMap.tileCentreY(playerStartTile[1]);
        float[] safeStart = tileMap.resolveCircleVsWalls(startX, startY, PLAYER_RADIUS);
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
        maxRespawns = (level <= 2) ? 5 : (level == 3 ? 4 : 3);
        respawnsRemaining = maxRespawns;
        signalPingsRemaining = (level <= 2) ? 4 : 3;
        protectionTimer = 2.6f;
        resetDroneAwareness(2.6f);
        String introSubtitle;
        switch (level) {
            case 1: introSubtitle = "No drones yet. Learn the terminals, checkpoints, and signal ping."; break;
            case 2: introSubtitle = "Break line of sight at corners and do not rush into the center lane."; break;
            case 3: introSubtitle = "Use pings to route efficiently. Every two terminals can restore one life."; break;
            case 4: introSubtitle = "Data Center drones are tougher. Play around checkpoints and stay patient."; break;
            default: introSubtitle = "Black Site alert level is high. Cloak after respawn and chain your hacks."; break;
        }
        showBanner(LEVEL_NAMES[Math.max(0, Math.min(level - 1, LEVEL_NAMES.length - 1))], introSubtitle, 5.8f);
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
        showBanner("CHECKPOINT SYNCED", "Progress saved. Respawns now snap to the nearest safe floor tile.", 2.8f);
    }

    private void showBanner(String title, String subtitle, float duration) {
        bannerTitle = title != null ? title : "";
        bannerSubtitle = subtitle != null ? subtitle : "";
        bannerTimer = Math.max(bannerTimer, duration);
    }

    private void triggerSignalPing() {
        if (signalPingsRemaining <= 0) {
            showBanner("NO SIGNAL PINGS LEFT", "Use the minimap, terminal glow, and threat arrow.", 2.1f);
            return;
        }

        signalPingsRemaining--;
        hintsUsed++;
        terminalPingTimer = 6.5f;
        showBanner("SIGNAL PING ONLINE", "Nearest objective highlighted for 6.5 seconds. No time penalty.", 2.8f);
    }

    private void respawnAtCheckpoint() {
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;

        float[] safePoint = findSafeRespawnPoint(checkpointX, checkpointY);
        checkpointX = safePoint[0];
        checkpointY = safePoint[1];

        tc.getPosition().set(checkpointX, checkpointY);
        float[] resolved = tileMap.resolveCircleVsWalls(tc.getPosition().x, tc.getPosition().y, PLAYER_RADIUS);
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
                    if (tileMap.isWall(col, row)) continue;
                    TileType type = tileMap.getType(col, row);
                    if (type == TileType.EXIT) continue;
                    boolean terminalTile = false;
                    for (int[] terminalTilePos : terminalTiles) {
                        if (terminalTilePos[0] == col && terminalTilePos[1] == row) { terminalTile = true; break; }
                    }
                    if (terminalTile) continue;

                    float worldX = TileMap.tileCentreX(col);
                    float worldY = TileMap.tileCentreY(row);
                    float[] resolved = tileMap.resolveCircleVsWalls(worldX, worldY, PLAYER_RADIUS);
                    if (Math.abs(resolved[0] - worldX) > 0.75f || Math.abs(resolved[1] - worldY) > 0.75f) continue;

                    float droneSeparation = Float.MAX_VALUE;
                    float losPenalty = 0f;
                    for (DroneAI drone : drones) {
                        droneSeparation = Math.min(droneSeparation, dist(worldX, worldY, drone.getPosition().x, drone.getPosition().y));
                        if (tileMap.hasLineOfSight(worldX, worldY, drone.getPosition().x, drone.getPosition().y)) {
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
        if (respawnsRemaining > 0) {
            respawnsRemaining--;
            respawnsUsed++;
            respawnAtCheckpoint();
            showBanner("INTEGRITY BREACH", "Respawned at checkpoint. Cloak active briefly  -  break line of sight and re-route.", 2.9f);
            return;
        }
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
        for (int row = 0; row < TileMap.ROWS; row++) {
            for (int col = 0; col < TileMap.COLS; col++) {
                if (tileMap.getType(col, row) == TileType.EXIT) {
                    return new float[]{TileMap.tileCentreX(col), TileMap.tileCentreY(row)};
                }
            }
        }
        return new float[]{TileMap.WORLD_W - 64f, TileMap.WORLD_H - 64f};
    }

    private void maybeRestoreIntegrity() {
        if (respawnsRemaining < maxRespawns && keysCollected > 0 && keysCollected % 2 == 0) {
            respawnsRemaining++;
            spawnParticles(checkpointX, checkpointY, 0.25f, 1f, 0.85f, 18);
            showBanner("INTEGRITY RESTORED", "+1 life granted for securing two terminals.", 2.5f);
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================
    @Override
    protected void onUpdate(float delta) {
        stateTime += delta;
        rotorAngle += delta * 200f;
        missionElapsed += delta;

        // Screen transition fade-in
        if (transitionAlpha > 0f) transitionAlpha = Math.max(0f, transitionAlpha - delta * 1.8f);

        // Particle update
        updateParticles(delta);
        if (chaseWarningTimer > 0f) chaseWarningTimer = Math.max(0f, chaseWarningTimer - delta);
        if (protectionTimer > 0f) protectionTimer = Math.max(0f, protectionTimer - delta);
        if (terminalPingTimer > 0f) terminalPingTimer = Math.max(0f, terminalPingTimer - delta);
        if (bannerTimer > 0f) bannerTimer = Math.max(0f, bannerTimer - delta);

        if (gameOver || victory) {
            if (input.isActionJustPressed("INTERACT") || input.isActionJustPressed("MENU_CONFIRM") || input.isActionJustPressed("START_GAME")) {
                if (victory)
                    nav.requestScene(factory.createVictoryScene(keysCollected, KEYS_REQUIRED, (int) missionElapsed, level, respawnsUsed, hintsUsed));
                else
                    nav.requestScene(factory.createGameOverScene(level));
            }
            return;
        }

        if (activeChallenge != null && activeChallenge.isOpen()) {
            activeChallenge.update(delta);
            if (!activeChallenge.isOpen()) {
                if (activeChallenge.isSolved()) {
                    terminalSolved[activeChallengeIdx] = true;
                    keysCollected++;
                    eventSystem.notifyKeyCollected(keysCollected, KEYS_REQUIRED);
                    tileMap.setTile(terminalTiles[activeChallengeIdx][0],
                                    terminalTiles[activeChallengeIdx][1], TileType.FLOOR);
                    // Particle burst: green sparks
                    spawnParticles(TileMap.tileCentreX(terminalTiles[activeChallengeIdx][0]),
                                   TileMap.tileCentreY(terminalTiles[activeChallengeIdx][1]),
                                   0f, 1f, 0.4f, 16);
                    setCheckpoint(TileMap.tileCentreX(terminalTiles[activeChallengeIdx][0]),
                                   TileMap.tileCentreY(terminalTiles[activeChallengeIdx][1]));
                    protectionTimer = Math.max(protectionTimer, 1.75f);
                    resetDroneAwareness(protectionTimer);
                    showBanner("TERMINAL SECURED", challenges[activeChallengeIdx].getTitle() + " cleared. Key " + keysCollected + " / " + KEYS_REQUIRED + " acquired.", 2.3f);
                    maybeRestoreIntegrity();
                    if (keysCollected >= KEYS_REQUIRED) {
                        exitUnlocked = true;
                        showBanner("EXIT UNLOCKED", "All terminals secured. Reach the magenta extraction door.", 3.0f);
                        // Purple energy burst at exit
                        for (int r = 0; r < TileMap.ROWS; r++)
                            for (int c = 0; c < TileMap.COLS; c++)
                                if (tileMap.getType(c, r) == TileType.EXIT)
                                    spawnParticles(TileMap.tileCentreX(c), TileMap.tileCentreY(r),
                                                   0.7f, 0f, 1f, 20);
                    }
                }
                if (activeChallenge != null && !activeChallenge.isSolved()) {
                    String msg = activeChallenge.wasPanicked() ? "Disconnected safely. Re-enter when the area is clear." : "You can re-enter the terminal whenever you are ready.";
            showBanner("TERMINAL DISCONNECTED", msg, 1.8f);
                }
                activeChallenge = null; activeChallengeIdx = -1;
            }
            return;
        }

        if (input.isActionJustPressed("SETTINGS")) {
            nav.pushScene(factory.createSettingsScene()); return;
        }
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene()); return;
        }
        if (input.isActionJustPressed("HELP")) {
            triggerSignalPing();
        }

        tileMap.update(delta);

        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            float[] resolved = tileMap.resolveCircleVsWalls(
                tc.getPosition().x, tc.getPosition().y, PLAYER_RADIUS);
            tc.getPosition().set(resolved[0], resolved[1]);
        }

        if (input.isActionJustPressed("INTERACT") && tc != null) {
            Vector2 pp = tc.getPosition();
            for (int i = 0; i < terminalTiles.length; i++) {
                if (terminalSolved[i]) continue;
                float tx = TileMap.tileCentreX(terminalTiles[i][0]);
                float ty = TileMap.tileCentreY(terminalTiles[i][1]);
                if (dist(pp.x, pp.y, tx, ty) < TileMap.TILE_SIZE * 2.2f) {
                    activeChallenge = challenges[i];
                    activeChallengeIdx = i;
                    activeChallenge.open();
                    break;
                }
            }
        }

        if (tc != null) {
            for (DroneAI drone : drones) {
                boolean wasChasing = "CHASE".equals(drone.getStateName());
                drone.update(tileMap, tc.getPosition(), delta);

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
        }

        if (exitUnlocked && tc != null) {
            for (int row = 0; row < TileMap.ROWS; row++) {
                for (int col = 0; col < TileMap.COLS; col++) {
                    if (tileMap.getType(col, row) != TileType.EXIT) continue;
                    float ex = TileMap.tileCentreX(col), ey = TileMap.tileCentreY(row);
                    if (dist(tc.getPosition().x, tc.getPosition().y, ex, ey)
                            < TileMap.TILE_SIZE * 1.5f) {
                        victory = true; return;
                    }
                }
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

        tileMap.render(sr, batch, exitUnlocked);
        renderRoomProps();          // FIX: render unused assets as room decorations
        renderCheckpointBeacon();
        renderDronePatrolRoutes();
        renderTerminalGlow();
        renderTerminalHints();
        renderExitGuidance();

        for (DroneAI drone : drones) drone.render(sr);

        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        renderPlayer(tc);
        renderParticles();    // Improvement 11
        renderAtmosphere();

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
        renderHUD();
        renderMinimap(tc);             // Improvement 9
        renderThreatIndicator(tc);     // Improvement 10
        renderChaseWarning();
        renderObjectiveBanner();

        if (gameOver || victory) renderEndScreen(victory);

        // Screen transition overlay (Improvement 8)
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
     * Purposeful usage of every previously-unused sprite asset:
     *
     *  ceiling_light.png  – A glowing light pool is drawn on the FLOOR directly
     *                       beneath the ceiling fixture sprite.  The pool radius
     *                       flickers slightly so it looks like a real lamp.
     *                       The fixture sprite itself is drawn at the top of the
     *                       pool so it reads as "mounted overhead".
     *
     *  barrier_large/small– Physical cover crates rendered in corridors.  A dark
     *                       shadow ellipse is drawn under each one so they sit on
     *                       the ground convincingly.  Players can hide behind them.
     *
     *  sec_camera.png     – Mounted on the wall ABOVE each corridor entrance
     *                       (one tile above the doorway).  The sprite rotates
     *                       slowly back and forth (±40°) like a real CCTV pan,
     *                       and a translucent scan-cone is drawn in front of it
     *                       so the player understands it has a field of view.
     *
     *  hunter.png         – Replaces the drone's geometric hexagon completely.
     *                       Drawn rotated to match the drone's facing angle so it
     *                       always looks like it is walking/flying toward its target.
     *                       Tinted blue (patrol) or red (chase) for instant readability.
     *
     *  phone_wifi.png     – A pulsing wifi signal badge rendered just above the
     *                       terminal sprite when the player walks within interact
     *                       range.  It communicates "this device is broadcasting –
     *                       get close to hack it" without any text.
     *
     *  camera.png         – Small icon prefix in the HUD alert bar, replacing the
     *                       plain "ALERT" text label so the bar is instantly
     *                       recognisable as a "camera / surveillance" meter.
     *
     *  map_pin.png        – Replaces the plain green circle terminal markers on
     *                       the minimap so unsolved terminals look like map pins.
     */
    private void renderRoomProps() {
        float ts = TileMap.TILE_SIZE;

        // ── 1. Ceiling lights: floor glow pool + mounted fixture sprite ────────
        renderCeilingLights(ts);

        // ── 2. Barrier crates with shadows ────────────────────────────────────
        renderBarriers(ts);

        // ── 3. Security cameras mounted above corridor doorways ───────────────
        renderSecurityCameras(ts);

        // ── 4. Hunter sprite as drone body ────────────────────────────────────
        renderDroneSprites(ts);

        // ── 5. Wifi badge near terminal when player is close ──────────────────
        renderTerminalWifiBadge(ts);
    }

    /** Ceiling light: draws a soft glow pool on the floor, then the fixture above it. */
    private void renderCeilingLights(float ts) {
        int[][] lights = getLightPositions();
        float flicker = 0.72f + 0.08f * (float)Math.sin(stateTime * 2.3f)
                               + 0.04f * (float)Math.sin(stateTime * 7.1f);

        // Draw floor glow pools with ShapeRenderer (filled circles, additive feel)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int[] lt : lights) {
            float cx = TileMap.tileCentreX(lt[0]);
            float cy = TileMap.tileCentreY(lt[1]);
            // Outer dim halo
            sr.setColor(0.9f, 0.95f, 0.6f, 0.07f * flicker);
            sr.circle(cx, cy, ts * 2.1f, 28);
            // Inner bright pool
            sr.setColor(0.95f, 1f, 0.75f, 0.16f * flicker);
            sr.circle(cx, cy, ts * 1.2f, 22);
            // Hot centre
            sr.setColor(1f, 1f, 0.9f, 0.22f * flicker);
            sr.circle(cx, cy, ts * 0.55f, 16);
        }
        sr.end();

        // Draw the fixture sprite itself just above the pool centre
        if (sprites.ceilingLight != null) {
            batch.begin();
            for (int[] lt : lights) {
                float cx = TileMap.tileCentreX(lt[0]);
                float cy = TileMap.tileCentreY(lt[1]);
                sprites.drawCentered(batch, sprites.ceilingLight,
                    cx, cy + ts * 0.35f,          // slightly above pool centre
                    ts * 0.7f, 0.85f * flicker);
            }
            batch.end();
        }
    }

    /** Barrier crates: shadow ellipse on ground + sprite on top. */
    private void renderBarriers(float ts) {
        int[][] barriers = getBarrierPositions();

        // Shadow pass
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < barriers.length; i++) {
            float bx = TileMap.tileCentreX(barriers[i][0]);
            float by = TileMap.tileCentreY(barriers[i][1]);
            boolean large = (i % 3 != 2);
            float shadowW = large ? ts * 0.95f : ts * 0.70f;
            sr.setColor(0f, 0f, 0f, 0.38f);
            sr.ellipse(bx - shadowW * 0.5f, by - ts * 0.22f, shadowW, ts * 0.28f);
        }
        sr.end();

        // Sprite pass
        batch.begin();
        for (int i = 0; i < barriers.length; i++) {
            boolean large = (i % 3 != 2);
            Texture bTex = large ? sprites.barrierLg : sprites.barrierSm;
            if (bTex == null) continue;
            float bx = TileMap.tileCentreX(barriers[i][0]);
            float by = TileMap.tileCentreY(barriers[i][1]);
            float size = large ? ts * 0.85f : ts * 0.62f;
            sprites.drawCentered(batch, bTex, bx, by + ts * 0.08f, size, 1.0f);
        }
        batch.end();
    }

    /**
     * Security cameras: mounted one tile above each corridor entrance.
     * The sprite pans back and forth like real CCTV and a scan cone is shown.
     */
    private void renderSecurityCameras(float ts) {
        if (sprites.secCamera == null) return;
        int[][] camPositions = getCameraPositions();

        for (int i = 0; i < camPositions.length; i++) {
            float cx = TileMap.tileCentreX(camPositions[i][0]);
            float cy = TileMap.tileCentreY(camPositions[i][1]);

            // Pan angle: each camera has a different phase so they don't all move together
            float phase  = i * 1.3f;
            float panAng = (float)Math.sin(stateTime * 0.7f + phase) * 40f; // ±40°
            float baseAng = camPositions[i][2]; // base facing direction (degrees)
            float totalAng = baseAng + panAng;
            float radAng = (float)Math.toRadians(totalAng);

            // Draw scan cone
            float coneLen = ts * 2.6f;
            float halfFov = (float)Math.toRadians(28f);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(1f, 0.95f, 0.3f, 0.10f);
            int steps = 14;
            for (int s = 0; s < steps; s++) {
                float a1 = radAng - halfFov + (2f * halfFov * s / steps);
                float a2 = radAng - halfFov + (2f * halfFov * (s + 1) / steps);
                sr.triangle(cx, cy,
                    cx + (float)Math.cos(a1) * coneLen, cy + (float)Math.sin(a1) * coneLen,
                    cx + (float)Math.cos(a2) * coneLen, cy + (float)Math.sin(a2) * coneLen);
            }
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(1f, 0.9f, 0.2f, 0.35f);
            sr.line(cx, cy, cx + (float)Math.cos(radAng - halfFov) * coneLen,
                             cy + (float)Math.sin(radAng - halfFov) * coneLen);
            sr.line(cx, cy, cx + (float)Math.cos(radAng + halfFov) * coneLen,
                             cy + (float)Math.sin(radAng + halfFov) * coneLen);
            sr.end();

            // Draw mounted camera sprite, rotated to match pan
            batch.begin();
            sprites.drawCenteredRotated(batch, sprites.secCamera,
                cx, cy, ts * 0.72f, totalAng - 90f, 0.92f);
            batch.end();
        }
    }

    /**
     * Hunter sprite as drone body.
     * Replaces the drone's hexagon completely – drawn rotated to face direction,
     * blue tint in patrol, red tint when chasing, with a subtle drop-shadow.
     */
    private void renderDroneSprites(float ts) {
        if (sprites.hunter == null) return;
        float dSize = ts * 1.05f;

        // Shadow pass
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (DroneAI drone : drones) {
            float dx = drone.getPosition().x;
            float dy = drone.getPosition().y;
            sr.setColor(0f, 0f, 0f, 0.28f);
            sr.ellipse(dx - dSize * 0.4f, dy - dSize * 0.22f, dSize * 0.8f, dSize * 0.22f);
        }
        sr.end();

        // Sprite pass
        batch.begin();
        for (DroneAI drone : drones) {
            float dx = drone.getPosition().x;
            float dy = drone.getPosition().y;
            boolean chasing = "CHASE".equals(drone.getStateName());
            boolean searching = "SEARCH".equals(drone.getStateName());
            // Blue = patrol, yellow = search, red = chase
            if (chasing)       batch.setColor(1f, 0.25f, 0.15f, 0.95f);
            else if (searching) batch.setColor(1f, 0.85f, 0.1f,  0.95f);
            else                batch.setColor(0.35f, 0.75f, 1f,  0.92f);

            float half = dSize * 0.5f;
            batch.draw(sprites.hunter,
                dx - half, dy - half,
                half, half,
                dSize, dSize, 1f, 1f,
                drone.getFacingAngle() - 90f,
                0, 0, sprites.hunter.getWidth(), sprites.hunter.getHeight(),
                false, false);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        batch.end();
    }

    /**
     * Phone/wifi badge: pulsing icon above each terminal when the player is
     * within interact range, signalling "this device is broadcasting – hack it".
     */
    private void renderTerminalWifiBadge(float ts) {
        if (sprites.phoneWifi == null) return;
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;
        Vector2 pp = tc.getPosition();

        batch.begin();
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            float d  = dist(pp.x, pp.y, tx, ty);

            if (d < ts * 3.5f) {
                // Fade in as the player approaches
                float proximity = 1f - (d / (ts * 3.5f));
                float pulse = 0.7f + 0.3f * (float)Math.sin(stateTime * 5f + i);
                float bob   = (float)Math.sin(stateTime * 3f + i * 1.1f) * 4f;
                float alpha = proximity * pulse;

                // Glow ring behind the icon
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(0.2f, 0.9f, 1f, alpha * 0.18f);
                sr.circle(tx, ty + ts * 1.15f + bob, ts * 0.55f, 18);
                sr.end();

                sprites.drawCentered(batch, sprites.phoneWifi,
                    tx, ty + ts * 1.15f + bob,
                    ts * 0.65f, alpha);
            }
        }
        batch.end();
    }

    /** Ceiling light tile positions per level (room centres). */
    private int[][] getLightPositions() {
        switch (level) {
            case 2: return new int[][]{ {7,6},{30,6},{19,11},{7,17},{30,17} };
            case 3: return new int[][]{ {7,5},{32,5},{19,11},{7,17},{32,17} };
            case 4: return new int[][]{ {4,5},{35,5},{19,10},{4,17},{35,17} };
            case 5: return new int[][]{ {4,3},{35,3},{19,9},{4,18},{35,18} };
            default: return new int[][]{ {19,5},{19,12},{19,18} };
        }
    }

    /** Barrier crate tile positions per level. */
    private int[][] getBarrierPositions() {
        switch (level) {
            case 2: return new int[][]{ {13,11},{15,11},{24,11},{26,11} };
            case 3: return new int[][]{ {14,10},{18,13},{22,10},{26,13} };
            case 4: return new int[][]{ {12,10},{16,10},{22,10},{26,10} };
            case 5: return new int[][]{ {14,8},{18,12},{22,8},{26,12} };
            default: return new int[][]{ {18,12},{20,12} };
        }
    }

    /**
     * Security camera mount positions per level.
     * Each entry: { col, row, baseFacingDegrees }
     * Mounted above corridor entrances, facing inward so the cone covers the doorway.
     */
    private int[][] getCameraPositions() {
        switch (level) {
            case 2: return new int[][]{ {8,8,270}, {27,8,270}, {8,14,90}, {27,14,90} };
            case 3: return new int[][]{ {13,5,0},  {26,5,180}, {13,14,0}, {26,14,180} };
            case 4: return new int[][]{ {8,9,0},   {31,9,180}, {8,13,0},  {31,13,180} };
            case 5: return new int[][]{ {9,6,0},   {30,6,180}, {9,14,0},  {30,14,180} };
            default: return new int[][]{ {19,8,270} };
        }
    }

    // ── Terminal glow & hints ────────────────────────────────────────────────
    private void renderTerminalGlow() {
        float pulse = 0.4f + 0.25f * (float) Math.sin(stateTime * 2.5f);
        float ts = TileMap.TILE_SIZE;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            sr.setColor(0f, 0.75f, 0.35f, pulse * 0.18f); sr.circle(tx, ty, ts * 0.72f, 20);
            sr.setColor(0f, 0.90f, 0.45f, pulse * 0.28f); sr.circle(tx, ty, ts * 0.54f, 18);
            sr.setColor(0f, 1f, 0.5f, 0.65f + 0.25f * pulse); sr.circle(tx, ty - ts * 0.28f, 3.5f, 10);
        }
        sr.end();
    }

    private void renderTerminalHints() {
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;
        Vector2 pp = tc.getPosition();
        float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 6f);
        sr.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2.2f);
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            if (dist(pp.x, pp.y, tx, ty) < TileMap.TILE_SIZE * 2.6f) {
                sr.setColor(0f, pulse, 0.4f * pulse, 1f);
                sr.circle(tx, ty, TileMap.TILE_SIZE * 1.6f, 26);
            }
        }

        if (terminalPingTimer > 0f) {
            int nearestIdx = getNearestTerminalIndex(pp);
            if (nearestIdx >= 0) {
                float tx = TileMap.tileCentreX(terminalTiles[nearestIdx][0]);
                float ty = TileMap.tileCentreY(terminalTiles[nearestIdx][1]);
                float pingPulse = 0.6f + 0.4f * (float)Math.sin(stateTime * 9f);
                sr.setColor(0.15f, 0.95f, 1f, pingPulse);
                sr.line(pp.x, pp.y, tx, ty);
                sr.circle(tx, ty, TileMap.TILE_SIZE * 2.2f, 34);
                sr.circle(tx, ty, TileMap.TILE_SIZE * 2.8f, 34);
            }
        }
        sr.end();
        Gdx.gl.glLineWidth(1f);
    }

    private void renderCheckpointBeacon() {
        float pulse = 0.45f + 0.25f * (float)Math.sin(stateTime * 4f);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.25f, 0.95f, 1f, 0.45f + pulse * 0.35f);
        sr.circle(checkpointX, checkpointY, TileMap.TILE_SIZE * 0.55f, 24);
        sr.circle(checkpointX, checkpointY, TileMap.TILE_SIZE * 0.85f, 24);
        sr.end();
    }

    private void renderExitGuidance() {
        if (!exitUnlocked) return;
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc == null) return;
        float[] exitCentre = getExitCentre();
        float pulse = 0.45f + 0.35f * (float)Math.sin(stateTime * 5.5f);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.95f, 0.15f, 1f, 0.28f + pulse * 0.25f);
        sr.line(tc.getPosition().x, tc.getPosition().y, exitCentre[0], exitCentre[1]);
        sr.circle(exitCentre[0], exitCentre[1], TileMap.TILE_SIZE * 2.2f, 34);
        sr.end();
    }


    private void renderDronePatrolRoutes() {
        if (drones.length == 0) return;
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (DroneAI drone : drones) {
            float[][] route = drone.getPatrolWaypoints();
            if (route == null || route.length < 2) continue;
            float alpha = "PATROL".equals(drone.getStateName()) ? 0.22f : 0.10f;
            sr.setColor(0.2f, 0.85f, 1f, alpha);
            for (int i = 0; i < route.length; i++) {
                int next = (i + 1) % route.length;
                float x1 = TileMap.tileCentreX((int) route[i][0]);
                float y1 = TileMap.tileCentreY((int) route[i][1]);
                float x2 = TileMap.tileCentreX((int) route[next][0]);
                float y2 = TileMap.tileCentreY((int) route[next][1]);
                sr.line(x1, y1, x2, y2);
                sr.circle(x1, y1, 3.2f, 10);
            }
        }
        sr.end();
    }

    // ── Atmosphere ───────────────────────────────────────────────────────────
    private void renderAtmosphere() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        float vw = VIEW_W * 0.10f, vh = VIEW_H * 0.10f;
        sr.setColor(0f, 0f, 0f, 0.32f);
        float cx = camera.position.x, cy = camera.position.y;
        float hw = VIEW_W / 2f, hh = VIEW_H / 2f;
        sr.rect(cx - hw, cy - hh, vw, VIEW_H);
        sr.rect(cx + hw - vw, cy - hh, vw, VIEW_H);
        sr.rect(cx - hw, cy - hh, VIEW_W, vh);
        sr.rect(cx - hw, cy + hh - vh, VIEW_W, vh);
        sr.end();
    }

    // ── HUD ──────────────────────────────────────────────────────────────────
    private void renderHUD() {
        float alert  = maxAlert();
        boolean chasing = alert > 0.55f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(5f, TileMap.WORLD_H - 136f, 280f, 130f);
        float bx = 12f, by = TileMap.WORLD_H - 24f, barW = 220f, barH = 9f;
        sr.setColor(0.10f, 0.10f, 0.12f, 1f); sr.rect(bx, by, barW, barH);
        sr.setColor(alert, 0.12f * (1f - alert), 0f, 1f); sr.rect(bx, by, barW * alert, barH);
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(TileMap.WORLD_W - 240f, TileMap.WORLD_H - 32f, 235f, 27f);
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(TileMap.WORLD_W - 240f, TileMap.WORLD_H - 64f, 235f, 27f);
        sr.end();

        batch.begin();
        // camera.png icon prefix on the alert bar — instant "surveillance" readability
        if (sprites.camera != null) {
            float iconSize = 14f;
            sprites.drawCentered(batch, sprites.camera,
                bx + iconSize * 0.5f, TileMap.WORLD_H - 27f - 5f, iconSize,
                chasing ? 1f : 0.7f);
        }
        hudFont.setColor(chasing ? Color.RED : new Color(0.25f, 1f, 0.55f, 1f));
        hudFont.draw(batch, chasing ? "!! DRONE ALERT !!" : "ALERT", bx + 18f, TileMap.WORLD_H - 27f);
        hudFont.setColor(Color.YELLOW);
        hudFont.draw(batch, "KEYS: " + keysCollected + " / " + KEYS_REQUIRED, bx, TileMap.WORLD_H - 48f);
        hudFont.setColor(new Color(0.55f, 0.95f, 1f, 1f));
        hudFont.draw(batch, "LIVES: " + respawnsRemaining + " / " + maxRespawns, bx, TileMap.WORLD_H - 70f);
        hudFont.setColor(new Color(0.65f, 1f, 0.7f, 1f));
        hudFont.draw(batch, "PINGS: " + signalPingsRemaining + "   [H]", bx, TileMap.WORLD_H - 92f);
        if (protectionTimer > 0f) {
            hudFont.setColor(new Color(0.35f, 0.9f, 1f, 1f));
            hudFont.draw(batch, String.format("CLOAK: %.1fs", protectionTimer), bx, TileMap.WORLD_H - 114f);
        } else {
            hudFont.setColor(new Color(0.75f, 0.85f, 0.95f, 1f));
            hudFont.draw(batch, String.format("TIME: %d:%02d", (int)(missionElapsed / 60f), (int)(missionElapsed % 60f)), bx, TileMap.WORLD_H - 114f);
        }

        promptFont.setColor(chasing ? Color.RED : new Color(0.4f, 0.82f, 1f, 1f));
        StringBuilder droneStr = new StringBuilder();
        for (int i = 0; i < drones.length; i++) {
            if (i > 0) droneStr.append("  ");
            droneStr.append("D").append(i + 1).append(":").append(drones[i].getStateName());
        }
        if (drones.length == 0) droneStr.append("No drones");
        promptFont.draw(batch, droneStr.toString(), bx, TileMap.WORLD_H - 126f);

        alertFont.setColor(respawnsRemaining <= 1 ? Color.RED : Color.WHITE);
        alertFont.draw(batch, "LIVES " + respawnsRemaining,
            TileMap.WORLD_W - 238f, TileMap.WORLD_H - 12f);

        int lvIdx = Math.max(0, Math.min(level - 1, LEVEL_NAMES.length - 1));
        promptFont.setColor(new Color(0.4f, 0.75f, 1.0f, 1f));
        promptFont.draw(batch, LEVEL_NAMES[lvIdx], TileMap.WORLD_W - 238f, TileMap.WORLD_H - 42f);
        promptFont.setColor(new Color(0.85f, 0.92f, 1f, 1f));
        promptFont.draw(batch, String.format("MISSION %d:%02d", (int)(missionElapsed / 60f), (int)(missionElapsed % 60f)),
            TileMap.WORLD_W - 238f, TileMap.WORLD_H - 62f);

        if (activeChallenge == null || !activeChallenge.isOpen()) {
            TransformComponent tc2 = playerEntity.getComponent(TransformComponent.class);
            if (tc2 != null) {
                int nearbyIdx = getNearbyTerminalIndex(tc2.getPosition(), TileMap.TILE_SIZE * 1.6f);
                if (nearbyIdx >= 0) {
                    float p = 0.55f + 0.45f * (float) Math.sin(stateTime * 5f);
                    promptFont.setColor(0f, p, 0.4f, 1f);
                    promptFont.draw(batch, "[ E ] JACK IN   [ H ] PING   [ ESC ] SETTINGS",
                        TileMap.WORLD_W / 2f - 190f, 44f);
                    smallFontSafe(promptFont, batch, nearbyIdx);
                }
            }
        }

        if (exitUnlocked) {
            float p = 0.5f + 0.5f * (float) Math.sin(stateTime * 4f);
            promptFont.setColor(p, 0f, p, 1f);
            promptFont.draw(batch, ">>> EXIT UNLOCKED  -  REACH THE MAGENTA DOOR <<<",
                TileMap.WORLD_W / 2f - 220f, 14f);
        }
        batch.end();
    }

    private void smallFontSafe(BitmapFont font, SpriteBatch batch, int idx) {
        if (idx < 0 || idx >= challenges.length) return;
        font.setColor(new Color(0.55f, 0.95f, 1f, 1f));
        String label = "READY: " + challenges[idx].getTitle();
        layout.setText(font, label);
        font.draw(batch, label, TileMap.WORLD_W / 2f - layout.width / 2f, 22f);
    }

    private void renderObjectiveBanner() {
        if (bannerTimer <= 0f || activeChallenge != null && activeChallenge.isOpen()) return;
        float alpha = Math.min(1f, bannerTimer / 0.35f);
        float y = TileMap.WORLD_H - 118f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.62f * alpha);
        sr.rect(TileMap.WORLD_W / 2f - 250f, y - 44f, 500f, 60f);
        sr.end();
        batch.begin();
        alertFont.setColor(0.35f, 0.95f, 1f, alpha);
        layout.setText(alertFont, bannerTitle);
        alertFont.draw(batch, bannerTitle, TileMap.WORLD_W / 2f - layout.width / 2f, y);
        hudFont.setColor(1f, 1f, 1f, alpha);
        layout.setText(hudFont, bannerSubtitle);
        hudFont.draw(batch, bannerSubtitle, TileMap.WORLD_W / 2f - layout.width / 2f, y - 22f);
        batch.end();
    }

    // ── Minimap (Improvement 9) ──────────────────────────────────────────────
    private void renderMinimap(TransformComponent tc) {
        if (tc == null) return;
        float mmW = 140f, mmH = 77f;
        float mmX = TileMap.WORLD_W - mmW - 10f, mmY = 10f;
        float scaleX = mmW / TileMap.WORLD_W, scaleY = mmH / TileMap.WORLD_H;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.6f);
        sr.rect(mmX - 2, mmY - 2, mmW + 4, mmH + 4);

        // Draw walls as tiny dots
        for (int row = 0; row < TileMap.ROWS; row += 2) {
            for (int col = 0; col < TileMap.COLS; col += 2) {
                if (tileMap.isWall(col, row)) {
                    sr.setColor(0.2f, 0.25f, 0.3f, 0.8f);
                } else {
                    sr.setColor(0.05f, 0.08f, 0.1f, 0.5f);
                }
                sr.rect(mmX + col * scaleX * TileMap.TILE_SIZE,
                        mmY + (TileMap.WORLD_H - (row + 1) * TileMap.TILE_SIZE) * scaleY,
                        2 * scaleX * TileMap.TILE_SIZE, 2 * scaleY * TileMap.TILE_SIZE);
            }
        }

        // Unsolved terminals — map_pin sprite instead of plain circle
        sr.end();
        batch.begin();
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]) * scaleX + mmX;
            float ty = TileMap.tileCentreY(terminalTiles[i][1]) * scaleY + mmY;
            float pinSize = 7f;
            if (sprites.mapPin != null) {
                sprites.drawCentered(batch, sprites.mapPin, tx, ty + 2f, pinSize, 1f);
            } else {
                // Fallback: plain circle if texture not loaded
                batch.end();
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(0f, 1f, 0.4f, 0.9f);
                sr.circle(tx, ty, 3f, 6);
                sr.end();
                batch.begin();
            }
        }
        batch.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Exit
        for (int row = 0; row < TileMap.ROWS; row++) {
            for (int col = 0; col < TileMap.COLS; col++) {
                if (tileMap.getType(col, row) == TileType.EXIT) {
                    float ex = TileMap.tileCentreX(col) * scaleX + mmX;
                    float ey = TileMap.tileCentreY(row) * scaleY + mmY;
                    sr.setColor(exitUnlocked ? new Color(0.8f, 0f, 1f, 1f)
                                             : new Color(0.3f, 0.05f, 0.05f, 0.8f));
                    sr.rect(ex - 2, ey - 2, 4, 4);
                }
            }
        }

        // Checkpoint
        float cx = checkpointX * scaleX + mmX;
        float cy = checkpointY * scaleY + mmY;
        sr.setColor(0.2f, 0.95f, 1f, 0.95f);
        sr.rect(cx - 2.5f, cy - 2.5f, 5f, 5f);

        // Player
        float px = tc.getPosition().x * scaleX + mmX;
        float py = tc.getPosition().y * scaleY + mmY;
        float blink = 0.7f + 0.3f * (float)Math.sin(stateTime * 8f);
        sr.setColor(0.3f, 0.8f * blink, 1f, 1f);
        sr.circle(px, py, 3f, 8);

        // Drones
        for (DroneAI d : drones) {
            float dx = d.getPosition().x * scaleX + mmX;
            float dy = d.getPosition().y * scaleY + mmY;
            sr.setColor(1f, 0.2f, 0.1f, 0.9f);
            sr.circle(dx, dy, 2f, 6);
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.3f, 0.5f, 0.7f, 0.6f);
        sr.rect(mmX - 2, mmY - 2, mmW + 4, mmH + 4);
        sr.end();
    }

    // ── Threat indicator (Improvement 10) ────────────────────────────────────
    private void renderThreatIndicator(TransformComponent tc) {
        if (tc == null || drones.length == 0) return;
        Vector2 pp = tc.getPosition();
        float nearestDist = Float.MAX_VALUE;
        DroneAI nearest = null;
        for (DroneAI d : drones) {
            float dx = d.getPosition().x - pp.x;
            float dy = d.getPosition().y - pp.y;
            float dd = dx * dx + dy * dy;
            if (dd < nearestDist) { nearestDist = dd; nearest = d; }
        }
        if (nearest == null) return;
        nearestDist = (float)Math.sqrt(nearestDist);
        if (nearestDist > 400f) return; // too far, don't show

        float angle = (float)Math.atan2(nearest.getPosition().y - pp.y,
                                         nearest.getPosition().x - pp.x);
        float indicatorDist = 60f;
        float ix = TileMap.WORLD_W / 2f + (float)Math.cos(angle) * indicatorDist;
        float iy = TileMap.WORLD_H / 2f + (float)Math.sin(angle) * indicatorDist;

        float alert = nearest.getAlertLevel();
        float pulse = 0.5f + 0.5f * (float)Math.sin(stateTime * 6f);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Color: green (far) → yellow (mid) → red (close/chasing)
        float t = Math.max(0f, Math.min(1f, 1f - nearestDist / 400f));
        float r = t + alert;
        float g = (1f - t) * (1f - alert);
        sr.setColor(Math.min(1f, r), g, 0f, 0.5f + 0.3f * pulse);

        // Arrow triangle pointing toward drone
        float size = 8f + alert * 6f;
        float perpAngle = angle + (float)Math.PI / 2f;
        float tipX = ix + (float)Math.cos(angle) * size;
        float tipY = iy + (float)Math.sin(angle) * size;
        float baseX1 = ix + (float)Math.cos(perpAngle) * size * 0.5f;
        float baseY1 = iy + (float)Math.sin(perpAngle) * size * 0.5f;
        float baseX2 = ix - (float)Math.cos(perpAngle) * size * 0.5f;
        float baseY2 = iy - (float)Math.sin(perpAngle) * size * 0.5f;
        sr.triangle(tipX, tipY, baseX1, baseY1, baseX2, baseY2);
        sr.end();
    }

    private void renderChaseWarning() {
        if (chaseWarningTimer <= 0f || (activeChallenge != null && activeChallenge.isOpen())) return;

        float pulse = 0.55f + 0.45f * (float)Math.sin(stateTime * 10f);
        batch.begin();
        alertFont.setColor(1f, 0.15f * pulse, 0.05f, 1f);
        String msg = "DETECTED   -   BREAK LINE OF SIGHT!";
        layout.setText(alertFont, msg);
        alertFont.draw(batch, msg,
            TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H - 22f);
        hudFont.setColor(1f, 0.9f, 0.7f, 1f);
        String sub = "Stay behind corners. If you leave the drone detection range, the chase drops quickly.";
        layout.setText(hudFont, sub);
        hudFont.draw(batch, sub,
            TileMap.WORLD_W / 2f - layout.width / 2f,
            TileMap.WORLD_H - 46f);
        batch.end();
    }

    // ── Particles (Improvement 11) ───────────────────────────────────────────
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
                pLife[i] = pLife[particleCount]; pR[i] = pR[particleCount];
                pG[i] = pG[particleCount]; pB[i] = pB[particleCount];
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

    // ── End screen ───────────────────────────────────────────────────────────
    private void renderEndScreen(boolean win) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.86f); sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
        sr.end();
        batch.begin();
        if (win) {
            float fl = 0.6f + 0.4f * (float) Math.sin(stateTime * 3.5f);
            alertFont.setColor(fl, 0f, fl, 1f);
            String t = "SYSTEM  BREACHED";
            layout.setText(alertFont, t);
            alertFont.draw(batch, t, TileMap.WORLD_W / 2f - layout.width / 2f,
                TileMap.WORLD_H / 2f + 70f);
            hudFont.setColor(Color.YELLOW);
            hudFont.draw(batch, "Terminals secured : " + keysCollected + " / " + KEYS_REQUIRED,
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f + 20f);
            hudFont.draw(batch, String.format("Mission time      : %d:%02d", (int)(missionElapsed / 60f), (int)(missionElapsed % 60f)),
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 12f);
            hudFont.draw(batch, "Lives remaining   : " + respawnsRemaining,
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 44f);
        } else {
            float fl = 0.6f + 0.4f * (float) Math.sin(stateTime * 6f);
            alertFont.setColor(fl, 0f, 0f, 1f);
            String m = "NO  INTEGRITY  LEFT";
            layout.setText(alertFont, m);
            alertFont.draw(batch, m, TileMap.WORLD_W / 2f - layout.width / 2f,
                TileMap.WORLD_H / 2f + 50f);
        }
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "[ E ] or [ ENTER ]  Continue",
            TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 110f);
        batch.end();
    }

    // =========================================================================
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
    @Override protected void onUnload() { Gdx.app.log("CyberGame", "unloading level " + level); }
    @Override protected void onDispose() {
        if (activeChallenge != null && activeChallenge.isOpen()) activeChallenge.close();
        if (playerAnimator != null) playerAnimator.dispose();
        if (sr     != null) sr.dispose();
        if (batch  != null) batch.dispose();
        if (hudFont    != null) hudFont.dispose();
        if (alertFont  != null) alertFont.dispose();
        if (promptFont != null) promptFont.dispose();
        tileMap.disposeTileset();
        sprites.dispose();
    }

    @Override
    public boolean blocksWorldUpdate() {
        return activeChallenge != null && activeChallenge.isOpen();
    }

    private float maxAlert() {
        float m = 0;
        for (DroneAI d : drones) m = Math.max(m, d.getAlertLevel());
        return m;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
