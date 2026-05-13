package com.connect4.model;

/**
 * Jatekosokat testesiti meg
 * NONE NONE = ures cellak a jatektablan
 */
public enum Player {
    NONE(0),
    RED(1),
    YELLOW(2);

    private final int value;

    Player(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * A jatekosok ellenfelet hatarozza meg
     * @return visszateres a jatekossal, vagy NONE ha nincs jelenleg ellenfel.
     */
    public Player getOpponent() {
        if (this == RED) return YELLOW;
        if (this == YELLOW) return RED;
        return NONE;
    }
}
