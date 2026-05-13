package com.connect4.ui;

import com.connect4.model.Board;
import com.connect4.model.Player;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * UI komponensek
 */
public class BoardView extends GridPane {

    private static final int TILE_SIZE = 80;
    private Circle[][] tokens;

    public BoardView() {
        this.setStyle("-fx-background-color: #0B5345; -fx-padding: 10; -fx-hgap: 5; -fx-vgap: 5;");
    }

    /**
     * Felepiti vagy ujraepiti a jatekboardot
     * @param rows sorok
     * @param cols oszlopok
     */
    public void rebuildBoard(int rows, int cols) {
        this.getChildren().clear();
        this.tokens = new Circle[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Egy helynek a vizualis felepitese
                Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                rect.setFill(Color.web("#148F77"));
                rect.setArcWidth(20);
                rect.setArcHeight(20);

                Circle circle = new Circle(TILE_SIZE / 2.0 - 5);
                circle.setFill(Color.WHITE);
                tokens[r][c] = circle;

                GridPane cell = new GridPane();
                cell.add(rect, 0, 0);
                cell.add(circle, 0, 0);

                // Kenyszeritjuk hogy a kor az objekt kozepen legyen
                javafx.geometry.HPos hpos = javafx.geometry.HPos.CENTER;
                javafx.geometry.VPos vpos = javafx.geometry.VPos.CENTER;
                GridPane.setHalignment(circle, hpos);
                GridPane.setValignment(circle, vpos);

                this.add(cell, c, r);
            }
        }
    }

    /**
     * A jatektabla frissitese
     * @param board A tabla logikaja
     */
    public void update(Board board) {
        int rows = board.getRows();
        int cols = board.getCols();

        //Ha nem egyeznek a dimenziok, mondjuk visszatoltes utan, ujjaepitjuk a board ui-t
        if (tokens == null || tokens.length != rows || tokens[0].length != cols) {
            rebuildBoard(rows, cols);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Player p = board.getCell(r, c);
                if (p == Player.RED) {
                    tokens[r][c].setFill(Color.RED);
                } else if (p == Player.YELLOW) {
                    tokens[r][c].setFill(Color.YELLOW);
                } else {
                    tokens[r][c].setFill(Color.WHITE);
                }
            }
        }
    }

    public int getTileSize() {
        return TILE_SIZE;
    }
}
