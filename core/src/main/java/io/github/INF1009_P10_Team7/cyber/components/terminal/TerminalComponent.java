package io.github.INF1009_P10_Team7.cyber.components.terminal;

import io.github.INF1009_P10_Team7.engine.entity.Entity;
import io.github.INF1009_P10_Team7.engine.entity.IComponent;

/**
 * Component tagging an entity as a world terminal node.
 */
public class TerminalComponent implements IComponent {

    private final int terminalIndex;
    private final int tileCol;
    private final int tileRow;
    private boolean solved;

    public TerminalComponent(int terminalIndex, int tileCol, int tileRow) {
        this.terminalIndex = terminalIndex;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.solved = false;
    }

    public int getTerminalIndex() { return terminalIndex; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public boolean isSolved() { return solved; }
    public void setSolved(boolean solved) { this.solved = solved; }

    @Override public void onAdded(Entity owner) { }
    @Override public void onRemoved(Entity owner) { }
    @Override public void update(float deltaTime) { }
}
