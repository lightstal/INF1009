package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.BinaryDecodeGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.CaesarCipherGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.IMiniGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.LogAnalysisGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.OsintHuntGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.PortMatchGame;
import io.github.INF1009_P10_Team7.simulation.cyber.minigame.TerminalMiniGame;
import io.github.INF1009_P10_Team7.simulation.cyber.observer.GameEventSystem;

/**
 * CyberGameScene — main gameplay scene for all 5 levels.
 *
 * Visual improvements over the original:
 *  • TileMap now renders floors using room-zone colours + Tech Dungeon tileset walls.
 *  • Fonts are created at a fixed scale and NEVER re-scaled per-frame
 *    (fixes the blurry-text bug that came from calling setScale() each draw).
 *  • Player sprite drawn from player_sheet.png (Tech Dungeon asset pack).
 *  • HUD updated to show current level name from the 5-level roster.
 *
 * Design patterns: State (DroneAI), Strategy (CTF challenges), Observer (GameEventSystem).
 */
public class CyberGameScene extends Scene {

    private final IEntitySystem    entitySystem;
    private final ICollisionSystem collisionSystem;
    private final IMovementSystem  movementSystem;
    private final CyberSceneFactory factory;
    private final int level;

    // ── Sprites ───────────────────────────────────────────────────────────────
    private final CyberSprites sprites = new CyberSprites();
    private SpriteAnimator playerAnimator;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private ShapeRenderer sr;
    private SpriteBatch   batch;

    // Fonts are baked at the right size at construction – setScale() is NEVER
    // called after creation, which is what caused blurry text in the original.
    private BitmapFont hudFont;      // HUD labels (keys, timer, drone status)
    private BitmapFont alertFont;    // Larger alert messages
    private BitmapFont promptFont;   // Small "press E" prompts
    private GlyphLayout layout;

    // ── Camera (game world + fixed HUD) ───────────────────────────────────────
    private static final float VIEW_W = 640f;
    private static final float VIEW_H = 352f;
    private OrthographicCamera camera;
    private Viewport           viewport;
    private OrthographicCamera hudCamera;
    private Viewport           hudViewport;

    // ── World ─────────────────────────────────────────────────────────────────
    private TileMap tileMap;
    private float   stateTime  = 0f;
    private float   rotorAngle = 0f;

    // ── Player ────────────────────────────────────────────────────────────────
    private GameEntity playerEntity;
    private static final float PLAYER_RADIUS = 10f;

    // ── Level config (populated in initLevelConfig) ───────────────────────────
    private int[][]    terminalTiles;
    private boolean[]  terminalSolved;
    private IMiniGame[] challenges;
    private int        KEYS_REQUIRED;
    private DroneAI[]  drones;
    private int[]      playerStartTile; // [col, row]

    private final TerminalEmulator terminal    = new TerminalEmulator();
    private IMiniGame  activeChallenge         = null;
    private int        activeChallengeIdx      = -1;

    private final GameEventSystem eventSystem  = new GameEventSystem();
    private final PlayerInventory inventory    = new PlayerInventory();

    // ── Game state ────────────────────────────────────────────────────────────
    private boolean gameOver     = false;
    private boolean victory      = false;
    private boolean exitUnlocked = false;
    private int     keysCollected = 0;
    private float   timeRemaining;

    // ── Level display names ───────────────────────────────────────────────────
    private static final String[] LEVEL_NAMES = {
        "LEVEL 1  —  RECON LAB",
        "LEVEL 2  —  NETWORK HUB",
        "LEVEL 3  —  SERVER FARM",
        "LEVEL 4  —  DATA CENTER",
        "LEVEL 5  —  BLACK SITE",
    };

    // ═════════════════════════════════════════════════════════════════════════
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

