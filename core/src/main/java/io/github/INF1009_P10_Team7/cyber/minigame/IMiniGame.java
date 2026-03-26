package io.github.INF1009_P10_Team7.cyber.minigame;

import io.github.INF1009_P10_Team7.engine.inputoutput.IInputController;
import io.github.INF1009_P10_Team7.engine.render.MiniGameRenderContext;

/**
 * IMiniGame, interface for all in-game interactive challenge puzzles.
 *
 * <p>{@code CyberGameScene} only ever talks to this interface, it does not
 * care whether the active challenge is a {@link BinaryDecodeGame},
 * {@link CaesarCipherGame}, {@link PortMatchGame}, or any other type (LSP, OCP).
 * New mini-game types can be added without touching the scene.</p>
 *
 * <p>Lifecycle:</p>
 * <ol>
 * <li>{@link #open()} , called when the player interacts with a terminal</li>
 * <li>{@link #update} , called every frame while the game is open</li>
 * <li>{@link #render} , called every frame to draw the UI</li>
 * <li>{@link #close()} , called when the player exits or the challenge ends</li>
 * </ol>
 */
public interface IMiniGame extends IInputController.ITextInputListener {

    /** @return the display title shown in the terminal header */
    String getTitle();

    /** Opens the mini-game UI and resets any transient state. */
    void open();

    /** Closes the mini-game UI without marking it solved. */
    void close();

    /** @return {@code true} while the mini-game UI is open */
    boolean isOpen();

    /**
     * Updates the mini-game logic for one frame.
     *
     * @param delta seconds since the last frame
     */
    void update(float delta);

    /**
     * Renders the mini-game UI using the provided rendering resources.
     * The caller is responsible for setting projection matrices.
     *
     * @param context engine-provided render context for this frame
     */
    void render(MiniGameRenderContext context);

    /** @return {@code true} if the player has completed this challenge */
    boolean isSolved();

    /**
     * @return {@code true} if the challenge was force-closed due to
     * drone detection (panic exit) rather than a normal exit
     */
    boolean wasPanicked();
}
