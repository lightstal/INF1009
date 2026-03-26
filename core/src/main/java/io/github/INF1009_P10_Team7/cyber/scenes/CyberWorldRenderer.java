package io.github.INF1009_P10_Team7.cyber.scenes;


import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.render.IShapeDraw;
import io.github.INF1009_P10_Team7.engine.render.ISpriteDraw;
import io.github.INF1009_P10_Team7.engine.render.ITextDraw;
import io.github.INF1009_P10_Team7.engine.render.ColorValue;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.engine.collision.IWorldCollisionQuery;
import io.github.INF1009_P10_Team7.cyber.clue.ClueSystem;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.render.CyberSprites;

/**
 * CyberWorldRenderer — renders all in-world visual elements for CyberGameScene.
 *
 * <p>Extracted from CyberGameScene (SRP): this class owns all world-space
 * drawing — room props, security cameras, drones, terminals, clue objects,
 * signal ping effects, checkpoint beacon, exit door, and exit guidance.
 * CyberGameScene calls the public render methods each frame.</p>
 *
 * <p>All required state is passed in as method parameters (OCP, DIP) so this
 * renderer has no direct dependency on CyberGameScene fields.</p>
 */
public class CyberWorldRenderer {

    private final IShapeDraw                 sr;
    private final ISpriteDraw                spriteDraw;
    private final CyberSprites               sprites;
    private final IInputController           input;
    private final ITextDraw                  hudSmallTextDraw;
    private final ITextDraw                  promptTextDraw;

