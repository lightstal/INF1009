package io.github.INF1009_P10_Team7.cyber.components;

import io.github.INF1009_P10_Team7.engine.entity.IComponent;
import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.cyber.minigame.IMiniGame;

public class TerminalComponent implements IComponent {
    private Entity owner;
    private IMiniGame miniGame;
    private int terminalIndex;
    private boolean solved = false;

    public TerminalComponent(IMiniGame miniGame, int terminalIndex) {
        this.miniGame = miniGame;
        this.terminalIndex = terminalIndex;
    }

    @Override public void onAdded(Entity entity) { this.owner = entity; }
    @Override public void onRemoved(Entity entity) { this.owner = null; }
    @Override public void update(float deltaTime) {}

    public IMiniGame getMiniGame() { return miniGame; }
    public int getTerminalIndex() { return terminalIndex; }
    public boolean isSolved() { return solved; }
    public void setSolved(boolean solved) { this.solved = solved; }
}