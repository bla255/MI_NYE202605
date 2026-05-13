package com.connect4;

import com.connect4.model.GameState;
import com.connect4.model.Player;
import com.connect4.repository.DatabaseManager;
import com.connect4.repository.GameRepository;
import com.connect4.service.GameManager;
import com.connect4.ui.BoardView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.File;

/**
 * Fo Main app osztaly, JavaFX inditasa + UI.
 */
public class Connect4App extends Application {

    private GameManager gameManager;
    private DatabaseManager dbManager;
    private GameRepository repository;
    
    private BoardView boardView;
    private Label statusLabel;
    private Label dbStatusLabel;
    
    private TextField playerNameField;
    private ComboBox<GameManager.GameMode> modeComboBox;
    private ComboBox<String> startingPlayerComboBox;
    private ComboBox<String> difficultyComboBox;
    private Spinner<Integer> colsSpinner;
    private Spinner<Integer> rowsSpinner;
    private CheckBox aiLogCheckBox;

    @Override
    public void start(Stage primaryStage) {
        // backend adatbaziskezelo-repo-jatekmanager behivasa
        dbManager = new DatabaseManager();
        repository = new GameRepository(dbManager);
        gameManager = new GameManager(dbManager, repository);

        // board UI osztaly letrehozas
        boardView = new BoardView();
        
        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setRight(createControlPanel(primaryStage));

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Connect 4 MI beadandó");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Interakcio letrehozasa
        setupInteraction();
        startNewGame(false); //Letrehoz a hatterbe egy alapertelmezett jatekboardot.
        updateDBStatus();
    }

    private VBox createControlPanel(Stage stage) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 0 0 0 1;");
        panel.setPrefWidth(260);

        Label title = new Label("Vezérlőpult");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        statusLabel = new Label("Piros jön");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-font-weight: bold;");

        // DB allapot + ujraproba
        dbStatusLabel = new Label("DB: Ellenőrzés...");
        dbStatusLabel.setStyle("-fx-font-weight: bold;");
        Button retryDbBtn = new Button("Kapcsolat Újrapróbálkozás");
        retryDbBtn.setOnAction(e -> {
            dbManager.reconnect();
            updateDBStatus();
        });

        // Jatekosnev
        playerNameField = new TextField();
        playerNameField.setPromptText("Játékos neve");

        modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll(
                GameManager.GameMode.EMBER_VS_AI,
                GameManager.GameMode.EMBER_VS_EMBER,
                GameManager.GameMode.AI_VS_AI
        );
        modeComboBox.setValue(GameManager.GameMode.EMBER_VS_AI);

        startingPlayerComboBox = new ComboBox<>();
        startingPlayerComboBox.getItems().addAll(
            "Játékos (Piros)",
            "AI / Másik (Sárga)",
            "Véletlenszerű"
        );
        startingPlayerComboBox.setValue("Játékos (Piros)");

        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll(
            "0 - Random",
            "2 Mélyésg",
            "4 Mélység",
            "6 Mélység",
            "8 Mélység",
            "10 Mélység"
        );
        difficultyComboBox.setValue("8 Mélység"); // Az alap mélyésg, ami a comboboxba van, legyen 8

        // Jatektabla meretek
        colsSpinner = new Spinner<>(4, 7, 7); // Min 4, Max 7, Alapertelmezett 7
        rowsSpinner = new Spinner<>(4, 6, 6); // Min 4, Max 6, Alapertelmezett 6

        // AI Log Checkbox
        aiLogCheckBox = new CheckBox("AI lépések mentése (ai_log.txt)");
        aiLogCheckBox.setSelected(false);
        aiLogCheckBox.setOnAction(e -> gameManager.setAiLoggingEnabled(aiLogCheckBox.isSelected()));

        Button newGameBtn = new Button("Új játék");
        newGameBtn.setMaxWidth(Double.MAX_VALUE);
        newGameBtn.setOnAction(e -> startNewGame(true));

