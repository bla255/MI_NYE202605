package com.connect4.ai;

import com.connect4.model.Board;
import com.connect4.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MinimaxAITest {

    private MinimaxAI ai;
    private Board board;

    @BeforeEach
    void setUp() {
        ai = new MinimaxAI(4); // Test with lower depth for speed
        board = new Board(6, 7);
    }

    @Test
    void testFindImmediateWin() {
        // Set up a situation where RED can win immediately in column 0
        board.makeMove(0, Player.RED);
        board.makeMove(0, Player.RED);
        board.makeMove(0, Player.RED);
        
        int bestMove = ai.findBestMove(board, Player.RED);
        assertEquals(0, bestMove, "AI should choose the winning move");
    }

    @Test
    void testBlockOpponentWin() {
        // Set up a situation where YELLOW is about to win in column 1
        board.makeMove(1, Player.YELLOW);
        board.makeMove(1, Player.YELLOW);
        board.makeMove(1, Player.YELLOW);
        
        // RED must block in column 1
        int bestMove = ai.findBestMove(board, Player.RED);
        assertEquals(1, bestMove, "AI should block the opponent's winning move");
    }
}