    // Backwards-compat ctor (Level 1)
    public CyberGameScene(IInputController input, IAudioController audio,
                          SceneNavigator nav,
                          IEntitySystem entitySystem,
                          ICollisionSystem collisionSystem,
                          IMovementSystem movementSystem,
                          CyberSceneFactory factory) {
        this(input, audio, nav, entitySystem, collisionSystem, movementSystem, factory, 1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    protected void onLoad() {
        // Game viewport (zoomed in, follows player)
        camera   = new OrthographicCamera();
        viewport = new StretchViewport(VIEW_W, VIEW_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0);
        camera.update();

        // HUD viewport (full world size, always fixed)
        hudCamera   = new OrthographicCamera();
        hudViewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, hudCamera);
        hudViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hudCamera.position.set(TileMap.WORLD_W / 2f, TileMap.WORLD_H / 2f, 0);
        hudCamera.update();

        sr     = new ShapeRenderer();
        batch  = new SpriteBatch();
        layout = new GlyphLayout();

        // ── Font setup ────────────────────────────────────────────────────
        // Each font is created at its intended display scale.
        // We NEVER call setScale() again after this point so text stays crisp.
        hudFont    = makeBitmapFont(1.1f);
        alertFont  = makeBitmapFont(1.5f);
        promptFont = makeBitmapFont(1.0f);

        tileMap = new TileMap(level);
        tileMap.loadTileset();         // load Tech Dungeon tileset.png

        sprites.load();
        playerAnimator = new SpriteAnimator("niceguy.png", 9, 4, 64, 64, 0.10f);
        eventSystem.addObserver(inventory);
        initLevelConfig();
        createPlayer();
        audio.setMusic("Music_Game.mp3");
    }

    /** Creates a BitmapFont locked to the given scale. Never re-scaled later. */
    private BitmapFont makeBitmapFont(float scale) {
        BitmapFont f = new BitmapFont();
        f.getData().setScale(scale);
        f.setUseIntegerPositions(true);
        return f;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LEVEL CONFIG  (edit this when adding new levels)
    // ═════════════════════════════════════════════════════════════════════════
    private void initLevelConfig() {
        switch (level) {

            // ── Level 1: INITIATION — Star layout, 5 rooms, no drones ──────
            // Terminals in: Room A(navy top), B(teal left), C(green hub),
            //               D(amber right), E(violet bottom)
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
                timeRemaining  = 240f;
                drones         = new DroneAI[]{};
                playerStartTile = new int[]{ 19, 12 };
                break;

            // ── Level 2: INFILTRATION — Z-shape, 5 rooms, 2 drones ─────────
            // Terminals: A(navy top-left), B(teal top-right), C(green bridge),
            //            D(amber bottom-left), E(violet bottom-right)
            case 2:
                terminalTiles  = new int[][]{ {7,5}, {31,5}, {19,11}, {7,17}, {31,17} };
                challenges     = new IMiniGame[]{
                    new BinaryDecodeGame(),
                    new CaesarCipherGame(),
                    new PortMatchGame(),
                    new LogAnalysisGame(),
                    new TerminalMiniGame(new SqlInjectionChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 220f;
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(8),  TileMap.tileCentreY(11)),
                    new DroneAI(TileMap.tileCentreX(28), TileMap.tileCentreY(11))
                };
                playerStartTile = new int[]{ 19, 11 };
                break;

            // ── Level 3: BREACH — 4-corners + vault, 5 rooms, 3 drones ─────
            // Terminals: A(navy top-left), B(teal top-right), C(green vault),
            //            D(amber bottom-left), E(violet bottom-right)
            default: // case 3
                terminalTiles  = new int[][]{ {6,4}, {32,4}, {19,10}, {6,16}, {32,16} };
                challenges     = new IMiniGame[]{
                    new BinaryDecodeGame(),
                    new CaesarCipherGame(),
                    new PortMatchGame(),
                    new LogAnalysisGame(),
                    new TerminalMiniGame(new NmapReconChallenge(), terminal)
                };
                KEYS_REQUIRED  = 5;
                timeRemaining  = 200f;
                drones         = new DroneAI[]{
                    new DroneAI(TileMap.tileCentreX(7),  TileMap.tileCentreY(6)),
                    new DroneAI(TileMap.tileCentreX(32), TileMap.tileCentreY(6)),
                    new DroneAI(TileMap.tileCentreX(19), TileMap.tileCentreY(14))
                };
                playerStartTile = new int[]{ 19, 10 };
                break;
        }

        terminalSolved = new boolean[terminalTiles.length];
        for (int i = 0; i < terminalSolved.length; i++) terminalSolved[i] = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void createPlayer() {
        playerEntity = new GameEntity("CyberPlayer");
        float startX = TileMap.tileCentreX(playerStartTile[0]);
        float startY = TileMap.tileCentreY(playerStartTile[1]);
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

    // ═════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    protected void onUpdate(float delta) {
        // Only reset input to engine when no challenge overlay is open
        if (activeChallenge == null || !activeChallenge.isOpen()) {
            Gdx.input.setInputProcessor(null);
        }
        stateTime += delta;
        rotorAngle += delta * 200f;

        if (gameOver || victory) {
            if (input.isActionJustPressed("INTERACT") || input.isActionJustPressed("BACK")) {
                if (victory)
                    nav.requestScene(factory.createVictoryScene(keysCollected, (int) timeRemaining, level));
                else
                    nav.requestScene(factory.createGameOverScene());
            }
            return;
        }

        // Scene navigation
        if (input.isActionJustPressed("SETTINGS")) {
            nav.pushScene(factory.createSettingsScene()); return;
        }
        if (input.isActionJustPressed("BACK")) {
            nav.requestScene(factory.createMainMenuScene()); return;
        }

        // Timer countdown
        timeRemaining -= delta;
        if (timeRemaining <= 0f) { timeRemaining = 0f; gameOver = true; return; }

        tileMap.update(delta);

        // Active challenge overlay
        if (activeChallenge != null && activeChallenge.isOpen()) {
            activeChallenge.update(delta);
            if (!activeChallenge.isOpen()) {
                if (activeChallenge.isSolved()) {
                    terminalSolved[activeChallengeIdx] = true;
                    keysCollected++;
                    eventSystem.notifyKeyCollected(keysCollected, KEYS_REQUIRED);
                    tileMap.setTile(terminalTiles[activeChallengeIdx][0],
                                    terminalTiles[activeChallengeIdx][1], TileType.FLOOR);
                    if (keysCollected >= KEYS_REQUIRED) exitUnlocked = true;
                }
                activeChallenge = null; activeChallengeIdx = -1;
            }
            return;
        }

        // Player position / tile-collision
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            float[] resolved = tileMap.resolveCircleVsWalls(
                tc.getPosition().x, tc.getPosition().y, PLAYER_RADIUS);
            tc.getPosition().set(resolved[0], resolved[1]);
        }

        // Terminal interaction
        if (input.isActionJustPressed("INTERACT") && tc != null) {
            Vector2 pp = tc.getPosition();
            for (int i = 0; i < terminalTiles.length; i++) {
                if (terminalSolved[i]) continue;
                float tx = TileMap.tileCentreX(terminalTiles[i][0]);
                float ty = TileMap.tileCentreY(terminalTiles[i][1]);
                if (dist(pp.x, pp.y, tx, ty) < TileMap.TILE_SIZE * 1.6f) {
                    activeChallenge = challenges[i];
                    activeChallengeIdx = i;
                    activeChallenge.open();
                    break;
                }
            }
        }

        // Drone updates
        if (tc != null) {
            for (DroneAI drone : drones) {
                drone.update(tileMap, tc.getPosition(), delta);
                if (drone.getAlertLevel() >= 1.0f) { gameOver = true; return; }
            }
        }

        // Exit check
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

        // Camera follow
        if (tc != null) followCamera(tc.getPosition().x, tc.getPosition().y);
    }

    private void followCamera(float px, float py) {
        float hw = VIEW_W / 2f, hh = VIEW_H / 2f;
        float cx = Math.max(hw, Math.min(TileMap.WORLD_W - hw, px));
        float cy = Math.max(hh, Math.min(TileMap.WORLD_H - hh, py));
        camera.position.set(cx, cy, 0);
        camera.update();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RENDER
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    protected void onRender() {
        Gdx.gl.glClearColor(0f, 0f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── Game world (zoomed camera) ─────────────────────────────────────
        viewport.apply();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // TileMap: floor fills + tileset wall sprites
        tileMap.render(sr, batch, exitUnlocked);

        // Terminal glows and hints
        renderTerminalGlow();
        renderTerminalHints();

        // Drones
        TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
        for (DroneAI drone : drones) {
            renderDrone(drone);
        }

        // Player sprite
        renderPlayer(tc);

        // Atmosphere / vignette
        renderAtmosphere();

        // ── Active challenge overlay (HUD camera) ─────────────────────────
        if (activeChallenge != null && activeChallenge.isOpen()) {
            hudViewport.apply();
            sr.setProjectionMatrix(hudCamera.combined);
            batch.setProjectionMatrix(hudCamera.combined);
            activeChallenge.render(sr, batch, hudFont);
            return;
        }

        // ── HUD (HUD camera) ──────────────────────────────────────────────
        hudViewport.apply();
        sr.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);
        renderHUD();

        // End-screen overlay
        if (gameOver || victory) renderEndScreen(victory);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void renderPlayer(TransformComponent tc) {
         if (tc == null)
            return;
        float px = tc.getPosition().x, py = tc.getPosition().y;
        float r = PLAYER_RADIUS;

        if (playerAnimator != null) {
            PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
            float vx = phys != null ? phys.getVelocity().x : 0f;
            float vy = phys != null ? phys.getVelocity().y : 0f;
            playerAnimator.update(Gdx.graphics.getDeltaTime(), vx, vy);
            batch.begin(); // ← add this
            playerAnimator.drawCentered(batch, px, py, r * 2.8f);
            batch.end(); // ← add this
        } else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.3f, 0.8f, 1.0f, 1f);
            sr.triangle(px, py + r, px - r * 0.85f, py - r * 0.6f, px + r * 0.85f, py - r * 0.6f);
            sr.end();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void renderDrone(DroneAI drone) {
        float dx = drone.getPosition().x, dy = drone.getPosition().y;
        float alert = drone.getAlertLevel();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Alert aura
        if (alert > 0.05f) {
            sr.setColor(alert, 0.05f, 0.05f, alert * 0.35f);
            sr.circle(dx, dy, TileMap.TILE_SIZE * (1.2f + alert * 0.8f), 24);
        }
        // Body
        sr.setColor(0.7f + alert * 0.3f, 0.1f, 0.1f, 1f);
        sr.circle(dx, dy, 10f, 16);
        sr.setColor(0.9f, 0.2f, 0.2f, 1f);
        sr.circle(dx, dy, 6f, 12);
        // Rotor arms (4)
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1f, 0.4f + alert * 0.4f, 0.2f, 0.9f);
        for (int i = 0; i < 4; i++) {
            float angle = (float) Math.toRadians(rotorAngle + i * 90f);
            float ax = (float) Math.cos(angle) * 13f, ay = (float) Math.sin(angle) * 13f;
            sr.line(dx, dy, dx + ax, dy + ay);
            sr.circle(dx + ax, dy + ay, 4f, 8);
        }
        sr.end();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void renderTerminalGlow() {
        float pulse = 0.4f + 0.25f * (float) Math.sin(stateTime * 2.5f);
        float ts = TileMap.TILE_SIZE;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            sr.setColor(0f, 0.75f, 0.35f, pulse * 0.18f);
            sr.circle(tx, ty, ts * 0.72f, 20);
            sr.setColor(0f, 0.90f, 0.45f, pulse * 0.28f);
            sr.circle(tx, ty, ts * 0.54f, 18);
            sr.setColor(0f, 1f, 0.5f, 0.65f + 0.25f * pulse);
            sr.circle(tx, ty - ts * 0.28f, 3.5f, 10);
        }
        sr.end();
    }

    // ─────────────────────────────────────────────────────────────────────────
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
        sr.end();
        Gdx.gl.glLineWidth(1f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void renderAtmosphere() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Vignette (scanlines removed)
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

    // ─────────────────────────────────────────────────────────────────────────
    private void renderHUD() {
        float alert  = maxAlert();
        boolean chasing = alert > 0.55f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Top-left panel
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(5f, TileMap.WORLD_H - 106f, 240f, 100f);
        // Alert bar track
        float bx = 12f, by = TileMap.WORLD_H - 24f, barW = 220f, barH = 9f;
        sr.setColor(0.10f, 0.10f, 0.12f, 1f); sr.rect(bx, by, barW, barH);
        sr.setColor(alert, 0.12f * (1f - alert), 0f, 1f); sr.rect(bx, by, barW * alert, barH);
        // Timer + level badge panels
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(TileMap.WORLD_W - 240f, TileMap.WORLD_H - 32f, 235f, 27f);
        sr.setColor(0f, 0f, 0f, 0.70f); sr.rect(TileMap.WORLD_W - 240f, TileMap.WORLD_H - 64f, 235f, 27f);
        sr.end();

        batch.begin();

        // Alert label
        hudFont.setColor(chasing ? Color.RED : new Color(0.25f, 1f, 0.55f, 1f));
        hudFont.draw(batch, chasing ? "!! DRONE ALERT !!" : "ALERT", bx, TileMap.WORLD_H - 27f);

        // Keys collected
        hudFont.setColor(Color.YELLOW);
        hudFont.draw(batch, "KEYS: " + keysCollected + " / " + KEYS_REQUIRED,
            bx, TileMap.WORLD_H - 48f);

        // Drone status line
        promptFont.setColor(chasing ? Color.RED : new Color(0.4f, 0.82f, 1f, 1f));
        StringBuilder droneStr = new StringBuilder();
        for (int i = 0; i < drones.length; i++) {
            if (i > 0) droneStr.append("  ");
            droneStr.append("D").append(i + 1).append(":").append(drones[i].getStateName());
        }
        if (drones.length == 0) droneStr.append("No drones");
        promptFont.draw(batch, droneStr.toString(), bx, TileMap.WORLD_H - 68f);

        // Timer
        int mins = (int)(timeRemaining / 60f), secs = (int)(timeRemaining % 60f);
        alertFont.setColor(timeRemaining < 30f ? Color.RED : Color.WHITE);
        alertFont.draw(batch, String.format("%d:%02d", mins, secs),
            TileMap.WORLD_W - 238f, TileMap.WORLD_H - 12f);

        // Level badge
        int lvIdx = Math.max(0, Math.min(level - 1, LEVEL_NAMES.length - 1));
        promptFont.setColor(new Color(0.4f, 0.75f, 1.0f, 1f));
        promptFont.draw(batch, LEVEL_NAMES[lvIdx], TileMap.WORLD_W - 238f, TileMap.WORLD_H - 42f);

        // "Jack in" prompt
        if (activeChallenge == null || !activeChallenge.isOpen()) {
            boolean nearAny = false;
            TransformComponent tc = playerEntity.getComponent(TransformComponent.class);
            if (tc != null) {
                for (int i = 0; i < terminalTiles.length; i++) {
                    if (terminalSolved[i]) continue;
                    if (dist(tc.getPosition().x, tc.getPosition().y,
                             TileMap.tileCentreX(terminalTiles[i][0]),
                             TileMap.tileCentreY(terminalTiles[i][1]))
                            < TileMap.TILE_SIZE * 1.6f) {
                        nearAny = true; break;
                    }
                }
            }
            if (nearAny) {
                float p = 0.55f + 0.45f * (float) Math.sin(stateTime * 5f);
                promptFont.setColor(0f, p, 0.4f, 1f);
                promptFont.draw(batch, "[ E ]  JACK IN — HACK TERMINAL",
                    TileMap.WORLD_W / 2f - 130f, 28f);
            }
        }

        // Exit unlocked banner
        if (exitUnlocked) {
            float p = 0.5f + 0.5f * (float) Math.sin(stateTime * 4f);
            promptFont.setColor(p, 0f, p, 1f);
            promptFont.draw(batch, ">>> EXIT UNLOCKED  —  REACH THE MAGENTA DOOR <<<",
                TileMap.WORLD_W / 2f - 220f, 14f);
        }
        batch.end();
    }

    // ─────────────────────────────────────────────────────────────────────────
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
            hudFont.draw(batch, "Time remaining    : " + (int) timeRemaining + "s",
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 12f);
        } else {
            float fl = 0.6f + 0.4f * (float) Math.sin(stateTime * 6f);
            alertFont.setColor(fl, 0f, 0f, 1f);
            String m = timeRemaining <= 0f ? "TIMER  EXPIRED" : "DRONE  GOT  YOU";
            layout.setText(alertFont, m);
            alertFont.draw(batch, m, TileMap.WORLD_W / 2f - layout.width / 2f,
                TileMap.WORLD_H / 2f + 50f);
        }
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "[ E ] or [ BACKSPACE ]  Continue",
            TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 80f);
        batch.end();
    }

    // ═════════════════════════════════════════════════════════════════════════
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

    // ─────────────────────────────────────────────────────────────────────────
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
