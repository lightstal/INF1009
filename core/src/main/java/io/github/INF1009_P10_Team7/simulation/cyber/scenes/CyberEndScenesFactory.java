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
 * Demonstrates the Factory Method pattern.
 */
public final class CyberEndScenesFactory {

    private CyberEndScenesFactory() {}

    public static Scene gameOver(IInputController input, IAudioController audio,
                                 SceneNavigator nav, CyberSceneFactory factory, int level) {
        return new CyberGameOverScene(input, audio, nav, factory, level);
    }

    /**
     * Passes all scoring parameters through to CyberVictoryScene.
     */
    public static Scene victory(IInputController input, IAudioController audio,
                                SceneNavigator nav, CyberSceneFactory factory,
                                int keys, int keysRequired, int missionElapsedSeconds, int level,
                                int respawnsUsed, int hintsUsed) {
        return new CyberVictoryScene(input, audio, nav, factory, keys, keysRequired, missionElapsedSeconds, level,
            respawnsUsed, hintsUsed);
    }
}
