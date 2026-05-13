package com.connect4.model;

/**
 * Egy jatekbeli lepest testesit meg - adatait tarolja.
 */
public class Move {
    private int column;
    private Player player;

    public Move() {}

    public Move(int column, Player player) {
        this.column = column;
        this.player = player;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
