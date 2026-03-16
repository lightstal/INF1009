package io.github.INF1009_P10_Team7.simulation.cyber;

/**
 * Defines all tile types used in the CyberMaze map.
 * Room floor types (7–11) allow per-room colour differentiation.
 */
public enum TileType {
    FLOOR(0),
    WALL(1),
    TERMINAL_XSS(2),
    TERMINAL_CSRF(3),
    TERMINAL_INPUT(4),
    EXIT(5),
    TERMINAL_PRIV_ESC(6),
    // Room-specific floor tiles – same walkable semantics as FLOOR, different colour zone
    ROOM_A(7),
    ROOM_B(8),
    ROOM_C(9),
    ROOM_D(10),
    ROOM_E(11);

    public final int id;
    TileType(int id) { this.id = id; }

    public static TileType fromId(int id) {
        for (TileType t : values()) if (t.id == id) return t;
        return FLOOR;
    }

    /** Returns true for tiles that block movement. */
    public boolean isSolid() { return this == WALL; }

    /** Returns true for any hackable terminal tile. */
    public boolean isTerminal() {
        return this == TERMINAL_XSS || this == TERMINAL_CSRF
            || this == TERMINAL_INPUT || this == TERMINAL_PRIV_ESC;
    }

    /** Returns true for any walkable floor tile (corridor or room). */
    public boolean isFloor() {
        return this == FLOOR || this == ROOM_A || this == ROOM_B
            || this == ROOM_C || this == ROOM_D || this == ROOM_E;
    }

    /**
     * Returns a 0-based room index for floor colour lookup.
     * 0 = corridor, 1-5 = room A-E.
     */
    public int roomIndex() {
        switch (this) {
            case ROOM_A: return 1;
            case ROOM_B: return 2;
            case ROOM_C: return 3;
            case ROOM_D: return 4;
            case ROOM_E: return 5;
            default:     return 0;
        }
    }
}
