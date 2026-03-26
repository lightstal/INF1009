package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.level.LevelConfig;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;
import io.github.INF1009_P10_Team7.cyber.clue.ClueSystem;
import io.github.INF1009_P10_Team7.cyber.player.PlayerState;
import io.github.INF1009_P10_Team7.cyber.minigame.IMiniGame;
import io.github.INF1009_P10_Team7.cyber.scenes.CyberWorldRenderer;

import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.PhysicComponent;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.render.MiniGameRenderContext;
import io.github.INF1009_P10_Team7.engine.render.gdx.GdxShapeDrawAdapter;
import io.github.INF1009_P10_Team7.engine.render.gdx.GdxSpriteDrawAdapter;
import io.github.INF1009_P10_Team7.engine.render.gdx.GdxTextDrawAdapter;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;
import io.github.INF1009_P10_Team7.engine.render.ISpriteDraw;
import io.github.INF1009_P10_Team7.engine.render.ITextDraw;

/**
 * CyberGameRenderer, owns all LibGDX rendering objects for CyberGameScene.
 * CyberGameScene should only maintain simulation/state; rendering is delegated here.
 */
public class CyberGameRenderer {

    private static final float VIEW_W = 640f;
    private static final float VIEW_H = 352f;
    private static final float PLAYER_RADIUS = 10f;

    private final IInputController input;
    private final io.github.INF1009_P10_Team7.engine.map.ILevelMapRuntime mapRuntime;
    private final LevelConfig config;
    private final CyberSprites sprites;

    private ShapeRenderer sr;
    private SpriteBatch batch;
    private GlyphLayout layout;

    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera hudCamera;
    private Viewport hudViewport;

    private BitmapFont hudFont, hudSmallFont, hudPanelFont, alertFont, promptFont;
    private BitmapFont miniBodyFont, miniTitleFont, miniSmallFont, miniMonoFont;
    private CyberHudRenderer hudRenderer;
    private CyberWorldRenderer worldRenderer;

    private SpriteAnimator playerAnimator;

    // Pooled references for adapters
    private IShapeDraw shapeDrawAdapter;
    private ISpriteDraw spriteDrawAdapter;
    private ITextDraw hudTextDrawAdapter;
    private ITextDraw promptTextDrawAdapter;

    public CyberGameRenderer(IInputController input,
                              io.github.INF1009_P10_Team7.engine.map.ILevelMapRuntime mapRuntime,
                              LevelConfig config,
                              CyberSprites sprites) {
        this.input = input;
        this.mapRuntime = mapRuntime;
        this.config = config;
        this.sprites = sprites;
    }

    public void load() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VIEW_W, VIEW_H, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.position.set(VIEW_W / 2f, VIEW_H / 2f, 0f);
        camera.update();

