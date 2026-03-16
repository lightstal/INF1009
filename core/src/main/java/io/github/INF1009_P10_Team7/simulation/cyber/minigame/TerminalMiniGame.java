package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.INF1009_P10_Team7.simulation.cyber.ctf.ICTFChallenge;
import io.github.INF1009_P10_Team7.simulation.cyber.ctf.TerminalEmulator;

/**
 * Adapter that wraps the classic ICTFChallenge + TerminalEmulator pair
 * into the IMiniGame interface so CyberGameScene can treat all challenges uniformly.
 */
public class TerminalMiniGame implements IMiniGame {

    private final ICTFChallenge  challenge;
    private final TerminalEmulator emulator;

    public TerminalMiniGame(ICTFChallenge challenge, TerminalEmulator emulator) {
        this.challenge = challenge;
        this.emulator  = emulator;
    }

    @Override public void open()  { challenge.reset(); emulator.open(challenge); }
    @Override public void close() { emulator.close(); }
    @Override public boolean isOpen()      { return emulator.isOpen();      }
    @Override public boolean isSolved()    { return emulator.isSolved();    }
    @Override public boolean wasPanicked() { return emulator.wasPanicked(); }
    @Override public void update(float dt) { emulator.update(dt);           }
    @Override public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        emulator.render(sr, batch, font);
    }
    @Override public String getTitle() { return challenge.getTitle(); }
}
