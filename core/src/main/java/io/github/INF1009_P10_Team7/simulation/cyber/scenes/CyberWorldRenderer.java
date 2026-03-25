package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.INF1009_P10_Team7.engine.entity.GameEntity;
import io.github.INF1009_P10_Team7.engine.entity.components.TransformComponent;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.utils.Vector2;
import io.github.INF1009_P10_Team7.simulation.cyber.ClueSystem;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSprites;
import io.github.INF1009_P10_Team7.simulation.cyber.TileMap;
import io.github.INF1009_P10_Team7.simulation.cyber.TiledObjectCollisionManager;
import io.github.INF1009_P10_Team7.simulation.cyber.drone.DroneAI;

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

    private final ShapeRenderer              sr;
    private final SpriteBatch                batch;
    private final CyberSprites               sprites;
    private final IInputController           input;
    private final BitmapFont                 hudSmallFont;
    private final BitmapFont                 promptFont;
    private final GlyphLayout                layout;

    public CyberWorldRenderer(ShapeRenderer sr, SpriteBatch batch,
                               CyberSprites sprites, IInputController input,
                               BitmapFont hudSmallFont, BitmapFont promptFont,
                               GlyphLayout layout) {
        this.sr          = sr;
        this.batch       = batch;
        this.sprites     = sprites;
        this.input       = input;
        this.hudSmallFont = hudSmallFont;
        this.promptFont  = promptFont;
        this.layout      = layout;
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
                                 TiledObjectCollisionManager collisionMgr) {
        float ts = TileMap.TILE_SIZE;
        renderSecurityCameras(ts, stateTime, camPositions, cctvAlerted, playerEntity, collisionMgr);
        renderDroneSprites(ts, stateTime, drones);
        renderTerminalWifiBadge(ts);
    }

    public void renderTerminalGlow(int[][] terminalTiles, boolean[] terminalSolved) {
        float ts = TileMap.TILE_SIZE;
        if (sprites.get("terminal") != null) {
            batch.begin();
            for (int i = 0; i < terminalTiles.length; i++) {
                if (terminalSolved[i]) continue;
                float tx = TileMap.tileLeft(terminalTiles[i][0]) + ts * 0.5f;
                float ty = TileMap.tileBottom(terminalTiles[i][1]) + ts * 0.5f;
                sprites.drawCentered(batch, "terminal", tx, ty, ts * 0.85f, 1f);
            }
            batch.end();
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
            Color accent = getClueAccent(clue.objectName);

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
                            "LOCKED", status, new Color(1f, 0.34f, 0.24f, 1f));
                    } else {
                        drawWorldPromptCard(tx, ty + ts * 0.52f,
                            "TERMINAL", buildPrompt("INTERACT", "JACK IN"),
                            new Color(0.10f, 0.90f, 0.55f, 1f));
                    }
                    break;
                }
            }
        }
    }

    public void renderSignalPingEffect() {
        // Ping ring removed — feedback handled by banner + revealed intel objects
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

        sr.begin(ShapeRenderer.ShapeType.Line);
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

        sr.begin(ShapeRenderer.ShapeType.Filled);
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
            Color accent = getClueAccent(clue.objectName);
            sr.setColor(accent.r, accent.g, accent.b, 0.34f + pulse * 0.16f);
            sr.circle(cx, cy, s, 16);
        }
        sr.end();
    }

    public void renderCheckpointBeacon(float stateTime, float checkpointX, float checkpointY) {
        float pulse = 0.45f + 0.25f * (float)Math.sin(stateTime * 3f);
        float s = 2.8f + pulse;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.85f, 1f, 0.14f * pulse);
        sr.triangle(checkpointX, checkpointY + s, checkpointX - s, checkpointY, checkpointX + s, checkpointY);
        sr.triangle(checkpointX, checkpointY - s, checkpointX - s, checkpointY, checkpointX + s, checkpointY);
        sr.end();
    }

    /**
     * Renders the exit door sprite (closed or open) at its world position.
     * Switches to the open texture once {@code exitUnlocked} is {@code true}.
     */
    public void renderTmxExitDoor(float tmxExitX, float tmxExitY,
                                   boolean exitUnlocked,
                                   TextureRegion doorClosedRegion,
                                   TextureRegion doorOpenedRegion) {
        TextureRegion region = exitUnlocked ? doorOpenedRegion : doorClosedRegion;
        if (region == null) return;
        float ts = TileMap.TILE_SIZE;
        batch.begin();
        batch.draw(region, tmxExitX - ts / 2f, tmxExitY - ts / 2f, ts, ts);
        batch.end();
    }

    public void renderExitGuidance(float stateTime, boolean exitUnlocked,
                                    GameEntity playerEntity,
                                    float tmxExitX, float tmxExitY) {
        if (!exitUnlocked) return;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        if (tc == null) return;

        float pulse = 0.35f + 0.25f * (float)Math.sin(stateTime * 5.5f);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.85f, 0.15f, 1f, 0.15f + pulse * 0.15f);
        sr.line(tc.getPosition().x, tc.getPosition().y, tmxExitX, tmxExitY);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        float s = 4.0f + pulse * 2f;
        sr.setColor(0.8f, 0.1f, 1f, 0.28f * pulse);
        sr.triangle(tmxExitX, tmxExitY + s, tmxExitX - s, tmxExitY, tmxExitX + s, tmxExitY);
        sr.triangle(tmxExitX, tmxExitY - s, tmxExitX - s, tmxExitY, tmxExitX + s, tmxExitY);
        sr.end();
    }

    /**
     * Renders an ambient atmospheric overlay (scanlines, vignette, etc.)
     * on top of the entire world. Called last in the render pass.
     */
    public void renderAtmosphere() {
        // Intentionally empty — no dark overlay, no flashlight, no light pools
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void renderCeilingLights(float ts, float stateTime, int[][] lights) {
        if (sprites.get("ceilingLight") == null) return;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < lights.length; i++) {
            int[] lt = lights[i];
            float cx = TileMap.tileCentreX(lt[0]);
            float cy = TileMap.tileCentreY(lt[1]);
            float flicker = 0.80f + 0.20f * (float)Math.sin(stateTime * 4.0f + i * 1.7f);
            sr.setColor(1.00f, 0.94f, 0.40f, 0.10f * flicker);
            sr.circle(cx, cy - ts * 0.10f, ts * 0.70f, 24);
            sr.setColor(1.00f, 0.88f, 0.22f, 0.06f * flicker);
            sr.circle(cx, cy - ts * 0.10f, ts * 1.02f, 28);
        }
        sr.end();
        batch.begin();
        for (int[] lt : lights) {
            float cx = TileMap.tileCentreX(lt[0]);
            float cy = TileMap.tileCentreY(lt[1]);
            sprites.drawCentered(batch, "ceilingLight", cx, cy + ts * 0.32f, ts * 0.64f, 0.92f);
        }
        batch.end();
    }

    private void renderSecurityCameras(float ts, float stateTime, int[][] camPositions,
                                        boolean[] cctvAlerted, GameEntity playerEntity,
                                        TiledObjectCollisionManager collisionMgr) {
        if (sprites.get("secCamera") == null) return;
        TransformComponent tc = playerEntity != null
            ? playerEntity.getComponent(TransformComponent.class) : null;
        Vector2 pp = tc != null ? tc.getPosition() : null;

        sr.begin(ShapeRenderer.ShapeType.Filled);
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

            Color coneColor;
            if (visionState >= 2)     coneColor = new Color(1.00f, 0.18f, 0.15f, 0.11f * pulse);
            else if (visionState == 1) coneColor = new Color(1.00f, 0.58f, 0.16f, 0.09f * pulse);
            else                       coneColor = new Color(1.00f, 0.92f, 0.20f, 0.06f * pulse);
            drawSoftCone(sr, cx, cy, totalAng, 58f, ts * 2.4f, coneColor, 18);
        }
        sr.end();

        batch.begin();
        for (int i = 0; i < camPositions.length; i++) {
            float cx = TileMap.tileCentreX(camPositions[i][0]);
            float cy = TileMap.tileCentreY(camPositions[i][1]);
            float phase   = i * 1.3f;
            float panAng  = (float)Math.sin(stateTime * 0.7f + phase) * 40f;
            float totalAng = camPositions[i][2] + panAng;
            boolean detected = cctvAlerted != null && i < cctvAlerted.length && cctvAlerted[i];
            if (detected) batch.setColor(1f, 0.82f, 0.82f, 0.98f);
            else          batch.setColor(1f, 1f, 1f, 0.92f);
            sprites.drawCenteredRotated(batch, "secCamera", cx, cy, ts * 0.72f, totalAng - 90f, 0.92f);
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void renderDroneSprites(float ts, float stateTime, DroneAI[] drones) {
        if (drones == null || drones.length == 0) return;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (DroneAI drone : drones) {
            float dx = drone.getPosition().x, dy = drone.getPosition().y;
            Color coneColor;
            if (drone.isDetectionSuppressed())            coneColor = new Color(0.45f, 0.78f, 1.00f, 0.035f);
            else if ("CHASE".equals(drone.getStateName())) coneColor = new Color(1.00f, 0.18f, 0.16f, 0.12f);
            else if ("SEARCH".equals(drone.getStateName()) || drone.getAlertLevel() > 0.38f)
                                                           coneColor = new Color(1.00f, 0.58f, 0.14f, 0.095f);
            else                                           coneColor = new Color(1.00f, 0.92f, 0.20f, 0.065f);
            drawSoftCone(sr, dx, dy, drone.getFacingAngle(), drone.getSightAngle(),
                drone.getSightRange() * 0.78f, coneColor, 18);
            sr.setColor(0f, 0f, 0f, 0.10f);
            sr.rect(dx - ts * 0.22f, dy - ts * 0.06f, ts * 0.44f, ts * 0.12f);
        }
        sr.end();

        batch.begin();
        for (DroneAI drone : drones) {
            String spriteKey = drone.isDetectionSuppressed() ? "droneDamagedRed" : "dronePatrolRed";
            if ("CHASE".equals(drone.getStateName()) || "SEARCH".equals(drone.getStateName()))
                spriteKey = "droneDetectRed";

            Texture tex = sprites.get(spriteKey);
            if (tex == null) continue;

            float dx = drone.getPosition().x, dy = drone.getPosition().y;
            float aspect = tex.getWidth() / (float)Math.max(1, tex.getHeight());
            float drawW  = ts * 1.30f;
            float drawH  = drawW / aspect;
            if (drawH > ts * 0.95f) { float sc = (ts * 0.95f) / drawH; drawW *= sc; drawH *= sc; }

            boolean chasing   = "CHASE".equals(drone.getStateName());
            boolean searching = "SEARCH".equals(drone.getStateName());
            if (chasing)                        batch.setColor(1f, 0.85f, 0.85f, 1f);
            else if (searching)                 batch.setColor(1f, 0.92f, 0.78f, 0.98f);
            else if (drone.isDetectionSuppressed()) batch.setColor(0.84f, 0.92f, 1f, 0.92f);
            else                                batch.setColor(1f, 1f, 1f, 0.96f);

            batch.draw(tex, dx - drawW / 2f, dy - drawH / 2f,
                drawW / 2f, drawH / 2f, drawW, drawH, 1f, 1f,
                drone.getFacingAngle() - 90f, 0, 0,
                tex.getWidth(), tex.getHeight(), false, false);
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    private void renderTerminalWifiBadge(float ts) {
        // Intentionally empty — world-prompt card handles proximity UI
    }

    private void drawSoftCone(ShapeRenderer renderer, float ox, float oy, float facingDeg,
                               float coneAngleDeg, float length, Color color, int segments) {
        renderer.setColor(color);
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

    private Color getClueAccent(String objectName) {
        String n = objectName == null ? "INTEL" : objectName.trim().toUpperCase();
        if (n.contains("KEY")) return new Color(1f, 0.83f, 0.28f, 1f);
        if (n.contains("USB")) return new Color(0.22f, 0.95f, 0.72f, 1f);
        return new Color(0.24f, 0.82f, 1f, 1f);
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
                                      String title, String action, Color accent) {
        GlyphLayout titleLayout  = new GlyphLayout(hudSmallFont, title);
        GlyphLayout actionLayout = new GlyphLayout(promptFont, action);
        float padX  = 4f, padY = 3f, lineGap = 1f;
        float boxW  = Math.max(titleLayout.width, actionLayout.width) + padX * 2f;
        float boxH  = titleLayout.height + actionLayout.height + padY * 2f + lineGap;
        float boxX  = centerX - boxW / 2f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.02f, 0.04f, 0.08f, 0.82f);
        sr.rect(boxX, baselineY, boxW, boxH);
        sr.setColor(accent.r, accent.g, accent.b, 0.72f);
        sr.rect(boxX, baselineY + boxH - 1f, boxW, 1f);
        sr.end();

        batch.begin();
        hudSmallFont.setColor(accent.r, accent.g, accent.b, 0.96f);
        hudSmallFont.draw(batch, title, centerX - titleLayout.width / 2f, baselineY + boxH - padY);
        promptFont.setColor(0.94f, 0.98f, 1f, 0.90f);
        promptFont.draw(batch, action, centerX - actionLayout.width / 2f,
            baselineY + padY + actionLayout.height);
        batch.end();
    }

    private void drawSpriteCenteredPreserveAspect(String spriteKey, float cx, float cy,
                                                   float maxSize, float alpha) {
        Texture tex = sprites.get(spriteKey);
        if (tex == null) return;
        float aspect = tex.getWidth() / (float)Math.max(1, tex.getHeight());
        float drawW  = aspect >= 1f ? maxSize : maxSize * aspect;
        float drawH  = aspect >= 1f ? maxSize / aspect : maxSize;
        batch.begin();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(tex, cx - drawW / 2f, cy - drawH / 2f, drawW, drawH);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
}