        Button saveBtn = new Button("Mentés");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveGame(stage));

        Button loadBtn = new Button("Betöltés");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadGame(stage));
        
        Button playAIBtn = new Button("AI léptetése (AI vs AI)");
        playAIBtn.setMaxWidth(Double.MAX_VALUE);
        playAIBtn.setOnAction(e -> handleAITurn());

        Button statsBtn = new Button("Statisztikák");
        statsBtn.setMaxWidth(Double.MAX_VALUE);
        statsBtn.setOnAction(e -> showStatistics());

        panel.getChildren().addAll(
                title, new Separator(),
                new Label("Adatbázis:"), dbStatusLabel, retryDbBtn,
                new Separator(),
                new Label("Állapot:"), statusLabel,
                new Label("Játékos neve:"), playerNameField,
                new Label("Játékmód:"), modeComboBox,
                new Label("Kezdőlépés:"), startingPlayerComboBox,
                new Label("Nehézség (Minimax):"), difficultyComboBox,
                new Label("Oszlopok száma:"), colsSpinner,
                new Label("Sorok száma:"), rowsSpinner,
                aiLogCheckBox,
                new Separator(),
                newGameBtn, saveBtn, loadBtn, playAIBtn, statsBtn
        );

        return panel;
    }

    private void showStatistics() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Figyelem", "Kérlek, add meg a játékos nevét a statisztikák megtekintéséhez.");
            return;
        }

        if (!dbManager.isConnected()) {
            showAlert(Alert.AlertType.ERROR, "Adatbázis Hiba", "Nincs kapcsolat az adatbázissal. Kérlek, próbáld újraépíteni a kapcsolatot.");
            return;
        }

        String stats = repository.getStatistics(playerName);
        showAlert(Alert.AlertType.INFORMATION, "Játékos Statisztikák", stats);
    }

    private void updateDBStatus() {
        if (dbManager.isConnected()) {
            dbStatusLabel.setText("DB: Kapcsolódva");
            dbStatusLabel.setTextFill(Color.GREEN);
        } else {
            dbStatusLabel.setText("DB: Nincs kapcsolat!");
            dbStatusLabel.setTextFill(Color.RED);
        }
    }

    private void setupInteraction() {
        boardView.setOnMouseClicked(event -> {
            if (playerNameField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Név megadása kötelező", "A játék megkezdése előtt kötelező megadni a neved!");
                return;
            }

            if (gameManager.getGameState().getStatus() != GameState.Status.IN_PROGRESS) {
                return;
            }

            if (gameManager.getGameMode() == GameManager.GameMode.AI_VS_AI) {
                return;
            }
            
            if (gameManager.getGameMode() == GameManager.GameMode.EMBER_VS_AI 
                && gameManager.getGameState().getCurrentPlayer() == Player.YELLOW) {
                return;
            }

            double clickX = event.getX();
            // Kiszamoljuk  melyik oszlopra lett katt, figyelembe veve a kor meret + rest, hogy ne misclicket nezzen
            int col = (int) (clickX / (boardView.getTileSize() + 5));
            
            if (col >= 0 && col < gameManager.getGameState().getBoard().getCols()) {
                try {
                    boolean moved = gameManager.playTurn(col);
                    if (moved) {
                        updateUI();
                        
                        if (gameManager.getGameState().getStatus() == GameState.Status.IN_PROGRESS && 
                            gameManager.getGameMode() == GameManager.GameMode.EMBER_VS_AI) {
                            
                            handleAITurn();
                        }
                    }
                } catch (RuntimeException e) {
                    // Ha a DB mentes sikertelen, akkor hibara fut
                    updateUI();
                    showAlert(Alert.AlertType.WARNING, "Adatbázis hiba", e.getMessage() + "\nEllenőrizd a kapcsolatot!");
                }
            }
        });
    }
    //AI lepes kezelese
    private void handleAITurn() {
        if (gameManager.getGameState().getStatus() != GameState.Status.IN_PROGRESS) return;
        
        statusLabel.setText("AI gondolkodik...");
        statusLabel.setTextFill(Color.GRAY);
        
        Thread aiThread = new Thread(() -> {
            try {
                gameManager.playAITurn();
                Platform.runLater(this::updateUI);
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    updateUI();
                    showAlert(Alert.AlertType.WARNING, "Adatbázis hiba", e.getMessage() + "\nEllenőrizd a kapcsolatot!");
                });
            }
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }
//uj game letrehoz
    private void startNewGame(boolean showWarning) {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            if (showWarning) {
                showAlert(Alert.AlertType.WARNING, "Név megadása kötelező", "A játék indítása előtt kötelező megadni a neved!");
            }
            //Blokkoljuk a jatekot, ha a jatekos neve ures
            gameManager.getGameState().setStatus(GameState.Status.DRAW);
            statusLabel.setText("Név megadására vár...");
            statusLabel.setTextFill(Color.GRAY);
            return;
        }

        int rows = rowsSpinner.getValue();
        int cols = colsSpinner.getValue();
        
        gameManager.setGameMode(modeComboBox.getValue());
        
        // Jateknevet a managernek leadjuk
        if (gameManager.getGameMode() == GameManager.GameMode.EMBER_VS_EMBER) {
            gameManager.setPlayerNames(playerName, "Player 2");
        } else if (gameManager.getGameMode() == GameManager.GameMode.EMBER_VS_AI) {
            gameManager.setPlayerNames(playerName, "AI");
        }
        
        // a melyseget kiszedjuk a combobox ertekebol, mivel mas is van az integer utan, lecsapjuk a nem szukseges reszt
        int depth = Integer.parseInt(difficultyComboBox.getValue().split(" ")[0]);
        gameManager.setDifficulty(depth);
        gameManager.setAiLoggingEnabled(aiLogCheckBox.isSelected());
        
        Player startingPlayer = Player.RED;
        String startChoice = startingPlayerComboBox.getValue();
        if (startChoice.equals("AI / Másik (Sárga)")) {
            startingPlayer = Player.YELLOW;
        } else if (startChoice.equals("Véletlenszerű")) {
            startingPlayer = Math.random() < 0.5 ? Player.RED : Player.YELLOW;
        }

        gameManager.startNewGame(rows, cols, startingPlayer);
        
        updateUI();

        if (gameManager.getGameState().getCurrentPlayer() == Player.YELLOW && 
            gameManager.getGameMode() == GameManager.GameMode.EMBER_VS_AI &&
            gameManager.getGameState().getStatus() == GameState.Status.IN_PROGRESS) {
            handleAITurn();
        }
    }

    private void saveGame(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Játék mentése");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                gameManager.saveGame(file);
                showAlert(Alert.AlertType.INFORMATION, "Sikeres mentés", "A játékállapot mentésre került.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült menteni a fájlt: " + e.getMessage());
            }
        }
    }

    private void loadGame(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Játék betöltése");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                gameManager.loadGame(file);
                // Megfrissitjuk az UI listat, hogy a betoltott ertekeket lathassuk
                colsSpinner.getValueFactory().setValue(gameManager.getGameState().getBoard().getCols());
                rowsSpinner.getValueFactory().setValue(gameManager.getGameState().getBoard().getRows());
                
                updateUI();
                showAlert(Alert.AlertType.INFORMATION, "Sikeres betöltés", "A játékállapot betöltésre került.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Hiba", "Nem sikerült betölteni a fájlt: " + e.getMessage());
            }
        }
    }

    private void updateUI() {
        boardView.update(gameManager.getGameState().getBoard());
        
        GameState.Status status = gameManager.getGameState().getStatus();
        if (status == GameState.Status.IN_PROGRESS) {
            if (gameManager.getGameState().getCurrentPlayer() == Player.RED) {
                statusLabel.setText("Piros (" + playerNameField.getText() + ") jön");
                statusLabel.setTextFill(Color.RED);
            } else {
                statusLabel.setText("Sárga (P2 / AI) jön");
                statusLabel.setTextFill(Color.rgb(200, 150, 0));
            }
        } else if (status == GameState.Status.WIN_PLAYER1) {
            statusLabel.setText("PIROS NYERT!");
            statusLabel.setTextFill(Color.RED);
        } else if (status == GameState.Status.WIN_PLAYER2) {
            statusLabel.setText("SÁRGA NYERT!");
            statusLabel.setTextFill(Color.rgb(200, 150, 0));
        } else if (status == GameState.Status.DRAW) {
            statusLabel.setText("DÖNTETLEN!");
            statusLabel.setTextFill(Color.BLUE);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
