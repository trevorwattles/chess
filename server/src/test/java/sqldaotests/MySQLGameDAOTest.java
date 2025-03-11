package sqldaotests;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MySQLGameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLGameDAOTest {

    private MySQLGameDAO gameDAO;
    private ChessGame dummyGame;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear();
        dummyGame = new ChessGame();
    }

    @Test
    public void testCreateGamePositive() throws DataAccessException {
        GameData gameData = new GameData(0, "whiteUser", "blackUser", "Test Game", dummyGame); // Use 0 as placeholder
        assertDoesNotThrow(() -> gameDAO.createGame(gameData));

        List<GameData> games = gameDAO.listGames();
        assertFalse(games.isEmpty(), "No games found after creation");
        GameData retrieved = games.get(0);

        assertNotNull(retrieved);
        assertEquals(gameData.whiteUsername(), retrieved.whiteUsername());
        assertEquals(gameData.blackUsername(), retrieved.blackUsername());
        assertEquals(gameData.gameName(), retrieved.gameName());
        assertEquals(dummyGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testCreateGameNegativeDuplicate() throws DataAccessException {
        GameData gameData = new GameData(2, "whiteUser", "blackUser", "Duplicate Game", dummyGame);
        gameDAO.createGame(gameData);

        gameDAO.createGame(gameData);

        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size(), "Expected two games to be created");
        assertNotEquals(games.get(0).gameID(), games.get(1).gameID(), "Game IDs should be unique");

    }

    @Test
    public void testGetGamePositive() throws DataAccessException {
        GameData gameData = new GameData(0, "whiteUser", "blackUser", "Get Game", dummyGame);
        gameDAO.createGame(gameData);

        List<GameData> games = gameDAO.listGames();
        assertFalse(games.isEmpty(), "No games found after creation");
        int assignedId = games.get(0).gameID();

        GameData retrieved = gameDAO.getGame(assignedId);
        assertNotNull(retrieved);
        assertEquals(gameData.whiteUsername(), retrieved.whiteUsername());
        assertEquals(gameData.blackUsername(), retrieved.blackUsername());
        assertEquals(gameData.gameName(), retrieved.gameName());
        assertEquals(dummyGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testGetGameNegativeNotFound() throws DataAccessException {
        GameData retrieved = gameDAO.getGame(999);
        assertNull(retrieved);
    }

    @Test
    public void testListGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(0, "white1", "black1", "Game 1", dummyGame);
        GameData game2 = new GameData(0, "white2", "black2", "Game 2", dummyGame);
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);
        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void testListGamesNegativeEmpty() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    public void testUpdateGamePositive() throws DataAccessException {
        GameData gameData = new GameData(0, "whiteUser", "blackUser", "Original Game", dummyGame);
        gameDAO.createGame(gameData);

        List<GameData> games = gameDAO.listGames();
        assertFalse(games.isEmpty(), "No games found after creation");
        int assignedId = games.get(0).gameID();

        ChessGame updatedGame = new ChessGame();
        GameData updatedData = new GameData(assignedId, "whiteUser", "blackUser", "Updated Game", updatedGame);
        gameDAO.updateGame(updatedData);

        GameData retrieved = gameDAO.getGame(assignedId);
        assertNotNull(retrieved);
        assertEquals("Updated Game", retrieved.gameName());
        assertEquals(updatedGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testUpdateGameNegativeNonExistent() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        GameData nonExistent = new GameData(999, "white", "black", "Nonexistent Game", newGame);
        gameDAO.updateGame(nonExistent);
        GameData retrieved = gameDAO.getGame(999);
        assertNull(retrieved);
    }
}