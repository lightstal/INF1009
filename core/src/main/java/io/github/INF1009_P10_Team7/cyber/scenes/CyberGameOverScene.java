package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.render.CyberGameOverRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * Game-over screen: rendering delegated to {@link CyberGameOverRenderer}.
 */
class CyberGameOverScene extends Scene {
    private final CyberSceneFactory factory;
    private final int level;

    private final CyberGameOverRenderer renderer;
    private float stateTime = 0f;

    CyberGameOverScene(IInputController input, IAudioController audio,
                         SceneNavigator nav, CyberSceneFactory factory, int level) {
        super(input, audio, nav);
        this.factory = factory;
        this.level = level;
        this.renderer = new CyberGameOverRenderer(TileMap.WORLD_W, TileMap.WORLD_H);
    }

    @Override
    protected void onLoad() {
        renderer.load();
        audio.stopMusic();
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("INTERACT")) {
            nav.requestScene(factory.createGameScene(level));
        }
        if (input.isActionJustPressed("MENU_BACK")) {
            nav.requestScene(factory.createLevelSelectScene());
        }
    }

    @Override
    protected void onRender() {
        renderer.render(stateTime, level);
    }

    @Override
    public void resize(int w, int h) { renderer.resize(w, h); }

    @Override
    protected void onUnload() { }

    @Override
    protected void onDispose() { renderer.dispose(); }
}