        hudCamera = new OrthographicCamera();
        hudViewport = new StretchViewport(TileMap.WORLD_W, TileMap.WORLD_H, hudCamera);
        hudViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hudCamera.position.set(TileMap.WORLD_W / 2f, TileMap.WORLD_H / 2f, 0f);
        hudCamera.update();

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        hudFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(1.0f);
        hudSmallFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.48f);
        hudPanelFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.80f);
        alertFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(1.28f);
        promptFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.46f);

        // Minigame fonts (kept here so minigames don't import LibGDX types)
        miniBodyFont  = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.90f);
        miniTitleFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(1.10f);
        miniSmallFont = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.76f);
        miniMonoFont  = io.github.INF1009_P10_Team7.engine.render.FontManager.create(0.82f);

        shapeDrawAdapter = new GdxShapeDrawAdapter(sr);
        spriteDrawAdapter = new GdxSpriteDrawAdapter(batch, sprites::get);
        hudTextDrawAdapter = new GdxTextDrawAdapter(batch, hudSmallFont);
        promptTextDrawAdapter = new GdxTextDrawAdapter(batch, promptFont);

        // HUD renderer uses the same fonts/batch the main renderer owns.
        hudRenderer = new CyberHudRenderer(
            shapeDrawAdapter, batch,
            hudFont, hudSmallFont, hudPanelFont,
            alertFont, promptFont,
            layout,
            sprites,
            config
        );

        worldRenderer = new CyberWorldRenderer(
            shapeDrawAdapter,
            spriteDrawAdapter,
            hudTextDrawAdapter,
            promptTextDrawAdapter,
            sprites,
            input
        );

        playerAnimator = new SpriteAnimator("niceguy.png", 9, 4, 64, 64, 0.10f);
    }

    public void resize(int w, int h) {
        if (viewport != null) viewport.update(w, h, true);
        if (hudViewport != null) hudViewport.update(w, h, true);
    }

    public void followCamera(float px, float py) {
        float hw = VIEW_W / 2f;
        float hh = VIEW_H / 2f;
        float cx = Math.max(hw, Math.min(TileMap.WORLD_W - hw, px));
        float cy = Math.max(hh, Math.min(TileMap.WORLD_H - hh, py));
        camera.position.set(cx, cy, 0);
        camera.update();
    }

    public void render(
        float stateTime,
        float missionElapsed,
        float timeRemaining,
        boolean gameOver,
        boolean victory,
        boolean exitUnlocked,
        float chaseWarningTimer,
        float bannerTimer,
        float bannerDuration,
        String bannerTitle,
        String bannerSubtitle,
        float transitionAlpha,
        int keysCollected,
        int keysRequired,
        int respawnsRemaining,
        int maxRespawns,
        int signalPingsRemaining,
        ClueSystem clueSystem,
        PlayerState playerState,
        float terminalPingTimer,
        float checkpointX,
        float checkpointY,
        float tmxExitX,
        float tmxExitY,
        float PING_REVEAL_RADIUS,
        boolean[] terminalSolved,
        int[][] terminalTiles,
        boolean[] cctvAlerted,
        int[][] cameraPositions,
        DroneAI[] drones,
        GameEntity playerEntity,
        TransformComponent playerTc,
        IWorldCollisionQuery collisionMgr,
        float[] pX,
        float[] pY,
        float[] pR,
        float[] pG,
        float[] pB,
        float[] pLife,
        int particleCount,
        float frameDelta,
        IMiniGame activeChallenge,
        int nearbyIdx,
        String[] challengeTitles
    ) {
        if (sr == null || batch == null) return;

        // Main world pass
        Gdx.gl.glClearColor(0f, 0f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        mapRuntime.render(camera);

        renderExitDoorFromMap(tmxExitX, tmxExitY, exitUnlocked);
        worldRenderer.renderRoomProps(stateTime, terminalTiles,
            cameraPositions, drones, cctvAlerted,
            playerEntity, collisionMgr);

        worldRenderer.renderCheckpointBeacon(stateTime, checkpointX, checkpointY);
        worldRenderer.renderTerminalGlow(terminalTiles, terminalSolved);
        worldRenderer.renderClueObjects(stateTime, clueSystem,
            terminalTiles, terminalSolved, playerEntity, terminalPingTimer);
        worldRenderer.renderTerminalHints(stateTime, terminalPingTimer,
            terminalTiles, terminalSolved, clueSystem,
            playerEntity, PING_REVEAL_RADIUS);
        worldRenderer.renderExitGuidance(stateTime, exitUnlocked,
            playerEntity, tmxExitX, tmxExitY);

        renderPlayer(playerEntity, playerTc, frameDelta);
        renderParticles(pX, pY, pR, pG, pB, pLife, particleCount);

        // Mini-game pass
        if (activeChallenge != null && activeChallenge.isOpen()) {
            hudViewport.apply();
            sr.setProjectionMatrix(hudCamera.combined);
            batch.setProjectionMatrix(hudCamera.combined);
            activeChallenge.render(new MiniGameRenderContext(
                sr, batch,
                miniBodyFont, miniTitleFont, miniSmallFont, miniMonoFont
            ));
            return;
        }

        // HUD pass
        hudViewport.apply();
        sr.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);

        hudRenderer.renderHUD(stateTime, timeRemaining, missionElapsed,
            keysCollected, keysRequired,
            respawnsRemaining, maxRespawns,
            signalPingsRemaining,
            clueSystem, playerState, exitUnlocked,
            chaseWarningTimer,
            bannerTimer,
            activeChallenge != null && activeChallenge.isOpen(),
            drones, playerTc, nearbyIdx,
            null, challengeTitles);

        hudRenderer.renderMinimap(playerTc, collisionMgr.getWallGrid(),
            terminalTiles, terminalSolved, clueSystem,
            tmxExitX, tmxExitY, exitUnlocked,
            checkpointX, checkpointY,
            drones, stateTime);

        hudRenderer.renderThreatIndicator(playerTc, drones, stateTime);
        hudRenderer.renderChaseWarning(stateTime, chaseWarningTimer);

        if (bannerTimer > 0f && (activeChallenge == null || !activeChallenge.isOpen())) {
            hudRenderer.renderObjectiveBanner(stateTime, bannerTimer, bannerDuration,
                bannerTitle, bannerSubtitle, chaseWarningTimer);
        }

        if (gameOver || victory) {
            hudRenderer.renderEndScreen(victory, stateTime,
                keysCollected, keysRequired, missionElapsed,
                respawnsRemaining);
        }

        // Screen transition overlay
        if (transitionAlpha > 0.01f) {
            sr.begin(ShapeType.Filled);
            sr.setColor(0f, 0f, 0f, transitionAlpha);
            sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
            sr.end();
        }
    }

    private void renderExitDoorFromMap(float exitX, float exitY, boolean exitUnlocked) {
        TextureRegion region = exitUnlocked
            ? mapRuntime.getDoorOpenedRegion()
            : mapRuntime.getDoorClosedRegion();
        if (region == null) return;

        float ts = TileMap.TILE_SIZE;
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(region, exitX - ts / 2f, exitY - ts / 2f, ts, ts);
        batch.end();
    }

    private void renderPlayer(GameEntity playerEntity, TransformComponent tc, float frameDelta) {
        if (playerEntity == null || tc == null) return;
        float px = tc.getPosition().x;
        float py = tc.getPosition().y;
        float r = PLAYER_RADIUS;

        if (playerAnimator != null) {
            PhysicComponent phys = playerEntity.getComponent(PhysicComponent.class);
            float vx = phys != null ? phys.getVelocity().x : 0f;
            float vy = phys != null ? phys.getVelocity().y : 0f;
            playerAnimator.update(frameDelta, vx, vy);
            batch.begin();
            playerAnimator.drawCentered(batch, px, py, r * 2.8f);
            batch.end();
        } else {
            sr.begin(ShapeType.Filled);
            sr.setColor(0.3f, 0.8f, 1.0f, 1f);
            sr.triangle(px, py + r, px - r * 0.85f, py - r * 0.6f,
                px + r * 0.85f, py - r * 0.6f);
            sr.end();
        }
    }

    private void renderParticles(float[] pX, float[] pY,
                                  float[] pR, float[] pG, float[] pB,
                                  float[] pLife,
                                  int particleCount) {
        if (particleCount <= 0) return;
        sr.begin(ShapeType.Filled);
        for (int i = 0; i < particleCount; i++) {
            float alpha = Math.min(1f, pLife[i] * 2f);
            sr.setColor(pR[i], pG[i], pB[i], alpha);
            sr.circle(pX[i], pY[i], 2f + pLife[i] * 3f, 6);
        }
        sr.end();
    }

    public void dispose() {
        try {
            if (sr != null) sr.dispose();
            if (batch != null) batch.dispose();
            if (hudFont != null) hudFont.dispose();
            if (hudSmallFont != null) hudSmallFont.dispose();
            if (hudPanelFont != null) hudPanelFont.dispose();
            if (alertFont != null) alertFont.dispose();
            if (promptFont != null) promptFont.dispose();
            if (miniBodyFont != null) miniBodyFont.dispose();
            if (miniTitleFont != null) miniTitleFont.dispose();
            if (miniSmallFont != null) miniSmallFont.dispose();
            if (miniMonoFont != null) miniMonoFont.dispose();
            if (playerAnimator != null) playerAnimator.dispose();
        } finally {
            sr = null;
            batch = null;
            playerAnimator = null;
        }
    }
}

