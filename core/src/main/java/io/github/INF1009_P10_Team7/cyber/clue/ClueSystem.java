package io.github.INF1009_P10_Team7.cyber.clue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.INF1009_P10_Team7.cyber.level.TileMap;

/**
 * ClueSystem — tracks collectible intel fragments the player discovers
 * by interacting with objects scattered across the level.
 */
public class ClueSystem {

    public static class Clue {
        public final String id;
        public final String title;
        public final String description;
        public final float collectTime;
        public Clue(String id, String title, String description, float collectTime) {
            this.id = id; this.title = title; this.description = description; this.collectTime = collectTime;
        }
    }

    public static class ClueObject {
        public final int tileCol, tileRow;
        public final String clueId;
        public final String objectName;
        public final String clueTitle;
        public final String clueDescription;
        public boolean collected;
        public float revealTimer;
        public ClueObject(int col, int row, String clueId, String objectName,
                          String clueTitle, String clueDescription) {
            this.tileCol = col; this.tileRow = row; this.clueId = clueId; this.objectName = objectName;
            this.clueTitle = clueTitle; this.clueDescription = clueDescription;
            this.collected = false; this.revealTimer = 0f;
        }
        public boolean isRevealed() { return revealTimer > 0f; }
    }

    private final Map<String, Clue> collectedClues = new LinkedHashMap<>();
    private final List<ClueObject> clueObjects = new ArrayList<>();

    public void addClueObject(ClueObject obj) { clueObjects.add(obj); }
    public boolean collectClue(String id, String title, String description, float time) {
        if (collectedClues.containsKey(id)) return false;
        collectedClues.put(id, new Clue(id, title, description, time));
        return true;
    }
    public boolean hasClue(String id) { return collectedClues.containsKey(id); }
    public int getCollectedCount() { return collectedClues.size(); }
    public int getTotalClueObjects() { return clueObjects.size(); }
    public List<ClueObject> getClueObjects() { return clueObjects; }
    public Map<String, Clue> getCollectedClues() { return collectedClues; }

    public void update(float delta) {
        for (ClueObject obj : clueObjects) if (obj.revealTimer > 0f) obj.revealTimer = Math.max(0f, obj.revealTimer - delta);
    }

    public int revealObjectsWithinRadius(float worldX, float worldY, float radius, float duration) {
        int revealed = 0; float radiusSq = radius * radius;
        for (ClueObject obj : clueObjects) {
            if (obj.collected) continue;
            float cx = TileMap.tileCentreX(obj.tileCol), cy = TileMap.tileCentreY(obj.tileRow);
            float dx = cx - worldX, dy = cy - worldY;
            if (dx * dx + dy * dy <= radiusSq) { if (obj.revealTimer <= 0f) revealed++; obj.revealTimer = Math.max(obj.revealTimer, duration); }
        }
        return revealed;
    }

    public boolean isVisible(ClueObject obj, float playerWorldX, float playerWorldY, float proximityRadius) {
        if (obj == null || obj.collected) return false;
        if (obj.isRevealed()) return true;
        float cx = TileMap.tileCentreX(obj.tileCol), cy = TileMap.tileCentreY(obj.tileRow);
        float dx = cx - playerWorldX, dy = cy - playerWorldY;
        return dx * dx + dy * dy <= proximityRadius * proximityRadius;
    }

    public boolean canAccessTerminal(int terminalIdx, int totalTerminals) {
        if (terminalIdx < 2) return true;
        int requiredClues = Math.min(terminalIdx - 1, clueObjects.size());
        return collectedClues.size() >= requiredClues;
    }

    public String getTerminalLockHint(int terminalIdx) {
        int requiredClues = Math.min(terminalIdx - 1, clueObjects.size());
        int missing = requiredClues - collectedClues.size();
        if (missing <= 0) return null;
        return "LOCKED — Collect " + missing + " more intel fragment" + (missing > 1 ? "s" : "") + " first";
    }

    public void reset() { collectedClues.clear(); clueObjects.clear(); }
}
