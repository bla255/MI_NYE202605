package com.connect4.ai;

import com.connect4.model.Board;
import com.connect4.model.Player;

/**
 * Heurisztikus értékelő függvény a Connect4 táblához.
 * Egy adott játékos szemszögéből értékeli a tábla aktuális állását.
 */
public class HeuristicEvaluator {

    private static final int WIN_SCORE = 100000;
    private static final int THREE_IN_A_ROW_SCORE = 100;
    private static final int TWO_IN_A_ROW_SCORE = 10;

/**
     * Kiértékeli a tábla állapotát. 
     * A pozitív pontszám azt jelenti, hogy a helyzet kedvező a játékosnak, 
     * a negatív pontszám pedig, az ellenfél áll jobban.
     * 
     * @param board Az aktuális játéktábla.
     * @param player A játékos, a maximalizáló játékos.
     * @return A heurisztikus pontszám.
     */
	 
    public int evaluate(Board board, Player player) {
        int score = 0;
        Player opponent = player.getOpponent();
        int rows = board.getRows();
        int cols = board.getCols();

        // Középső oszlop értékelése (a középpontok uralása előnyt jelent a Connect4-ben)
        int centerCount = 0;
        int centerCol = cols / 2;
        for (int r = 0; r < rows; r++) {
            if (board.getCell(r, centerCol) == player) {
                centerCount++;
            }
        }
        score += centerCount * 3; // Kis súlyt adunk a középső oszlopban lévő korongoknak, játék leírás
		
        // Vízszintes helyzet kiértékelés
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 3; c++) {
                Player[] window = {
                    board.getCell(r, c), board.getCell(r, c+1),
                    board.getCell(r, c+2), board.getCell(r, c+3)
                };
                score += evaluateWindow(window, player, opponent);
            }
        }

        // Függőleges helyzet kiértékelés
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 3; r++) {
                Player[] window = {
                    board.getCell(r, c), board.getCell(r+1, c),
                    board.getCell(r+2, c), board.getCell(r+3, c)
                };
                score += evaluateWindow(window, player, opponent);
            }
        }

        // Átlós helyzet vizsgálata (ereszkedő átló: \ )
        for (int r = 3; r < rows; r++) {
            for (int c = 0; c < cols - 3; c++) {
                Player[] window = {
                    board.getCell(r, c), board.getCell(r-1, c+1),
                    board.getCell(r-2, c+2), board.getCell(r-3, c+3)
                };
                score += evaluateWindow(window, player, opponent);
            }
        }

        // Átlós helyzet  (ereszkedő átló: \ )
        for (int r = 0; r < rows - 3; r++) {
            for (int c = 0; c < cols - 3; c++) {
                Player[] window = {
                    board.getCell(r, c), board.getCell(r+1, c+1),
                    board.getCell(r+2, c+2), board.getCell(r+3, c+3)
                };
                score += evaluateWindow(window, player, opponent);
            }
        }

        return score;
    }

    /**
     * 4 cellás szakaszt kiértékel
     */
    private int evaluateWindow(Player[] window, Player player, Player opponent) {
        int score = 0;
        int playerCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;

        for (Player p : window) {
            if (p == player) playerCount++;
            else if (p == opponent) opponentCount++;
            else emptyCount++;
        }
// Pontozás a saját szemszögünkből
        if (playerCount == 4) {
            score += WIN_SCORE;
        } else if (playerCount == 3 && emptyCount == 1) {
            score += THREE_IN_A_ROW_SCORE;
        } else if (playerCount == 2 && emptyCount == 2) {
            score += TWO_IN_A_ROW_SCORE;
        }


// Büntetés az ellenfél esélyei alapján
        if (opponentCount == 3 && emptyCount == 1) {
            score -= THREE_IN_A_ROW_SCORE * 2; // PErősebb büntetés, hogy blokkolni akarja az ellenfelet
        } else if (opponentCount == 2 && emptyCount == 2) {
             score -= TWO_IN_A_ROW_SCORE;
        } else if (opponentCount == 4) {
            score -= WIN_SCORE;
        }

        return score;
    }
}
