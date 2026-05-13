package com.connect4.service;

import com.connect4.model.Board;
import com.connect4.model.Player;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Jatekallas mentese és visszatoltese JSON fajbol vagy fajlba.
 */
public class PersistenceManager {

    private final ObjectMapper mapper;

    public PersistenceManager() {
        this.mapper = new ObjectMapper();
    }

    /**
     * DTO (DataTransferObject JSON szerializaciohoz
     */
    public static class SaveStateDTO {
        public Player[][] grid;
        public Player currentPlayer;
        public int moveCount;

        public SaveStateDTO() {}

        public SaveStateDTO(Player[][] grid, Player currentPlayer, int moveCount) {
            this.grid = grid;
            this.currentPlayer = currentPlayer;
            this.moveCount = moveCount;
        }
    }

    /**
     * Jatekallas mentese JSON-be
     * @param file A fajl amibe ment
     * @param board A jelenlegi jatektabla - jatekmezo
     * @param currentPlayer A jelenlegi jatekos aki lep
     * @param moveCount Eddig elvegzett lepesek szama
     * @throws IOException Ha gondba utkozne a filementes
     */
    public void saveGame(File file, Board board, Player currentPlayer, int moveCount) throws IOException {
        SaveStateDTO dto = new SaveStateDTO(board.getGrid(), currentPlayer, moveCount);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, dto);
    }

    /**
     * JSON-bol visszatoltjuk az elmentett jatekot
     * @param file a fajl, ahonnan visszatoltsuk
     * @return Visszaterunk a betoltott dto-val
     * @throws IOException ha valami miatt meghiusul a beolvasas
     */
    public SaveStateDTO loadGame(File file) throws IOException {
        return mapper.readValue(file, SaveStateDTO.class);
    }
}
