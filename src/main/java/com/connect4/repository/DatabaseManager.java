package com.connect4.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MYSQL JDBC
 */
public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/connect4db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    /**
     * Adatbazis kapcsolodas, ha letezik csatlakozik - ha nem, akkor letrehozza.
     */
    public DatabaseManager() {
        connect();
    }
    
    /**
     * Egy kapcsolodast letrehozunk, megnezzuk mukodik-e
     */
    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    private void connect() {
        try {
            // Megnezzuk hogy a jdbc driver megy-e, try blokk
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            initializeTables();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Adatbazis kapcsolodas sikertelen: " + e.getMessage());
            this.connection = null;
        }
    }

    /**
     * Teszteljuj, hogy el-e meg a kapcsolat
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Ujrakapcsolodas megkiserlese
     * @return true ha sikeres, false ha lehalt a proba
     */
    public boolean reconnect() {
        close();
        connect();
        return isConnected();
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Inicializalja az MSQL tablat, ha nem talalja, letrehozza.
     */
    private void initializeTables() {
        if (connection == null) return;
        
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL)";

        String createGames = "CREATE TABLE IF NOT EXISTS games (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player1_id INT, " +
                "player2_id INT, " +
                "winner_id INT NULL, " +
                "is_draw BOOLEAN DEFAULT FALSE, " +
                "move_count INT DEFAULT 0, " +
                "difficulty INT, " +
                "game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (player1_id) REFERENCES users(id), " +
                "FOREIGN KEY (player2_id) REFERENCES users(id), " +
                "FOREIGN KEY (winner_id) REFERENCES users(id))";

        String createStates = "CREATE TABLE IF NOT EXISTS game_states (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "game_id INT, " +
                "board_state TEXT NOT NULL, " +
                "current_turn INT, " +
                "step_number INT, " +
                "FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createGames);
            stmt.execute(createStates);
        } catch (SQLException e) {
            System.err.println("Hiba az inicializalas kozbe: " + e.getMessage());
        }
    }

    /**
     * Kapcsolat bezarasa
     */
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Kpacsolat lezarasa sikertelen: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
