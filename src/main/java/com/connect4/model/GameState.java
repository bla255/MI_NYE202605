package com.connect4.model;

/**
 * Jatekallapotok
 */
public class GameState {
    
    public enum Status {
        IN_PROGRESS,
        WIN_PLAYER1,
        WIN_PLAYER2,
        DRAW
    }

    private Board board;
    private Player currentPlayer;
    private Status status;
    private int moveCount;

    /**
     * Uj jatekallapot, 6x7-es jatekmezovel
     */
    public GameState() {
        this(6, 7);
    }

    /**
     * Uj jatekallapot elkeszitese, ha eltero 6x7-tol a jatekmezo
     */
    public GameState(int rows, int cols) {
        this.board = new Board(rows, cols);
        this.currentPlayer = Player.RED; // mindig a RED kezd.
        this.status = Status.IN_PROGRESS;
        this.moveCount = 0;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }
    
    public void incrementMoveCount() {
        this.moveCount++;
    }

    /**
     * Ellenorzi, hogy a jatek befejezodott-e es aszerint frissiti a statust
     * @return true ha vege a jateknak, false ha meg nincs.
     */
    public boolean checkAndSetGameOver() {
        if (board.checkWin(Player.RED)) {
            status = Status.WIN_PLAYER1;
            return true;
        } else if (board.checkWin(Player.YELLOW)) {
            status = Status.WIN_PLAYER2;
            return true;
        } else if (board.isFull()) {
            status = Status.DRAW;
            return true;
        }
        return false;
    }
}
