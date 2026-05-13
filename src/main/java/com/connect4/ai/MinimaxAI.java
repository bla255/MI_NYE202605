package com.connect4.ai;

import com.connect4.model.Board;
import com.connect4.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimax AI Alfa-Beta vagassal
 */
public class MinimaxAI {

    private int searchDepth;
    private HeuristicEvaluator evaluator;
    private boolean loggingEnabled = false;

    public MinimaxAI(int searchDepth) {
        this.searchDepth = searchDepth;
        this.evaluator = new HeuristicEvaluator();
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }
    
    public int getSearchDepth() {
        return searchDepth;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    /**
     * Megnezi a legjobb lepest a jatekosnak
     * @param board a jelenlegi jatekallapot
     * @param aiPlayer az AI jatekos
     * @return A legjobb oszlop a korong elhelyezeshez
     */
    public int findBestMove(Board board, Player aiPlayer) {
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<Integer> validMoves = getValidMoves(board);
        if (validMoves.isEmpty()) return -1;
        
        StringBuilder logBuilder = new StringBuilder();
        if (loggingEnabled) {
            logBuilder.append("--- AI Lépés Keresése (Játékos: ").append(aiPlayer).append(") ---\n");
            logBuilder.append("Keresési mélység: ").append(searchDepth).append("\n");
        }
        
        // A legkonnyebb nehezsegegn, szimplan randomozunk ++ ha AI kezd vagy AI vs AI van, hogy ne a jatektabla kozepen nyisson, VELETLENSZERU dobassal nyit.
        if (searchDepth == 0 || board.isEmpty()) {
            int randomMove = validMoves.get((int) (Math.random() * validMoves.size()));
            if (loggingEnabled) {
                if (board.isEmpty()) {
                    logBuilder.append("Üres pálya (első lépés) - Véletlenszerű választás: ").append(randomMove).append("\n\n");
                } else {
                    logBuilder.append("Nagyon könnyű mód (0-ás mélység) - Véletlenszerű választás: ").append(randomMove).append("\n\n");
                }
                writeLog(logBuilder.toString());
            }
            return randomMove;
        }

        int centerCol = board.getCols() / 2;
        
        // Algo optimalizalas: Kozepso mezot nezzuk legeloszor, mivel altalaban az a legjobb
        validMoves.sort((a, b) -> Math.abs(b - centerCol) - Math.abs(a - centerCol));

        java.util.Map<Integer, Integer> evaluations = new java.util.TreeMap<>();

        for (int col : validMoves) {
            Board tempBoard = new Board(board);
            tempBoard.makeMove(col, aiPlayer);

            int score = minimax(tempBoard, searchDepth - 1, alpha, beta, false, aiPlayer);
            
            if (loggingEnabled) {
                evaluations.put(col, score);
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = col;
            }
            alpha = Math.max(alpha, bestScore);
        }

        if (loggingEnabled) {
            for (java.util.Map.Entry<Integer, Integer> entry : evaluations.entrySet()) {
                logBuilder.append("Oszlop: ").append(entry.getKey()).append(" -> Értékelés: ").append(entry.getValue()).append("\n");
            }
        }

        // Visszateres ha valami hiba csuszik az algoritmusbs
        if (bestMove == -1 && !validMoves.isEmpty()) {
            bestMove = validMoves.get(0);
        }
        
        if (loggingEnabled) {
            logBuilder.append("Kiválasztott oszlop: ").append(bestMove).append(" (Legjobb pontszám: ").append(bestScore).append(")\n\n");
            writeLog(logBuilder.toString());
        }

        return bestMove;
    }

    private void writeLog(String text) {
        try (java.io.FileWriter fw = new java.io.FileWriter("ai_log.txt", true)) {
            fw.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A minimax rekurziv algoritmusa
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing, Player aiPlayer) {
        boolean aiWon = board.checkWin(aiPlayer);
        boolean opponentWon = board.checkWin(aiPlayer.getOpponent());
        boolean isFull = board.isFull();

        // Fo nodeok
        if (depth == 0 || aiWon || opponentWon || isFull) {
            if (aiWon) {
                return 1000000 + depth; // Elony: Hamarabb nyerni
            } else if (opponentWon) {
                return -1000000 - depth; // Elony: Kesobb vesziteni
            } else if (isFull) {
                return 0; // Dontetlen
            } else {
                return evaluator.evaluate(board, aiPlayer); // Elerte a megengedett melyseget
            }
        }

        List<Integer> validMoves = getValidMoves(board);
        int centerCol = board.getCols() / 2;
        validMoves.sort((a, b) -> Math.abs(b - centerCol) - Math.abs(a - centerCol));

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : validMoves) {
                Board tempBoard = new Board(board);
                tempBoard.makeMove(col, aiPlayer);
                int eval = minimax(tempBoard, depth - 1, alpha, beta, false, aiPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta levagas
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col : validMoves) {
                Board tempBoard = new Board(board);
                tempBoard.makeMove(col, aiPlayer.getOpponent());
                int eval = minimax(tempBoard, depth - 1, alpha, beta, true, aiPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpfa levagas
                }
            }
            return minEval;
        }
    }

    /**
     * Listaba tesszuk az osszes lehetseges valid lepest
     */
    private List<Integer> getValidMoves(Board board) {
        List<Integer> moves = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) {
            if (board.isValidMove(c)) {
                moves.add(c);
            }
        }
        return moves;
    }
}
