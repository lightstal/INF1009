package io.github.INF1009_P10_Team7.cyber.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.cyber.clue.ClueSystem;
import io.github.INF1009_P10_Team7.cyber.level.LevelConfig;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.player.PlayerState;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;

/**
 * CyberHudRenderer, renders all HUD overlays for CyberGameScene.
 *
 * <p>Extracted from CyberGameScene (SRP): this class owns all 2D overlay
 * drawing, status panel, alert bar, minimap, threat indicator, objective
 * banners, chase warning, and end screen. CyberGameScene calls the public
 * render methods each frame; it retains no HUD-drawing logic itself.</p>
 *
 * <p>All game-state is passed in via method parameters so this renderer
 * stays stateless and reusable (OCP, DIP).</p>
 */
public class CyberHudRenderer {

    private final IShapeDraw sr;
    private final SpriteBatch   batch;
    private final BitmapFont    hudFont;
    private final BitmapFont    hudSmallFont;
    private final BitmapFont    hudPanelFont;
    private final BitmapFont    alertFont;
    private final BitmapFont    promptFont;
    private final GlyphLayout   layout;
    private final CyberSprites  sprites;
    private final LevelConfig   config;

    public CyberHudRenderer(IShapeDraw sr, SpriteBatch batch,
                             BitmapFont hudFont, BitmapFont hudSmallFont,
                             BitmapFont hudPanelFont, BitmapFont alertFont,
                             BitmapFont promptFont, GlyphLayout layout,
                             CyberSprites sprites, LevelConfig config) {
        this.sr           = sr;
        this.batch        = batch;
        this.hudFont      = hudFont;
        this.hudSmallFont = hudSmallFont;
        this.hudPanelFont = hudPanelFont;
        this.alertFont    = alertFont;
        this.promptFont   = promptFont;
        this.layout       = layout;
        this.sprites      = sprites;
        this.config       = config;
    }

    // =========================================================================
    // PUBLIC RENDER API (called by CyberGameScene each frame)
    // =========================================================================

