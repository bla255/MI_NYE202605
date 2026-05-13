package com.connect4.service;

import com.connect4.ai.MinimaxAI;
import com.connect4.model.Board;
import com.connect4.model.GameState;
import com.connect4.model.Player;
import com.connect4.repository.DatabaseManager;
import com.connect4.repository.GameRepository;

import java.io.File;
import java.io.IOException;

/**
 * Jateklogika elemei, osszekotve az UI+AI+Adatbazissal
 */
public class GameManager {

    public enum GameMode {
        EMBER_VS_AI,
        EMBER_VS_EMBER,
        AI_VS_AI
    }

    private GameState gameState;
    private MinimaxAI aiPlayer1;
    private MinimaxAI aiPlayer2;
    private GameMode gameMode;
    private DatabaseManager dbManager;
    private GameRepository repository;
    private PersistenceManager persistenceManager;

    private String player1Name = "Jatekos 1";
    private String player2Name = "AI";

    public GameManager(DatabaseManager dbManager, GameRepository repository) {
        this.dbManager = dbManager;
        this.repository = repository;
        this.persistenceManager = new PersistenceManager();
        this.gameState = new GameState(); // Alap a Connect4ban a 7X6
        this.aiPlayer2 = new MinimaxAI(8); //8-as melysegu AI-t inditunk
        this.aiPlayer1 = new MinimaxAI(8); //8-as melysegu AI-t inditunk
        this.gameMode = GameMode.EMBER_VS_AI;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        if (gameMode == GameMode.EMBER_VS_EMBER) {
            player2Name = "Jatekos 2";
        } else if (gameMode == GameMode.EMBER_VS_AI) {
            player2Name = "AI";
        } else if (gameMode == GameMode.AI_VS_AI) {
            player1Name = "AI 1";
            player2Name = "AI 2";
        }
    }

    public void setPlayerNames(String p1, String p2) {
        this.player1Name = p1;
        this.player2Name = p2;
    }

    public void setDifficulty(int depth) {
        aiPlayer1.setSearchDepth(depth);
        aiPlayer2.setSearchDepth(depth);
    }

    private boolean aiLoggingEnabled = false;

    public void setAiLoggingEnabled(boolean enabled) {
        this.aiLoggingEnabled = enabled;
        aiPlayer1.setLoggingEnabled(enabled);
        aiPlayer2.setLoggingEnabled(enabled);
    }

    /**
     * Elinditjuk a gamet az uj konfiggal
     */
    public void startNewGame(int rows, int cols, Player startingPlayer) {
        this.gameState = new GameState(rows, cols);
        this.gameState.setCurrentPlayer(startingPlayer);
//Ha logger megy, akkor a gyokerbe ai_log.txt-be behuzzuk az kezdodatumot a fejlecbe
        if (this.aiLoggingEnabled) {
            try (java.io.FileWriter fw = new java.io.FileWriter("ai_log.txt", true)) {
                String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                fw.write("\n=========================================\n");
                fw.write("ÚJ JÁTÉK KEZDŐDÖTT: " + timeStamp + "\n");
                fw.write("=========================================\n\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Megprobal a jatek egy korongot letenni
     * @param col Az oszlopindex
     * @return true ha a mozgas valos es vegrehajtva
     * @throws RuntimeException ha az adatbazis mentes meghiusul, akkor dob egy exceptiont
     */
    public boolean playTurn(int col) throws RuntimeException {
        if (gameState.getStatus() != GameState.Status.IN_PROGRESS) {
            return false;
        }

        Board board = gameState.getBoard();
        if (!board.isValidMove(col)) {
            return false;
        }

        board.makeMove(col, gameState.getCurrentPlayer());
        gameState.incrementMoveCount();

        if (gameState.checkAndSetGameOver()) {
            saveResultToDatabase();
            return true;
        }

        // Atadjuk a masik jatekosnak a lehetoseget lepni
        gameState.setCurrentPlayer(gameState.getCurrentPlayer().getOpponent());
        return true;
    }

    /**
     * AI lepest meghivja, ha szuksges
     * @return AI altal valasztott oszlopot visszadobja, vagy -1, ha nem volt mozgas
     * @throws RuntimeException ha mentes sikertelen
     */
    public int playAITurn() throws RuntimeException {
        if (gameState.getStatus() != GameState.Status.IN_PROGRESS) {
            return -1;
        }

        Player current = gameState.getCurrentPlayer();
        int bestMove = -1;

        if (current == Player.YELLOW && (gameMode == GameMode.EMBER_VS_AI || gameMode == GameMode.AI_VS_AI)) {
            bestMove = aiPlayer2.findBestMove(gameState.getBoard(), current);
        } else if (current == Player.RED && gameMode == GameMode.AI_VS_AI) {
            bestMove = aiPlayer1.findBestMove(gameState.getBoard(), current);
        }

        if (bestMove != -1) {
            playTurn(bestMove);
            return bestMove;
        }
        return -1;
    }
//adatbazisba bementjuk a jatekot
    private void saveResultToDatabase() throws RuntimeException {
        if (repository != null) {
            if (!dbManager.isConnected()) {
                throw new RuntimeException("Nincs adatbáziskapcsolat! Az eredmény nem menthető.");
            }
            repository.saveGameResult(player1Name, player2Name, gameState, aiPlayer2.getSearchDepth());
        }
    }

    public void saveGame(File file) throws IOException {
        persistenceManager.saveGame(file, gameState.getBoard(), gameState.getCurrentPlayer(), gameState.getMoveCount());
    }

    public void loadGame(File file) throws IOException {
        PersistenceManager.SaveStateDTO dto = persistenceManager.loadGame(file);
        
        // Jatekvisszatolteshez visszaszedjuk a sort es oszlopot integerre, majd bedobjuk a jatekallapotba
        int loadedRows = dto.grid.length;
        int loadedCols = dto.grid[0].length;
        
        this.gameState = new GameState(loadedRows, loadedCols);
        this.gameState.getBoard().setGrid(dto.grid);
        this.gameState.setCurrentPlayer(dto.currentPlayer);
        this.gameState.setMoveCount(dto.moveCount);
        this.gameState.checkAndSetGameOver();
    }
}
