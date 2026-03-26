package io.github.INF1009_P10_Team7.cyber;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ClueSystem — tracks collectible intel fragments the player discovers
 * by interacting with objects scattered across the level (data vaults,
 * log screens, keycards, USB devices, etc.).
 *
 * Some clue objects are hidden by default and only become visible when the
 * player is very close or triggers a signal ping. This turns ping into a real
 * scanning mechanic instead of a debug helper.
 */
public class ClueSystem {

    /** A single clue fragment the player has found. */
    public static class Clue {
        public final String id;
        public final String title;
        public final String description;
        public final float  collectTime;

        public Clue(String id, String title, String description, float collectTime) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.collectTime = collectTime;
        }
    }

    /** An interactable clue source placed in the world. */
    public static class ClueObject {
        public final int tileCol, tileRow;
        public final String clueId;
        public final String objectName;  // e.g. "Data Vault", "Server Log", "USB Device"
        public final String clueTitle;
        public final String clueDescription;
        public boolean collected;
        public float revealTimer;

        public ClueObject(int col, int row, String clueId, String objectName,
                         String clueTitle, String clueDescription) {
            this.tileCol = col;
            this.tileRow = row;
            this.clueId = clueId;
            this.objectName = objectName;
            this.clueTitle = clueTitle;
            this.clueDescription = clueDescription;
            this.collected = false;
            this.revealTimer = 0f;
        }

        public boolean isRevealed() {
            return revealTimer > 0f;
        }
    }

    /** Ordered map of clues the player has successfully collected this run. */
    private final Map<String, Clue> collectedClues = new LinkedHashMap<>();
    /** All clue objects placed in the current level (collected or not). */
    private final List<ClueObject> clueObjects = new ArrayList<>();

    /** Register a clue object in the world (call during level setup). */
    public void addClueObject(ClueObject obj) {
        clueObjects.add(obj);
    }

    /** Collect a clue by ID. Returns true if newly collected. */
    public boolean collectClue(String id, String title, String description, float time) {
        if (collectedClues.containsKey(id)) return false;
        collectedClues.put(id, new Clue(id, title, description, time));
        return true;
    }

    /** Check if the player has a specific clue. */
    public boolean hasClue(String id) {
        return collectedClues.containsKey(id);
    }

    /** Get the total number of collected clues. */
    public int getCollectedCount() {
        return collectedClues.size();
    }

    /** Get the total number of clue objects in the level. */
    public int getTotalClueObjects() {
        return clueObjects.size();
    }

    /** Get all clue objects (for rendering). */
    public List<ClueObject> getClueObjects() {
        return clueObjects;
    }

    /** Get collected clues (for HUD display). */
    public Map<String, Clue> getCollectedClues() {
        return collectedClues;
    }

    /** Update temporary reveal timers. */
    public void update(float delta) {
        for (ClueObject obj : clueObjects) {
            if (obj.revealTimer > 0f) {
                obj.revealTimer = Math.max(0f, obj.revealTimer - delta);
            }
        }
    }

    /** Reveal any hidden clue objects inside the signal ping radius. */
    public int revealObjectsWithinRadius(float worldX, float worldY, float radius, float duration) {
        int revealed = 0;
        float radiusSq = radius * radius;
        for (ClueObject obj : clueObjects) {
            if (obj.collected) continue;
            float cx = TileMap.tileCentreX(obj.tileCol);
            float cy = TileMap.tileCentreY(obj.tileRow);
            float dx = cx - worldX;
            float dy = cy - worldY;
            if (dx * dx + dy * dy <= radiusSq) {
                if (obj.revealTimer <= 0f) revealed++;
                obj.revealTimer = Math.max(obj.revealTimer, duration);
            }
        }
        return revealed;
    }

    /** Hidden objects can still be stumbled upon when the player gets very close. */
    public boolean isVisible(ClueObject obj, float playerWorldX, float playerWorldY, float proximityRadius) {
        if (obj == null || obj.collected) return false;
        if (obj.isRevealed()) return true;
        float cx = TileMap.tileCentreX(obj.tileCol);
        float cy = TileMap.tileCentreY(obj.tileRow);
        float dx = cx - playerWorldX;
        float dy = cy - playerWorldY;
        return dx * dx + dy * dy <= proximityRadius * proximityRadius;
    }

    /** Check if a terminal (by index) can be accessed. */
    public boolean canAccessTerminal(int terminalIdx, int totalTerminals) {
        if (terminalIdx < 2) return true;
        int requiredClues = Math.min(terminalIdx - 1, clueObjects.size());
        return collectedClues.size() >= requiredClues;
    }

    /** Get a hint about what's needed to access a locked terminal. */
    public String getTerminalLockHint(int terminalIdx) {
        int requiredClues = Math.min(terminalIdx - 1, clueObjects.size());
        int missing = requiredClues - collectedClues.size();
        if (missing <= 0) return null;
        return "LOCKED — Collect " + missing + " more intel fragment" + (missing > 1 ? "s" : "") + " first";
    }

    /** Reset for level restart. */
    public void reset() {
        collectedClues.clear();
        clueObjects.clear();
    }
}
