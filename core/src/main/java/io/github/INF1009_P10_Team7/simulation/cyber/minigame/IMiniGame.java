package io.github.INF1009_P10_Team7.simulation.cyber.minigame;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Unified interface for all challenge overlays (terminal emulators AND mini-games).
 * CyberGameScene only talks to this interface  -  it doesn't care which type it is.
 */
public interface IMiniGame {
    /** Open / show the challenge. */
    void open();
    /** Force-close without solving. */
    void close();
    /** True while the overlay is visible and consuming input. */
    boolean isOpen();
    /** True once the student has answered correctly. */
    boolean isSolved();
    /** True if the player hit TAB / ESC to flee without solving. */
    boolean wasPanicked();
    /** Called every frame while open. */
    void update(float dt);
    /**
     * Draw the full-screen overlay.
     * Called after world rendering; projection matrices are already set to HUD camera (1280×704).
     */
    void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font);
    /** Short label used in HUD hints near the tile. */
    String getTitle();
}