    public CyberWorldRenderer(IShapeDraw sr, ISpriteDraw spriteDraw,
                               ITextDraw hudSmallTextDraw, ITextDraw promptTextDraw,
                               CyberSprites sprites, IInputController input) {
        this.sr          = sr;
        this.spriteDraw  = spriteDraw;
        this.hudSmallTextDraw = hudSmallTextDraw;
        this.promptTextDraw = promptTextDraw;
        this.sprites     = sprites;
        this.input       = input;
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * Renders all in-world room props: ceiling lights, security cameras,
     * and drone sprites. Called every frame by {@code CyberGameScene.onRender()}.
     */
    public void renderRoomProps(float stateTime, int[][] terminalTiles,
                                 int[][] camPositions, DroneAI[] drones,
                                 boolean[] cctvAlerted, GameEntity playerEntity,
                                 IWorldCollisionQuery collisionMgr) {
        float ts = TileMap.TILE_SIZE;
        renderSecurityCameras(ts, stateTime, camPositions, cctvAlerted, playerEntity, collisionMgr);
        renderDroneSprites(ts, stateTime, drones);
    }

    public void renderTerminalGlow(int[][] terminalTiles, boolean[] terminalSolved) {
        float ts = TileMap.TILE_SIZE;
        if (sprites.get("terminal") != null) {
            spriteDraw.begin();
            for (int i = 0; i < terminalTiles.length; i++) {
                if (terminalSolved[i]) continue;
                float tx = TileMap.tileLeft(terminalTiles[i][0]) + ts * 0.5f;
                float ty = TileMap.tileBottom(terminalTiles[i][1]) + ts * 0.5f;
                float size = ts * 0.85f;
                spriteDraw.resetTint();
                spriteDraw.draw("terminal", tx - size / 2f, ty - size / 2f, size, size);
            }
            spriteDraw.end();
        }
    }

    public void renderClueObjects(float stateTime, ClueSystem clueSystem,
                                   int[][] terminalTiles, boolean[] terminalSolved,
                                   GameEntity playerEntity, float terminalPingTimer) {
        float ts = TileMap.TILE_SIZE;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        Vector2 pp = tc != null ? tc.getPosition() : null;
        float revealFallbackRadius = ts * 1.15f;

        for (ClueSystem.ClueObject clue : clueSystem.getClueObjects()) {
            if (clue.collected) continue;
            if (pp == null || !clueSystem.isVisible(clue, pp.x, pp.y, revealFallbackRadius)) continue;

            float cx = TileMap.tileCentreX(clue.tileCol);
            float cy = TileMap.tileCentreY(clue.tileRow);
            ColorValue accent = getClueAccent(clue.objectName);

            drawSpriteCenteredPreserveAspect(getClueSpriteKey(clue.objectName), cx, cy, ts * 0.72f, 0.92f);

            if (pp != null && dist(pp.x, pp.y, cx, cy) < ts * 2.0f) {
                drawWorldPromptCard(cx, cy + ts * 0.46f,
                    getCluePromptTitle(clue.objectName), buildClueAction(clue.objectName), accent);
            }
        }

        if (pp != null) {
            for (int i = 0; i < terminalTiles.length; i++) {
                if (terminalSolved[i]) continue;
                float tx = TileMap.tileCentreX(terminalTiles[i][0]);
                float ty = TileMap.tileCentreY(terminalTiles[i][1]);
                if (dist(pp.x, pp.y, tx, ty) < TileMap.TILE_SIZE * 2.0f) {
                    boolean locked = !clueSystem.canAccessTerminal(i, terminalTiles.length);
                    if (locked) {
                        int requiredForThis = Math.min(i - 1, clueSystem.getTotalClueObjects());
                        String status = clueSystem.getCollectedCount() + "/"
                            + requiredForThis + " INTEL";
                        drawWorldPromptCard(tx, ty + ts * 0.52f,
                            "LOCKED", status, new ColorValue(1f, 0.34f, 0.24f, 1f));
                    } else {
                        drawWorldPromptCard(tx, ty + ts * 0.52f,
                            "TERMINAL", buildPrompt("INTERACT", "JACK IN"),
                            new ColorValue(0.10f, 0.90f, 0.55f, 1f));
                    }
                    break;
                }
            }
        }
    }

    public void renderTerminalHints(float stateTime, float terminalPingTimer,
                                     int[][] terminalTiles, boolean[] terminalSolved,
                                     ClueSystem clueSystem, GameEntity playerEntity,
                                     float PING_REVEAL_RADIUS) {
        if (terminalPingTimer <= 0f) return;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        if (tc == null) return;

        Vector2 pp = tc.getPosition();
        float pulse = 0.55f + 0.45f * (float)Math.sin(stateTime * 7.5f);

        int nearestTerminal = -1;
        float best = PING_REVEAL_RADIUS;
        for (int i = 0; i < terminalTiles.length; i++) {
            if (terminalSolved[i]) continue;
            float tx = TileMap.tileCentreX(terminalTiles[i][0]);
            float ty = TileMap.tileCentreY(terminalTiles[i][1]);
            float d  = dist(pp.x, pp.y, tx, ty);
            if (d < best) { best = d; nearestTerminal = i; }
        }

        sr.beginLine();
        sr.setColor(0.20f, 0.88f, 1f, 0.28f + pulse * 0.16f);

        if (nearestTerminal >= 0 && !terminalSolved[nearestTerminal]) {
            float tx = TileMap.tileCentreX(terminalTiles[nearestTerminal][0]);
            float ty = TileMap.tileCentreY(terminalTiles[nearestTerminal][1]);
            sr.line(pp.x, pp.y, tx, ty);
        }
        for (ClueSystem.ClueObject clue : clueSystem.getClueObjects()) {
            if (clue.collected) continue;
            if (!clue.isRevealed() && !clueSystem.isVisible(clue, pp.x, pp.y, TileMap.TILE_SIZE * 1.15f)) continue;
            float cx = TileMap.tileCentreX(clue.tileCol);
            float cy = TileMap.tileCentreY(clue.tileRow);
            sr.line(pp.x, pp.y, cx, cy);
        }
        sr.end();

        sr.beginFilled();
        if (nearestTerminal >= 0 && !terminalSolved[nearestTerminal]) {
            float tx = TileMap.tileCentreX(terminalTiles[nearestTerminal][0]);
            float ty = TileMap.tileCentreY(terminalTiles[nearestTerminal][1]);
            float s  = 1.8f + pulse * 1.4f;
            sr.setColor(0.20f, 0.88f, 1f, 0.42f + pulse * 0.18f);
            sr.circle(tx, ty, s, 16);
        }
        for (ClueSystem.ClueObject clue : clueSystem.getClueObjects()) {
            if (clue.collected) continue;
            if (!clue.isRevealed() && !clueSystem.isVisible(clue, pp.x, pp.y, TileMap.TILE_SIZE * 1.15f)) continue;
            float cx = TileMap.tileCentreX(clue.tileCol);
            float cy = TileMap.tileCentreY(clue.tileRow);
            float s  = 1.8f + pulse * 1.2f;
            ColorValue accent = getClueAccent(clue.objectName);
            sr.setColor(accent.r, accent.g, accent.b, 0.34f + pulse * 0.16f);
            sr.circle(cx, cy, s, 16);
        }
        sr.end();
    }

    public void renderCheckpointBeacon(float stateTime, float checkpointX, float checkpointY) {
        float pulse = 0.45f + 0.25f * (float)Math.sin(stateTime * 3f);
        float s = 2.8f + pulse;
        sr.beginFilled();
        sr.setColor(0.2f, 0.85f, 1f, 0.14f * pulse);
        sr.triangle(checkpointX, checkpointY + s, checkpointX - s, checkpointY, checkpointX + s, checkpointY);
        sr.triangle(checkpointX, checkpointY - s, checkpointX - s, checkpointY, checkpointX + s, checkpointY);
        sr.end();
    }

    /**
     * Renders the exit door sprite (closed or open) at its world position.
     * Switches to the open texture once {@code exitUnlocked} is {@code true}.
     */
    public void renderTmxExitDoor(float tmxExitX, float tmxExitY, boolean exitUnlocked) {
        // Door sprite is now rendered from TMX TextureRegion in CyberGameRenderer.
        // Intentionally left as a no-op.
    }

    public void renderExitGuidance(float stateTime, boolean exitUnlocked,
                                    GameEntity playerEntity,
                                    float tmxExitX, float tmxExitY) {
        if (!exitUnlocked) return;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        if (tc == null) return;

        float pulse = 0.35f + 0.25f * (float)Math.sin(stateTime * 5.5f);
        sr.beginLine();
        sr.setColor(0.85f, 0.15f, 1f, 0.15f + pulse * 0.15f);
        sr.line(tc.getPosition().x, tc.getPosition().y, tmxExitX, tmxExitY);
        sr.end();
        sr.beginFilled();
        float s = 4.0f + pulse * 2f;
        sr.setColor(0.8f, 0.1f, 1f, 0.28f * pulse);
        sr.triangle(tmxExitX, tmxExitY + s, tmxExitX - s, tmxExitY, tmxExitX + s, tmxExitY);
        sr.triangle(tmxExitX, tmxExitY - s, tmxExitX - s, tmxExitY, tmxExitX + s, tmxExitY);
        sr.end();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void renderSecurityCameras(float ts, float stateTime, int[][] camPositions,
                                        boolean[] cctvAlerted, GameEntity playerEntity,
                                        IWorldCollisionQuery collisionMgr) {
        if (sprites.get("secCamera") == null) return;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        Vector2 pp = tc != null ? tc.getPosition() : null;

        sr.beginFilled();
        for (int i = 0; i < camPositions.length; i++) {
            float cx = TileMap.tileCentreX(camPositions[i][0]);
            float cy = TileMap.tileCentreY(camPositions[i][1]);
            float phase   = i * 1.3f;
            float panAng  = (float)Math.sin(stateTime * 0.7f + phase) * 40f;
            float baseAng = camPositions[i][2];
            float totalAng = baseAng + panAng;
            float pulse = 0.88f + 0.12f * (float)Math.sin(stateTime * 5.0f + i);

            int visionState = 0;
            if (pp != null) {
                float pdx = pp.x - cx, pdy = pp.y - cy;
                float pDist = (float)Math.sqrt(pdx * pdx + pdy * pdy);
                if (pDist <= ts * 2.8f && collisionMgr.hasLineOfSight(cx, cy, pp.x, pp.y)) {
                    float angleToPlayer = (float)Math.toDegrees(Math.atan2(pdy, pdx));
                    float angleDiff = angleToPlayer - totalAng;
                    while (angleDiff > 180f) angleDiff -= 360f;
                    while (angleDiff < -180f) angleDiff += 360f;
                    if (Math.abs(angleDiff) <= 36f) visionState = 1;
                    if (Math.abs(angleDiff) <= 28f) visionState = 2;
                }
            }
            if (cctvAlerted != null && i < cctvAlerted.length && cctvAlerted[i]) visionState = 2;

            ColorValue coneColor;
            if (visionState >= 2)     coneColor = new ColorValue(1.00f, 0.18f, 0.15f, 0.11f * pulse);
            else if (visionState == 1) coneColor = new ColorValue(1.00f, 0.58f, 0.16f, 0.09f * pulse);
            else                       coneColor = new ColorValue(1.00f, 0.92f, 0.20f, 0.06f * pulse);
            drawSoftCone(sr, cx, cy, totalAng, 58f, ts * 2.4f, coneColor, 18);
        }
        sr.end();

        spriteDraw.begin();
        for (int i = 0; i < camPositions.length; i++) {
            float cx = TileMap.tileCentreX(camPositions[i][0]);
            float cy = TileMap.tileCentreY(camPositions[i][1]);
            float phase   = i * 1.3f;
            float panAng  = (float)Math.sin(stateTime * 0.7f + phase) * 40f;
            float totalAng = camPositions[i][2] + panAng;
            boolean detected = cctvAlerted != null && i < cctvAlerted.length && cctvAlerted[i];
            if (detected) spriteDraw.setTint(1f, 0.82f, 0.82f, 0.98f);
            else          spriteDraw.setTint(1f, 1f, 1f, 0.92f);
            float size = ts * 0.72f;
            spriteDraw.drawRotated("secCamera", cx - size / 2f, cy - size / 2f, size, size, totalAng - 90f);
            spriteDraw.resetTint();
        }
        spriteDraw.end();
    }

    private void renderDroneSprites(float ts, float stateTime, DroneAI[] drones) {
        if (drones == null || drones.length == 0) return;

        sr.beginFilled();
        for (DroneAI drone : drones) {
            float dx = drone.getPosition().x, dy = drone.getPosition().y;
            ColorValue coneColor;
            if (drone.isDetectionSuppressed())            coneColor = new ColorValue(0.45f, 0.78f, 1.00f, 0.035f);
            else if ("CHASE".equals(drone.getStateName())) coneColor = new ColorValue(1.00f, 0.18f, 0.16f, 0.12f);
            else if ("SEARCH".equals(drone.getStateName()) || drone.getAlertLevel() > 0.38f)
                                                           coneColor = new ColorValue(1.00f, 0.58f, 0.14f, 0.095f);
            else                                           coneColor = new ColorValue(1.00f, 0.92f, 0.20f, 0.065f);
            drawSoftCone(sr, dx, dy, drone.getFacingAngle(), drone.getSightAngle(),
                drone.getSightRange() * 0.78f, coneColor, 18);
            sr.setColor(0f, 0f, 0f, 0.10f);
            sr.rect(dx - ts * 0.22f, dy - ts * 0.06f, ts * 0.44f, ts * 0.12f);
        }
        sr.end();

        spriteDraw.begin();
        for (DroneAI drone : drones) {
            String spriteKey = drone.isDetectionSuppressed() ? "droneDamagedRed" : "dronePatrolRed";
            if ("CHASE".equals(drone.getStateName()) || "SEARCH".equals(drone.getStateName()))
                spriteKey = "droneDetectRed";

            if (!sprites.has(spriteKey)) continue;

            float dx = drone.getPosition().x, dy = drone.getPosition().y;
            float aspect = sprites.getAspectRatio(spriteKey);
            float drawW  = ts * 1.30f;
            float drawH  = drawW / aspect;
            if (drawH > ts * 0.95f) { float sc = (ts * 0.95f) / drawH; drawW *= sc; drawH *= sc; }

            boolean chasing   = "CHASE".equals(drone.getStateName());
            boolean searching = "SEARCH".equals(drone.getStateName());
            if (chasing)                        spriteDraw.setTint(1f, 0.85f, 0.85f, 1f);
            else if (searching)                 spriteDraw.setTint(1f, 0.92f, 0.78f, 0.98f);
            else if (drone.isDetectionSuppressed()) spriteDraw.setTint(0.84f, 0.92f, 1f, 0.92f);
            else                                spriteDraw.setTint(1f, 1f, 1f, 0.96f);

            spriteDraw.drawRotated(spriteKey, dx - drawW / 2f, dy - drawH / 2f,
                drawW, drawH, drawW / 2f, drawH / 2f, drone.getFacingAngle() - 90f);
            spriteDraw.resetTint();
        }
        spriteDraw.end();
    }

    private void drawSoftCone(IShapeDraw renderer, float ox, float oy, float facingDeg,
                               float coneAngleDeg, float length, ColorValue color, int segments) {
        renderer.setColor(color.r, color.g, color.b, color.a);
        float start = facingDeg - coneAngleDeg * 0.5f;
        float step  = coneAngleDeg / Math.max(1, segments);
        for (int i = 0; i < segments; i++) {
            float a1 = (float)Math.toRadians(start + step * i);
            float a2 = (float)Math.toRadians(start + step * (i + 1));
            renderer.triangle(ox, oy,
                ox + (float)Math.cos(a1) * length, oy + (float)Math.sin(a1) * length,
                ox + (float)Math.cos(a2) * length, oy + (float)Math.sin(a2) * length);
        }
    }

    private String buildPrompt(String actionName, String verb) {
        String key = input.getKeyName(actionName);
        if (key == null || key.trim().isEmpty() || "UNKNOWN".equalsIgnoreCase(key)) key = actionName;
        return "[" + key.toUpperCase() + "] " + verb;
    }

    private String getClueSpriteKey(String objectName) {
        String n = objectName == null ? "INTEL" : objectName.trim().toUpperCase();
        if (n.contains("KEY")) return "intelKeycard";
        if (n.contains("USB")) return "intelUsb";
        return "intelServerLog";
    }

    private ColorValue getClueAccent(String objectName) {
        String n = objectName == null ? "INTEL" : objectName.trim().toUpperCase();
        if (n.contains("KEY")) return new ColorValue(1f, 0.83f, 0.28f, 1f);
        if (n.contains("USB")) return new ColorValue(0.22f, 0.95f, 0.72f, 1f);
        return new ColorValue(0.24f, 0.82f, 1f, 1f);
    }

    private String getCluePromptTitle(String objectName) {
        String n = objectName == null ? "INTEL CACHE" : objectName.trim().toUpperCase();
        if (n.contains("KEY")) return "SECURITY KEY";
        if (n.contains("USB")) return "USB FRAGMENT";
        if (n.contains("LOG")) return "SERVER LOG";
        return n;
    }

    private String buildClueAction(String objectName) {
        String n = objectName == null ? "INTEL" : objectName.trim().toUpperCase();
        if (n.contains("KEY") || n.contains("USB")) return buildPrompt("INTERACT", "TAKE");
        if (n.contains("LOG"))                       return buildPrompt("INTERACT", "READ");
        return buildPrompt("INTERACT", "COLLECT");
    }

    private void drawWorldPromptCard(float centerX, float baselineY,
                                      String title, String action, ColorValue accent) {
        float titleW = hudSmallTextDraw.measureWidth(title);
        float titleH = hudSmallTextDraw.measureHeight(title);
        float actionW = promptTextDraw.measureWidth(action);
        float actionH = promptTextDraw.measureHeight(action);
        float padX  = 4f, padY = 3f, lineGap = 1f;
        float boxW  = Math.max(titleW, actionW) + padX * 2f;
        float boxH  = titleH + actionH + padY * 2f + lineGap;
        float boxX  = centerX - boxW / 2f;

        sr.beginFilled();
        sr.setColor(0.02f, 0.04f, 0.08f, 0.82f);
        sr.rect(boxX, baselineY, boxW, boxH);
        sr.setColor(accent.r, accent.g, accent.b, 0.72f);
        sr.rect(boxX, baselineY + boxH - 1f, boxW, 1f);
        sr.end();

        hudSmallTextDraw.begin();
        hudSmallTextDraw.setColor(accent.r, accent.g, accent.b, 0.96f);
        hudSmallTextDraw.draw(title, centerX - titleW / 2f, baselineY + boxH - padY);
        hudSmallTextDraw.end();

        promptTextDraw.begin();
        promptTextDraw.setColor(0.94f, 0.98f, 1f, 0.90f);
        promptTextDraw.draw(action, centerX - actionW / 2f, baselineY + padY + actionH);
        promptTextDraw.end();
    }

    private void drawSpriteCenteredPreserveAspect(String spriteKey, float cx, float cy,
                                                   float maxSize, float alpha) {
        if (!sprites.has(spriteKey)) return;
        float aspect = sprites.getAspectRatio(spriteKey);
        float drawW  = aspect >= 1f ? maxSize : maxSize * aspect;
        float drawH  = aspect >= 1f ? maxSize / aspect : maxSize;
        spriteDraw.begin();
        spriteDraw.setTint(1f, 1f, 1f, alpha);
        spriteDraw.draw(spriteKey, cx - drawW / 2f, cy - drawH / 2f, drawW, drawH);
        spriteDraw.resetTint();
        spriteDraw.end();
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
}
