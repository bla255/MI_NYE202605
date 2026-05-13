CREATE DATABASE IF NOT EXISTS connect4db;
USE connect4db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS games (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT,
    player2_id INT,
    winner_id INT NULL,
    is_draw BOOLEAN DEFAULT FALSE,
    move_count INT DEFAULT 0,
    difficulty INT,
    game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES users(id),
    FOREIGN KEY (player2_id) REFERENCES users(id),
    FOREIGN KEY (winner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS game_states (
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT,
    board_state TEXT NOT NULL,
    current_turn INT,
    step_number INT,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);
