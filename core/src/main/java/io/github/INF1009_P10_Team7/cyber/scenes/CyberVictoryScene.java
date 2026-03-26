package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.render.CyberVictoryRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * Victory screen: rendering delegated to {@link CyberVictoryRenderer}.
 */
class CyberVictoryScene extends Scene {

    private final CyberSceneFactory factory;
    private final int keysCollected;
    private final int keysRequired;
    private final int missionTimeSeconds;
    private final int level;
    private final int respawnsUsed;
    private final int hintsUsed;

    private final CyberVictoryRenderer renderer;
    private float stateTime = 0f;

    CyberVictoryScene(IInputController input, IAudioController audio,
                        SceneNavigator nav, CyberSceneFactory factory,
                        int keysCollected, int keysRequired, int missionElapsedSeconds, int level,
                        int respawnsUsed, int hintsUsed) {
        super(input, audio, nav);
        this.factory = factory;
        this.keysCollected = keysCollected;
        this.keysRequired = keysRequired;
        this.missionTimeSeconds = missionElapsedSeconds;
        this.level = level;
        this.respawnsUsed = respawnsUsed;
        this.hintsUsed = hintsUsed;

        this.renderer = new CyberVictoryRenderer(TileMap.WORLD_W, TileMap.WORLD_H);
    }

    @Override
    protected void onLoad() {
        renderer.load();
        audio.stopMusic();
        audio.playSound("audio/bell.mp3");
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createLevelSelectScene());
        }
        if (input.isActionJustPressed("MENU_BACK")) {
            nav.requestScene(factory.createMainMenuScene());
        }
    }

    private int getScore() {
        int timeBonus = Math.max(0, 2400 - missionTimeSeconds * 8);
        int score = keysCollected * 1000 + timeBonus + level * 150;
        score -= respawnsUsed * 300;
        score -= hintsUsed * 180;
        return Math.max(0, score);
    }

    private String getRank() {
        int score = getScore();
        if (score >= 5600) return "S";
        if (score >= 4700) return "A";
        if (score >= 3800) return "B";
        if (score >= 3000) return "C";
        return "D";
    }

    @Override
    protected void onRender() {
        String rank = getRank();
        renderer.render(stateTime,
            keysCollected, keysRequired,
            missionTimeSeconds, level,
            respawnsUsed, hintsUsed,
            getScore(), rank);
    }

    @Override
    public void resize(int w, int h) {
        renderer.resize(w, h);
    }

    @Override
    protected void onUnload() { }

    @Override
    protected void onDispose() {
        renderer.dispose();
    }
}

