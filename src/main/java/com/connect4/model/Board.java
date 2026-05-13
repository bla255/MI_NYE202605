package com.connect4.model;

import java.util.Arrays;

/**
 * Connect 4 jatektabla
 * Felelos ez az osztaly a korong lehelyezesert, gyozelem ellenorzesert es a jatekmenet fenntartasaerrt
 */
public class Board {
    
    private final int rows;
    private final int cols;
    private Player[][] grid;

    /**
     * Alap jatektabla konstruktora (7x6)
     */
    public Board() {
        this(6, 7);
    }

    /**
     * Ha elterunk az alapertelmezettol
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Player[rows][cols];
        reset();
    }

    /**
     * Konstruktor az AI reszere es az allapot klonozasa erdekebe
     * @param other Az adott tabla, melyet masolunk
     */
    public Board(Board other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.grid = new Player[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(other.grid[r], 0, this.grid[r], 0, cols);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Reseteli a boardot
     */
    public void reset() {
        for (int r = 0; r < rows; r++) {
            Arrays.fill(grid[r], Player.NONE);
        }
    }

    /**
     * Csekkoljuk, hogy a lepes valos-e, es elvegezheto-e
     * @param col az oszlopindex
     * @return ha az oszlopra lepes valos, false egyebnkent
     */
    public boolean isValidMove(int col) {
        return col >= 0 && col < cols && grid[0][col] == Player.NONE;
    }

    /**
     * Lerakjuk a korongot a kert helyre
     * @param col az oszlopindex
     * @param player a jatekos, aki lepakolja
     * @return row a sor ahova lerakjuk, -1el terunk vissza ha hibas vagy ervenytelen amit szeretnenk
     */
    public int makeMove(int col, Player player) {
        if (!isValidMove(col)) {
            return -1;
        }
        for (int r = rows - 1; r >= 0; r--) {
            if (grid[r][col] == Player.NONE) {
                grid[r][col] = player;
                return r;
            }
        }
        return -1;
    }

    /**
     * Visszavonjuk a lepest az adott oszlopban. Leginkabb AI miatt kerult implementalasra.
     * @param col Oszlopindex
     */
    public void undoMove(int col) {
        for (int r = 0; r < rows; r++) {
            if (grid[r][col] != Player.NONE) {
                grid[r][col] = Player.NONE;
                break;
            }
        }
    }

    /**
     * Jatekos cellajat lekrdezzuk
     * @param row Sorindex
     * @param col Oszlpindex
     * @return A jatekos aki elfoglalja a mezot
     */
    public Player getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Az egesz gridet lecsereli, visszatoltesnel szukseges
     * @param newGrid Az uj grid.
     */
    public void setGrid(Player[][] newGrid) {
        for (int r = 0; r < rows; r++) {
            System.arraycopy(newGrid[r], 0, this.grid[r], 0, cols);
        }
    }

    public Player[][] getGrid() {
        return grid;
    }

    /**
     * Megnezi, hogy az adott jatekos nyert-e a jatekot
     * @param player A jatekos
     * @return true ha van a jatekosnak 4 megfelelo tokenje egymashoz nezve.
     */
    public boolean checkWin(Player player) {
        // Fuggoleges ellenorzes
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 3; c++) {
                if (grid[r][c] == player && grid[r][c+1] == player &&
                    grid[r][c+2] == player && grid[r][c+3] == player) {
                    return true;
                }
            }
        }
        // Vizszintes ellenorzes
        for (int r = 0; r < rows - 3; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == player && grid[r+1][c] == player &&
                    grid[r+2][c] == player && grid[r+3][c] == player) {
                    return true;
                }
            }
        }
        // Atlo bal-felsobol, jobb alsora
        for (int r = 0; r < rows - 3; r++) {
            for (int c = 0; c < cols - 3; c++) {
                if (grid[r][c] == player && grid[r+1][c+1] == player &&
                    grid[r+2][c+2] == player && grid[r+3][c+3] == player) {
                    return true;
                }
            }
        }
        // Atlo bal-alsobol, jobb felsobe
        for (int r = 3; r < rows; r++) {
            for (int c = 0; c < cols - 3; c++) {
                if (grid[r][c] == player && grid[r-1][c+1] == player &&
                    grid[r-2][c+2] == player && grid[r-3][c+3] == player) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Megnezzuk a jatekmezo full-e
     * @return true ha tele van, false ha nem
     */
    public boolean isFull() {
        for (int c = 0; c < cols; c++) {
            if (grid[0][c] == Player.NONE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Megnezi, hogy a jatektabla ures-e teljesen
     * @return true ha igen, false ha nem.
     */
    public boolean isEmpty() {
        for (int c = 0; c < cols; c++) {
            if (grid[rows - 1][c] != Player.NONE) {
                return false;
            }
        }
        return true;
    }
}
