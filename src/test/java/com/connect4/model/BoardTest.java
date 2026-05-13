package com.connect4.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(6, 7);
    }

    @Test
    void testInitialBoardIsEmpty() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                assertEquals(Player.NONE, board.getCell(r, c));
            }
        }
    }

    @Test
    void testMakeMove() {
        assertTrue(board.isValidMove(0));
        int row = board.makeMove(0, Player.RED);
        assertEquals(board.getRows() - 1, row);
        assertEquals(Player.RED, board.getCell(board.getRows() - 1, 0));
    }

    @Test
    void testColumnFull() {
        for (int i = 0; i < board.getRows(); i++) {
            board.makeMove(0, Player.RED);
        }
        assertFalse(board.isValidMove(0));
        assertEquals(-1, board.makeMove(0, Player.YELLOW));
    }

    @Test
    void testHorizontalWin() {
        board.makeMove(0, Player.RED);
        board.makeMove(1, Player.RED);
        board.makeMove(2, Player.RED);
        board.makeMove(3, Player.RED);
        assertTrue(board.checkWin(Player.RED));
        assertFalse(board.checkWin(Player.YELLOW));
    }

    @Test
    void testVerticalWin() {
        board.makeMove(0, Player.YELLOW);
        board.makeMove(0, Player.YELLOW);
        board.makeMove(0, Player.YELLOW);
        board.makeMove(0, Player.YELLOW);
        assertTrue(board.checkWin(Player.YELLOW));
        assertFalse(board.checkWin(Player.RED));
    }

    @Test
    void testDiagonalWin1() {
        // Build a diagonal from bottom-left to top-right
        board.makeMove(0, Player.RED);
        
        board.makeMove(1, Player.YELLOW);
        board.makeMove(1, Player.RED);
        
        board.makeMove(2, Player.YELLOW);
        board.makeMove(2, Player.YELLOW);
        board.makeMove(2, Player.RED);
        
        board.makeMove(3, Player.YELLOW);
        board.makeMove(3, Player.YELLOW);
        board.makeMove(3, Player.YELLOW);
        board.makeMove(3, Player.RED);
        
        assertTrue(board.checkWin(Player.RED));
    }

    @Test
    void testDynamicSize() {
        Board smallBoard = new Board(4, 5);
        assertEquals(4, smallBoard.getRows());
        assertEquals(5, smallBoard.getCols());
        smallBoard.makeMove(0, Player.RED);
        assertEquals(Player.RED, smallBoard.getCell(3, 0));
    }
}
