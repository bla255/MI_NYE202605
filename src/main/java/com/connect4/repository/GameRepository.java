package com.connect4.repository;

import com.connect4.model.Board;
import com.connect4.model.GameState;
import com.connect4.model.Player;

import java.sql.*;

/**
 * Alap JDBC Adatbazis kezeles Felhasznalok, Jatekallapotok
 */
public class GameRepository {

    private DatabaseManager dbManager;

    public GameRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Megnezzuk hogy letezik-e ilyen user, ha igen eloszedjuk az id-jét, ha nem, akkor beszurjuk a tablaba
     */
    public int getOrCreateUser(String username) {
        Connection conn = dbManager.getConnection();
        if (conn == null) return -1;

        String selectSql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSql = "INSERT INTO users (username) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Bementjuk a jatekallast, HA vege.
     * @return Visszater a jatekIDv-el
     */
    public int saveGameResult(String player1Name, String player2Name, GameState gameState, int difficulty) {
        int p1Id = getOrCreateUser(player1Name);
        int p2Id = getOrCreateUser(player2Name);
        int winnerId = -1;

        if (gameState.getStatus() == GameState.Status.WIN_PLAYER1) {
            winnerId = p1Id;
        } else if (gameState.getStatus() == GameState.Status.WIN_PLAYER2) {
            winnerId = p2Id;
        }

        Connection conn = dbManager.getConnection();
        if (conn == null) return -1;

        String sql = "INSERT INTO games (player1_id, player2_id, winner_id, is_draw, move_count, difficulty) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, p1Id);
            pstmt.setInt(2, p2Id);
            if (winnerId != -1) {
                pstmt.setInt(3, winnerId);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setBoolean(4, gameState.getStatus() == GameState.Status.DRAW);
            pstmt.setInt(5, gameState.getMoveCount());
            pstmt.setInt(6, difficulty);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Statisztikat selecteli a tablabol, osszegzi - statot adja
     */
    public String getStatistics(String username) {
        if (!dbManager.isConnected()) {
            return "Adatbázis hiba: Nincs kapcsolat.";
        }

        int userId = getOrCreateUser(username);
        if (userId == -1) {
            return "Hiba a felhasználó lekérdezésekor.";
        }

        String sql = "SELECT " +
                "COUNT(*) as total_games, " +
                "SUM(CASE WHEN winner_id = ? THEN 1 ELSE 0 END) as wins, " +
                "SUM(CASE WHEN is_draw = TRUE THEN 1 ELSE 0 END) as draws " +
                "FROM games " +
                "WHERE player1_id = ? OR player2_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalGames = rs.getInt("total_games");
                int wins = rs.getInt("wins");
                int draws = rs.getInt("draws");
                int losses = totalGames - wins - draws;

                return String.format(
                        "Játékos: %s\nÖsszes játék: %d\nGyőzelmek: %d\nVereségek: %d\nDöntetlenek: %d",
                        username, totalGames, wins, losses, draws
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Hiba a statisztikák lekérdezésekor: " + e.getMessage();
        }

        return "Nincs elérhető statisztika.";
    }

    /**
     * SZerializaljuk a boardot szimpla stringge, karakterlancca
     */
    private String serializeBoard(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                sb.append(board.getCell(r, c).getValue());
            }
        }
        return sb.toString();
    }
}
