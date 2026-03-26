package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory; // used in static create()
import io.github.INF1009_P10_Team7.cyber.render.LevelCutsceneRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * LevelCutsceneScene, pre-level cinematic scene shown before gameplay.
 *
 * <p>This is a delegator: all rendering is done by {@link LevelCutsceneRenderer}
 * so this class remains focused on the typing/phase state machine.</p>
 */
public class LevelCutsceneScene extends Scene {

    // Timing
    private static final float FADE_IN_DUR = 0.55f;
    private static final float CHAR_DELAY = 0.032f;   // seconds per character
    private static final float LINE_PAUSE = 0.28f;    // extra pause between lines
    private static final float HOLD_DUR = 1.6f;        // pause after all text done
    private static final float FADE_OUT_DUR = 0.55f;

    // Per-level briefing data
    private static final Object[][][] LEVELS = {
        { // Level 1, INITIATION
            { 0.20f, 0.45f, 1.00f },
            { "MISSION 01", "INITIATION — STAR FORMATION" },
            {
                "Intel confirmed. Five isolated research labs",
                "are holding the access tokens you need.",
                "",
                "Breach every terminal. Collect all 5 keys.",
                "Then reach the extraction point.",
                "",
                "No drones on site tonight. Stay sharp.",
            },
            { "BREACH ALL TERMINALS.",  "GET THE KEY.  ESCAPE." }
        },
        { // Level 2, INFILTRATION
            { 0.00f, 0.80f, 0.55f },
            { "MISSION 02", "INFILTRATION — Z-SHAPE COMPLEX" },
            {
                "Two wings. One bridge. Enemy drones patrol",
                "the corridor in sweeping arcs.",
                "",
                "Five terminals scattered across both wings.",
                "Breach them all — the key spawns at the exit.",
                "",
                "One drone tag and the alarm goes live. Move fast.",
            },
            { "BREACH ALL TERMINALS.",  "GET THE KEY.  ESCAPE." }
        }
    };

    // State
    private enum Phase { FADE_IN, TYPING, HOLD, FADE_OUT }
    private Phase phase = Phase.FADE_IN;

    private final int level;
    private float phaseTimer = 0f;
    private float charTimer = 0f;
    private int lineIdx = 0;
    private int charIdx = 0;
    private boolean typingDone = false;
    private float overlayAlpha = 1f;  // 1 = black, 0 = clear

    // Pre-created game scene, set by static factory method
    private io.github.INF1009_P10_Team7.engine.scene.Scene gameSceneRef;

    private float[] accent;
    private String[] titles;
    private String[] bodyLines;
    private String[] kicker;

    private LevelCutsceneRenderer renderer;

    public LevelCutsceneScene(IInputController input, IAudioController audio,
                               SceneNavigator nav, int level) {
        super(input, audio, nav);
        this.level = level;
    }

    @Override
    protected void onLoad() {
        int idx = Math.min(level - 1, LEVELS.length - 1);
        Object[] accentObj = LEVELS[idx][0];
        accent = new float[]{ (Float) accentObj[0], (Float) accentObj[1], (Float) accentObj[2] };
        titles = toStringArray(LEVELS[idx][1]);
        bodyLines = toStringArray(LEVELS[idx][2]);
        kicker = toStringArray(LEVELS[idx][3]);

        renderer = new LevelCutsceneRenderer(accent, titles, bodyLines, kicker);
        renderer.load();
    }

    private String[] toStringArray(Object[] src) {
        String[] out = new String[src.length];
        for (int i = 0; i < src.length; i++) out[i] = (String) src[i];
        return out;
    }

    @Override
    protected void onUpdate(float dt) {
        phaseTimer += dt;

        // Skip on SPACE / ENTER / click.
        // MENU_CLICK is bound globally; we only listen to it in this scene.
        boolean skip = input.isActionJustPressed("START_GAME")
            || input.isActionJustPressed("MENU_CONFIRM")
            || input.isActionJustPressed("MENU_CLICK");

        switch (phase) {
            case FADE_IN:
                overlayAlpha = 1f - (phaseTimer / FADE_IN_DUR);
                if (phaseTimer >= FADE_IN_DUR || skip) {
                    overlayAlpha = 0f;
                    phase = Phase.TYPING;
                    phaseTimer = 0f;
                    if (skip) skipToFadeOut();
                }
                break;

            case TYPING:
                if (!typingDone) {
                    charTimer += dt;
                    float advance = charTimer / CHAR_DELAY;
                    charTimer -= (int) advance * CHAR_DELAY;
                    for (int a = 0; a < (int) advance && !typingDone; a++) {
                        advanceChar();
                    }
                }
                if (typingDone) {
                    phase = Phase.HOLD;
                    phaseTimer = 0f;
                }
                if (skip) skipToFadeOut();
                break;

            case HOLD:
                if (phaseTimer >= HOLD_DUR || skip) {
                    phase = Phase.FADE_OUT;
                    phaseTimer = 0f;
                    overlayAlpha = 0f;
                }
                break;

            case FADE_OUT:
                overlayAlpha = phaseTimer / FADE_OUT_DUR;
                if (phaseTimer >= FADE_OUT_DUR) {
                    overlayAlpha = 1f;
                    launchGame();
                }
                break;
        }
    }

    private void advanceChar() {
        if (lineIdx >= bodyLines.length) { typingDone = true; return; }

        String line = bodyLines[lineIdx];
        if (charIdx < line.length()) {
            charIdx++;
        } else {
            // Line done, pause then move to next
            charTimer -= LINE_PAUSE;
            lineIdx++;
            charIdx = 0;
        }
    }

    private void skipToFadeOut() {
        typingDone = true;
        lineIdx = bodyLines.length;
        phase = Phase.FADE_OUT;
        phaseTimer = 0f;
        overlayAlpha = 0f;
    }

    private void launchGame() {
        nav.requestScene(gameSceneRef);
    }

    @Override
    protected void onRender() {
        if (renderer == null) return;

        boolean typingPhase = phase == Phase.TYPING;
        boolean holdPhase = phase == Phase.HOLD;
        boolean fadeOutPhase = phase == Phase.FADE_OUT;

        renderer.render(
            overlayAlpha,
            typingPhase,
            holdPhase,
            fadeOutPhase,
            typingDone,
            phaseTimer,
            lineIdx,
            charIdx
        );
    }

    @Override
    public void resize(int w, int h) {
        if (renderer != null) renderer.resize(w, h);
    }

    @Override
    protected void onUnload() { }

    @Override
    protected void onDispose() {
        if (renderer != null) renderer.dispose();
    }

    // Static factory method (sets up gameSceneRef cleanly)
    public static LevelCutsceneScene create(IInputController input, IAudioController audio,
                                             SceneNavigator nav, CyberSceneFactory factory,
                                             int level) {
        LevelCutsceneScene scene = new LevelCutsceneScene(input, audio, nav, level);
        // Pre-create the game scene so it's ready for instant transition
        scene.gameSceneRef = factory.createGameScene(level);
        return scene;
    }
}

