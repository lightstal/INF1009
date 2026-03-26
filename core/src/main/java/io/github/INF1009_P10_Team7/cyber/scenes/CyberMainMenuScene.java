package io.github.INF1009_P10_Team7.cyber.scenes;

import io.github.INF1009_P10_Team7.cyber.CyberSceneFactory;
import io.github.INF1009_P10_Team7.cyber.level.TileMap;
import io.github.INF1009_P10_Team7.cyber.render.CyberMainMenuRenderer;
import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;

/**
 * CyberMainMenuScene, main menu screen delegating all LibGDX rendering
 * to {@link CyberMainMenuRenderer} so this class has no LibGDX imports.
 */
public class CyberMainMenuScene extends Scene {

    private final CyberSceneFactory factory;

    private float stateTime = 0f;

    private final IInputController input;
    private final IAudioController audio;
    private final SceneNavigator nav;

    private CyberMainMenuRenderer renderer;

    public CyberMainMenuScene(IInputController input, IAudioController audio,
                              SceneNavigator nav, CyberSceneFactory factory) {
        super(input, audio, nav);
        this.factory = factory;
        this.input = input;
        this.audio = audio;
        this.nav = nav;
    }

    @Override
    protected void onLoad() {
        renderer = new CyberMainMenuRenderer(TileMap.WORLD_W, TileMap.WORLD_H);
        renderer.load();
        audio.setMusic("audio/Music_Menu.mp3");
    }

    @Override
    protected void onUpdate(float dt) {
        stateTime += dt;

        renderer.updateRain(dt);
        renderer.updateHover(input.getMouseX(), input.getMouseY());

        // Keyboard shortcuts
        if (input.isActionJustPressed("START_GAME")) {
            nav.requestScene(factory.createLevelSelectScene());
            return;
        }
        if (input.isActionJustPressed("MENU_BACK")) {
            System.exit(0);
            return;
        }

        // Mouse click, hover decides start vs exit
        if (input.isActionJustPressed("MENU_CLICK")) {
            if (renderer.isStartHover()) {
                nav.requestScene(factory.createLevelSelectScene());
            } else if (renderer.isExitHover()) {
                System.exit(0);
            }
        }
    }

    @Override
    protected void onRender() {
        renderer.render(stateTime);
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
}