    public void renderHUD(float stateTime, float timeRemaining, float missionElapsed,
                          int keysCollected, int KEYS_REQUIRED,
                          int respawnsRemaining, int maxRespawns,
                          int signalPingsRemaining,
                          ClueSystem clueSystem, PlayerState playerState,
                          boolean exitUnlocked, float chaseWarningTimer,
                          float bannerTimer, boolean activeChallenge,
                          DroneAI[] drones, TransformComponent playerTc,
                          int activeChallengeIdx, BitmapFont[] challengeFonts,
                          String[] challengeTitles) {

        float alert   = maxAlert(drones);
        boolean chasing = alert > 0.55f;
        float W = TileMap.WORLD_W, H = TileMap.WORLD_H;

        String[] levelParts   = splitLevelLabel(config.getLevelName());
        String levelTag       = levelParts[0];
        String levelLocation  = levelParts[1];
        float displayTime     = config.getTimeLimit() > 0 ? timeRemaining : missionElapsed;
        boolean countingDown  = config.getTimeLimit() > 0;
        String timeStr = String.format("TIME %d:%02d",
            (int)(displayTime / 60f), (int)(Math.max(0, displayTime) % 60f));

        GlyphLayout levelTagLayout = new GlyphLayout(hudPanelFont, levelTag);
        GlyphLayout levelLocLayout = new GlyphLayout(hudPanelFont, levelLocation);
        GlyphLayout timeLayout     = new GlyphLayout(hudPanelFont, timeStr);

        float panelX = 8f, panelY = H - 156f, panelW = 212f, panelH = 148f;
        float rightPanelH = 56f;
        float rightPanelW = Math.max(232f,
            Math.max(levelTagLayout.width, Math.max(levelLocLayout.width, timeLayout.width)) + 30f);
        rightPanelW = Math.min(296f, rightPanelW);
        float trX = W - rightPanelW - 10f;

        sr.beginFilled();
        sr.setColor(0.02f, 0.03f, 0.06f, 0.84f);
        sr.rect(panelX, panelY, panelW, panelH);
        sr.rect(trX, H - rightPanelH - 8f, rightPanelW, rightPanelH);

        sr.setColor(0.18f, 0.56f, 0.92f, 0.65f);
        sr.rect(panelX, panelY, 2f, panelH);
        sr.rect(trX + rightPanelW - 2f, H - rightPanelH - 8f, 2f, rightPanelH);

        float barX = panelX + 10f, barY = H - 15f, barW = panelW - 20f, barH = 5f;
        sr.setColor(0.08f, 0.10f, 0.13f, 1f);
        sr.rect(barX, barY, barW, barH);
        if (alert > 0.01f) {
            sr.setColor(alert, 0.24f * (1f - alert), 0.06f, 1f);
            sr.rect(barX, barY, barW * alert, barH);
        }
        sr.end();

        batch.begin();
        float lx   = panelX + 13f;
        float rowY = H - 24f;
        float line = 20f;

        hudPanelFont.setColor(chasing
            ? new Color(1f, 0.28f, 0.20f, 1f)
            : new Color(0.46f, 0.92f, 0.70f, 0.92f));
        hudPanelFont.draw(batch, chasing ? "STATUS // ALERT" : "STATUS // CLEAR", lx, rowY);

        hudPanelFont.setColor(0.92f, 0.86f, 0.30f, 1f);
        hudPanelFont.draw(batch, "KEYS   " + keysCollected + "/" + KEYS_REQUIRED, lx, rowY - line);

        hudPanelFont.setColor(respawnsRemaining <= 1
            ? new Color(1f, 0.30f, 0.22f, 1f)
            : new Color(0.52f, 0.88f, 0.98f, 1f));
        hudPanelFont.draw(batch, "LIVES  " + respawnsRemaining + "/" + maxRespawns, lx, rowY - line * 2f);

        hudPanelFont.setColor(0.54f, 0.92f, 0.66f, 1f);
        hudPanelFont.draw(batch, "PINGS  " + signalPingsRemaining, lx, rowY - line * 3f);

        hudPanelFont.setColor(0.26f, 0.84f, 1f, 1f);
        hudPanelFont.draw(batch, "INTEL  "
            + clueSystem.getCollectedCount() + "/" + clueSystem.getTotalClueObjects(),
            lx, rowY - line * 4f);

        hudPanelFont.setColor(0.72f, 0.78f, 0.88f, 0.90f);
        hudPanelFont.draw(batch, "MODE // " + playerState.getDisplayName(), lx, rowY - line * 5f);

        if (countingDown && timeRemaining <= 60f) {
            float urgency = 0.5f + 0.5f * (float)Math.sin(stateTime * (timeRemaining <= 30f ? 8f : 4f));
            hudPanelFont.setColor(1f, 0.20f + 0.20f * urgency, 0.10f, 1f);
        } else {
            hudPanelFont.setColor(0.72f, 0.78f, 0.88f, 0.90f);
        }
        hudPanelFont.draw(batch, timeStr, lx, rowY - line * 6f);

        hudPanelFont.setColor(0.55f, 0.80f, 0.96f, 0.95f);
        hudPanelFont.draw(batch, levelTag, trX + 12f, H - 20f);
        hudPanelFont.setColor(0.74f, 0.92f, 1f, 1f);
        hudPanelFont.draw(batch, levelLocation, trX + 12f, H - 41f);
        if (countingDown && timeRemaining <= 60f) {
            float urgency = 0.5f + 0.5f * (float)Math.sin(stateTime * (timeRemaining <= 30f ? 8f : 4f));
            hudPanelFont.setColor(1f, 0.20f + 0.20f * urgency, 0.10f, 1f);
        } else {
            hudPanelFont.setColor(0.70f, 0.76f, 0.86f, 0.92f);
        }
        hudPanelFont.draw(batch, timeStr, trX + rightPanelW - 12f - timeLayout.width, H - 41f);

        if (!activeChallenge && playerTc != null && activeChallengeIdx >= 0
                && challengeTitles != null && activeChallengeIdx < challengeTitles.length) {
            hudSmallFont.setColor(0.44f, 0.84f, 0.96f, 0.76f);
            String label = "NODE // " + challengeTitles[activeChallengeIdx];
            GlyphLayout nodeLayout = new GlyphLayout(hudSmallFont, label);
            hudSmallFont.draw(batch, label, W / 2f - nodeLayout.width / 2f, 18f);
        }

        if (exitUnlocked) {
            float p = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f);
            batch.end();
            sr.beginFilled();
            sr.setColor(0.16f * p, 0.02f, 0.22f * p, 0.74f);
            sr.rect(W / 2f - 208f, 2f, 416f, 18f);
            sr.end();
            batch.begin();
            promptFont.setColor(0.90f * p, 0.36f, 1f * p, 1f);
            String exitMsg = "EXTRACTION POINT ACTIVE — PROCEED TO EXIT";
            GlyphLayout exitLayout = new GlyphLayout(promptFont, exitMsg);
            promptFont.draw(batch, exitMsg, W / 2f - exitLayout.width / 2f, 16f);
        }
        batch.end();
    }

    public void renderObjectiveBanner(float stateTime, float bannerTimer, float bannerDuration,
                                       String bannerTitle, String bannerSubtitle,
                                       float chaseWarningTimer) {
        if (bannerTimer <= 0f) return;

        float W = TileMap.WORLD_W, H = TileMap.WORLD_H;

        float elapsed = Math.max(0f, bannerDuration - bannerTimer);
        float fadeIn  = Math.min(1f, elapsed / 0.35f);
        float fadeOut = Math.min(1f, bannerTimer / 0.4f);
        float alpha   = Math.min(1f, Math.min(fadeIn, fadeOut));

        float glitchX = 0f;
        float glitchCycle = (stateTime * 7.3f) % 6f;
        if (glitchCycle < 0.08f) glitchX = (float)(Math.random() * 6f - 3f);

        float maxTextW = 620f;
        float padX = 24f, padY = 14f, titleSubGap = 6f;

        layout.setText(alertFont, bannerTitle);
        float titleW = layout.width;
        float titleH = layout.height;

        GlyphLayout subLayout = new GlyphLayout(hudFont, bannerSubtitle,
            new Color(0.75f, 0.82f, 0.88f, alpha * 0.9f), maxTextW, Align.center, true);
        float subW = subLayout.width;
        float subH = subLayout.height;

        float contentW = Math.max(Math.max(titleW, subW), 280f);
        float boxW = contentW + padX * 2f;
        float boxH = titleH + subH + padY * 2f + titleSubGap;
        float boxX = W / 2f - boxW / 2f;

        float topOffset = chaseWarningTimer > 0f ? 195f : 115f;
        float boxY = H - topOffset - boxH;

        boolean isAlert = bannerTitle.contains("TERMINATED") || bannerTitle.contains("BREACH")
            || bannerTitle.contains("SURVEILLANCE") || bannerTitle.contains("DETECTED")
            || bannerTitle.contains("INTRUSION") || bannerTitle.contains("DEPLETED");

        sr.beginFilled();
        sr.setColor(0.01f, 0.02f, 0.06f, 0.82f * alpha);
        sr.rect(boxX + glitchX, boxY, boxW, boxH);
        if (isAlert) sr.setColor(1f, 0.15f, 0.1f, 0.6f * alpha);
        else         sr.setColor(0f, 0.85f, 0.9f, 0.7f * alpha);
        sr.rect(boxX + glitchX, boxY + boxH - 2f, boxW, 2f);
        sr.rect(boxX + glitchX, boxY, boxW, 2f);
        if (isAlert) sr.setColor(1f, 0.2f, 0.1f, 0.85f * alpha);
        else         sr.setColor(0f, 0.95f, 1f, 0.85f * alpha);
        sr.rect(boxX + glitchX, boxY + 4f, 3f, boxH - 8f);
        if (isAlert) { sr.setColor(1f, 0.1f, 0.15f, 0.10f * alpha); sr.rect(boxX + glitchX, boxY, boxW, boxH); }
        sr.setColor(0f, 0f, 0f, 0.05f * alpha);
        for (float sy = boxY; sy < boxY + boxH; sy += 3f) sr.rect(boxX + glitchX, sy, boxW, 1f);
        sr.end();

        sr.beginLine();
        float glowPulse = 0.5f + 0.3f * (float)Math.sin(stateTime * 5f);
        if (isAlert) sr.setColor(1f, 0.2f, 0.15f, (0.35f + glowPulse * 0.25f) * alpha);
        else         sr.setColor(0f, 0.9f, 1f, (0.3f + glowPulse * 0.2f) * alpha);
        sr.rect(boxX - 1f + glitchX, boxY - 1f, boxW + 2f, boxH + 2f);
        sr.end();

        batch.begin();
        if (isAlert) {
            float flicker = 0.7f + 0.3f * (float)Math.sin(stateTime * 12f);
            alertFont.setColor(1f, 0.25f * flicker, 0.15f, alpha);
        } else {
            alertFont.setColor(0f, 0.95f, 1f, alpha);
        }
        layout.setText(alertFont, bannerTitle);
        alertFont.draw(batch, bannerTitle, W / 2f - layout.width / 2f + glitchX, boxY + boxH - padY);
        hudFont.setColor(0.75f, 0.82f, 0.88f, alpha * 0.9f);
        hudFont.draw(batch, bannerSubtitle,
            W / 2f - maxTextW / 2f + glitchX,
            boxY + boxH - padY - titleH - titleSubGap,
            maxTextW, Align.center, true);
        batch.end();
    }

    /**
     * Renders the minimap panel in the corner of the HUD.
     * Draws wall cells, terminal/exit markers, drone positions,
     * clue objects, checkpoint, and the player blip.
     */
    public void renderMinimap(TransformComponent tc, boolean[][] wallGrid,
                               int[][] terminalTiles, boolean[] terminalSolved,
                               ClueSystem clueSystem, float tmxExitX, float tmxExitY,
                               boolean exitUnlocked, float checkpointX, float checkpointY,
                               DroneAI[] drones, float stateTime) {
        if (tc == null) return;
        float mmW = 140f, mmH = 77f;
        float mmX = TileMap.WORLD_W - mmW - 10f, mmY = 10f;
        float scaleX = mmW / TileMap.WORLD_W, scaleY = mmH / TileMap.WORLD_H;

        sr.beginFilled();
        sr.setColor(0f, 0f, 0f, 0.6f);
        sr.rect(mmX - 2, mmY - 2, mmW + 4, mmH + 4);

        float tileW = scaleX * TileMap.TILE_SIZE;
        float tileH = scaleY * TileMap.TILE_SIZE;
        if (wallGrid != null) {
            for (int row = 0; row < TileMap.ROWS; row++) {
                for (int col = 0; col < TileMap.COLS; col++) {
                    if (wallGrid[row][col]) sr.setColor(0.2f, 0.25f, 0.3f, 0.8f);
                    else sr.setColor(0.05f, 0.08f, 0.1f, 0.5f);
                    sr.rect(mmX + col * tileW,
                        mmY + (TileMap.WORLD_H - (row + 1) * TileMap.TILE_SIZE) * scaleY,
                        tileW, tileH);
                }
            }
        }
        sr.end();

        batch.begin();
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]) * scaleX + mmX;
            float ty = TileMap.tileCentreY(terminalTiles[i][1]) * scaleY + mmY;
            if (sprites.get("mapPin") != null) sprites.drawCentered(batch, "mapPin", tx, ty + 2f, 7f, 1f);
        }
        batch.end();

        sr.beginFilled();
        for (ClueSystem.ClueObject clue : clueSystem.getClueObjects()) {
            if (clue.collected || !clue.isRevealed()) continue;
            float clx = TileMap.tileCentreX(clue.tileCol) * scaleX + mmX;
            float cly = TileMap.tileCentreY(clue.tileRow) * scaleY + mmY;
            float clBlink = 0.5f + 0.5f * (float)Math.sin(stateTime * 4f + clue.tileCol);
            sr.setColor(0.2f, 0.85f, 1f, 0.9f * clBlink);
            sr.triangle(clx, cly + 2.5f, clx - 1.8f, cly, clx + 1.8f, cly);
            sr.triangle(clx, cly - 2.5f, clx - 1.8f, cly, clx + 1.8f, cly);
        }
        float ex = tmxExitX * scaleX + mmX;
        float ey = tmxExitY * scaleY + mmY;
        if (exitUnlocked) sr.setColor(0.8f, 0f, 1f, 1f);
        else sr.setColor(0.3f, 0.05f, 0.05f, 0.8f);
        sr.rect(ex - 2, ey - 2, 4, 4);
        float cx = checkpointX * scaleX + mmX;
        float cy = checkpointY * scaleY + mmY;
        sr.setColor(0.2f, 0.95f, 1f, 0.95f);
        sr.rect(cx - 2.5f, cy - 2.5f, 5f, 5f);
        float px = tc.getPosition().x * scaleX + mmX;
        float py = tc.getPosition().y * scaleY + mmY;
        float blink = 0.7f + 0.3f * (float)Math.sin(stateTime * 8f);
        sr.setColor(0.3f, 0.8f * blink, 1f, 1f);
        sr.rect(px - 2f, py - 2f, 4f, 4f);
        for (DroneAI d : drones) {
            float dx = d.getPosition().x * scaleX + mmX;
            float dy = d.getPosition().y * scaleY + mmY;
            sr.setColor(1f, 0.2f, 0.1f, 0.9f);
            sr.rect(dx - 1.5f, dy - 1.5f, 3f, 3f);
        }
        sr.end();

        sr.beginLine();
        sr.setColor(0.3f, 0.5f, 0.7f, 0.6f);
        sr.rect(mmX - 2, mmY - 2, mmW + 4, mmH + 4);
        sr.end();
    }

    /**
     * Renders the radial threat indicator that shows relative drone positions
     * around the player as directional warning arrows.
     */
    public void renderThreatIndicator(TransformComponent tc, DroneAI[] drones, float stateTime) {
        if (tc == null || drones.length == 0) return;
        Vector2 pp = tc.getPosition();
        float nearestDist = Float.MAX_VALUE;
        DroneAI nearest = null;
        for (DroneAI d : drones) {
            float ddx = d.getPosition().x - pp.x;
            float ddy = d.getPosition().y - pp.y;
            float dd = ddx * ddx + ddy * ddy;
            if (dd < nearestDist) { nearestDist = dd; nearest = d; }
        }
        if (nearest == null) return;
        nearestDist = (float)Math.sqrt(nearestDist);
        if (nearestDist > 400f) return;

        float angle = (float)Math.atan2(
            nearest.getPosition().y - pp.y, nearest.getPosition().x - pp.x);
        float indicatorDist = 60f;
        float ix = TileMap.WORLD_W / 2f + (float)Math.cos(angle) * indicatorDist;
        float iy = TileMap.WORLD_H / 2f + (float)Math.sin(angle) * indicatorDist;

        float alert  = nearest.getAlertLevel();
        float pulse  = 0.5f + 0.5f * (float)Math.sin(stateTime * 6f);
        float t = Math.max(0f, Math.min(1f, 1f - nearestDist / 400f));

        sr.beginFilled();
        sr.setColor(Math.min(1f, t + alert), (1f - t) * (1f - alert), 0f, 0.5f + 0.3f * pulse);
        float size = 8f + alert * 6f;
        float perpAngle = angle + (float)Math.PI / 2f;
        float tipX  = ix + (float)Math.cos(angle)      * size;
        float tipY  = iy + (float)Math.sin(angle)      * size;
        float baseX1 = ix + (float)Math.cos(perpAngle) * size * 0.5f;
        float baseY1 = iy + (float)Math.sin(perpAngle) * size * 0.5f;
        float baseX2 = ix - (float)Math.cos(perpAngle) * size * 0.5f;
        float baseY2 = iy - (float)Math.sin(perpAngle) * size * 0.5f;
        sr.triangle(tipX, tipY, baseX1, baseY1, baseX2, baseY2);
        sr.end();
    }

    /**
     * Renders a full-screen red-tinted chase warning banner when a drone
     * has just spotted the player.
     */
    public void renderChaseWarning(float stateTime, float chaseWarningTimer) {
        if (chaseWarningTimer <= 0f) return;
        float W = TileMap.WORLD_W, H = TileMap.WORLD_H;
        float pulse     = 0.55f + 0.45f * (float)Math.sin(stateTime * 10f);
        float fadeAlpha = Math.min(1f, chaseWarningTimer / 0.25f);

        String title = "INTRUSION DETECTED";
        String sub   = "Evade behind cover. Break line of sight to drop pursuit.";
        float maxTextW = 560f;
        float padX = 20f, padY = 14f;

        layout.setText(alertFont, title);
        float titleW = layout.width;
        float titleH = layout.height;
        GlyphLayout subLayout = new GlyphLayout(hudFont, sub,
            new Color(1f, 0.9f, 0.7f, fadeAlpha), maxTextW, Align.center, true);
        float subW = subLayout.width;
        float subH = subLayout.height;

        float boxW = Math.max(titleW, subW) + padX * 2f;
        float boxH = titleH + subH + padY * 3f + 6f;
        float boxX = W / 2f - boxW / 2f;
        float boxY = H - boxH - 8f;

        sr.beginFilled();
        sr.setColor(0.04f, 0.01f, 0.02f, 0.85f * fadeAlpha); sr.rect(boxX, boxY, boxW, boxH);
        sr.setColor(1f, 0.15f, 0.1f, 0.75f * fadeAlpha * pulse);
        sr.rect(boxX, boxY + boxH - 2f, boxW, 2f);
        sr.rect(boxX, boxY, boxW, 2f);
        sr.setColor(1f, 0.2f, 0.1f, 0.9f * fadeAlpha); sr.rect(boxX, boxY + 4f, 3f, boxH - 8f);
        sr.setColor(1f, 0.05f, 0.05f, 0.08f * fadeAlpha); sr.rect(boxX, boxY, boxW, boxH);
        sr.setColor(0f, 0f, 0f, 0.06f * fadeAlpha);
        for (float sy = boxY; sy < boxY + boxH; sy += 3f) sr.rect(boxX, sy, boxW, 1f);
        sr.end();

        sr.beginLine();
        sr.setColor(1f, 0.2f, 0.1f, 0.5f * fadeAlpha * pulse);
        sr.rect(boxX - 1f, boxY - 1f, boxW + 2f, boxH + 2f);
        sr.end();

        batch.begin();
        alertFont.setColor(1f, 0.15f * pulse, 0.05f, fadeAlpha);
        layout.setText(alertFont, title);
        alertFont.draw(batch, title, W / 2f - layout.width / 2f, boxY + boxH - padY);
        hudFont.setColor(1f, 0.9f, 0.7f, fadeAlpha * 0.9f);
        hudFont.draw(batch, sub, W / 2f - maxTextW / 2f,
            boxY + boxH - padY - titleH - 8f, maxTextW, Align.center, true);
        batch.end();
    }

    /**
     * Renders the victory or game-over overlay directly on the HUD layer.
     * Shown when {@code gameOver} or {@code victory} is {@code true}.
     */
    public void renderEndScreen(boolean win, float stateTime,
                                 int keysCollected, int KEYS_REQUIRED, float missionElapsed,
                                 int respawnsRemaining) {
        sr.beginFilled();
        sr.setColor(0f, 0f, 0f, 0.86f);
        sr.rect(0, 0, TileMap.WORLD_W, TileMap.WORLD_H);
        sr.end();

        batch.begin();
        if (win) {
            float fl = 0.6f + 0.4f * (float)Math.sin(stateTime * 3.5f);
            alertFont.setColor(fl, 0f, fl, 1f);
            String t = "SYSTEM  BREACHED  // ACCESS GRANTED";
            layout.setText(alertFont, t);
            alertFont.draw(batch, t,
                TileMap.WORLD_W / 2f - layout.width / 2f, TileMap.WORLD_H / 2f + 70f);
            hudFont.setColor(Color.YELLOW);
            hudFont.draw(batch, "Nodes compromised : " + keysCollected + " / " + KEYS_REQUIRED,
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f + 20f);
            hudFont.draw(batch, String.format("Mission time      : %d:%02d",
                (int)(missionElapsed / 60f), (int)(missionElapsed % 60f)),
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 12f);
            hudFont.draw(batch, "Lives remaining   : " + respawnsRemaining,
                TileMap.WORLD_W / 2f - 140f, TileMap.WORLD_H / 2f - 44f);
        } else {
            float fl = 0.6f + 0.4f * (float)Math.sin(stateTime * 6f);
            alertFont.setColor(fl, 0f, 0f, 1f);
            String m = "CONNECTION  SEVERED  // MISSION FAILED";
            layout.setText(alertFont, m);
            alertFont.draw(batch, m,
                TileMap.WORLD_W / 2f - layout.width / 2f, TileMap.WORLD_H / 2f + 50f);
        }
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "[ E ]  [ ENTER ]  [ SPACE ]  Continue",
            TileMap.WORLD_W / 2f - 160f, TileMap.WORLD_H / 2f - 110f);
        batch.end();
    }


    // private helpers
    private float maxAlert(DroneAI[] drones) {
        float m = 0;
        for (DroneAI d : drones) m = Math.max(m, d.getAlertLevel());
        return m;
    }

    private String[] splitLevelLabel(String rawLevelName) {
        String safe = rawLevelName == null ? "LEVEL" : rawLevelName.trim().replace("—", "-");
        String[] parts = safe.split("\\s*-\\s*", 2);
        return parts.length == 2
            ? new String[]{ parts[0].trim(), parts[1].trim() }
            : new String[]{ safe, "CYBER FACILITY" };
    }
}
