package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.render.LevelSelectRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * Level select screen — delegates rendering to {@link LevelSelectRenderer}
 * so this cyber scene contains no LibGDX imports/calls.
 */
public class LevelSelectScene extends Scene {

    private static final int NUM_LEVELS = 2;

    private int selected = 0;
    private float stateTime = 0f;
    private float selectAnim = 0f;

    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator nav;
    private final CyberSceneFactory factory;

    private LevelSelectRenderer renderer;

    public LevelSelectScene(IInputController input, IAudioController audio,
                             SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.input = input;
        this.audio = audio;
        this.nav = nav;
        this.factory = factory;
    }

    @Override
    protected void onLoad() {
        renderer = new LevelSelectRenderer();
        renderer.load();
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;
        if (selectAnim > 0) selectAnim -= dt * 4f;

        if (input.isActionJustPressed("MENU_BACK")) {
            nav.setScene(factory.createMainMenuScene());
            return;
        }

        if (input.isActionJustPressed("LEFT") || input.isActionJustPressed("MENU_LEFT")) {
            selected = (selected + NUM_LEVELS - 1) % NUM_LEVELS;
            selectAnim = 1f;
        }
        if (input.isActionJustPressed("RIGHT") || input.isActionJustPressed("MENU_RIGHT")) {
            selected = (selected + 1) % NUM_LEVELS;
            selectAnim = 1f;
        }

        if (input.isActionJustPressed("START_GAME") || input.isActionJustPressed("MENU_CONFIRM")) {
            nav.setScene(factory.createCutsceneScene(selected + 1));
        }
    }

    @Override
    protected void onRender() {
        if (renderer != null) renderer.render(stateTime, selected);
    }

    @Override
    public void resize(int w, int h) {
        if (renderer != null) renderer.resize(w, h);
    }

    @Override
    protected void onDispose() {
        if (renderer != null) renderer.dispose();
    }

    @Override
    public void onUnload() { }
}

