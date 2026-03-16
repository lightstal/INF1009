package io.github.INF1009_P10_Team7.simulation.cyber.scenes;

import io.github.INF1009_P10_Team7.engine.inputoutput.IAudioController;
import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.scene.Scene;
import io.github.INF1009_P10_Team7.engine.scene.SceneNavigator;
import io.github.INF1009_P10_Team7.simulation.cyber.CyberSceneFactory;

/**
 * Public static factory for end-game scenes.
 * Keeps CyberGameOverScene and CyberVictoryScene package-private while
 * still allowing CyberSceneFactory (in a different package) to create them.
 * Demonstrates the Factory Method pattern supplementing the Strategy/Observer patterns.
 */
public final class CyberEndScenesFactory {

    private CyberEndScenesFactory() {}

    public static Scene gameOver(IInputController input, IAudioController audio,
                                 SceneNavigator nav, CyberSceneFactory factory) {
        return new CyberGameOverScene(input, audio, nav, factory);
    }

    public static Scene victory(IInputController input, IAudioController audio,
                                SceneNavigator nav, CyberSceneFactory factory,
                                int keys, int timeLeft, int level) {
        return new CyberVictoryScene(input, audio, nav, factory, keys, timeLeft, level);
    }
}
