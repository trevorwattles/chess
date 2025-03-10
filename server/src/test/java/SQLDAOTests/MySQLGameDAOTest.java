package SQLDAOTests;

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
    public void testCreateGame_Positive() throws DataAccessException {
        GameData gameData = new GameData(1, "whiteUser", "blackUser", "Test Game", dummyGame);
        assertDoesNotThrow(() -> gameDAO.createGame(gameData));

        GameData retrieved = gameDAO.getGame(1);
        assertNotNull(retrieved);
        assertEquals(gameData.gameID(), retrieved.gameID());
        assertEquals(gameData.whiteUsername(), retrieved.whiteUsername());
        assertEquals(gameData.blackUsername(), retrieved.blackUsername());
        assertEquals(gameData.gameName(), retrieved.gameName());
        assertEquals(dummyGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testCreateGame_Negative_Duplicate() throws DataAccessException {
        GameData gameData = new GameData(2, "whiteUser", "blackUser", "Duplicate Game", dummyGame);
        gameDAO.createGame(gameData);
        Exception exception = assertThrows(DataAccessException.class, () -> gameDAO.createGame(gameData));
        String expectedMessage = "Error inserting game record";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }


    @Test
    public void testGetGame_Positive() throws DataAccessException {
        GameData gameData = new GameData(3, "whiteUser", "blackUser", "Get Game", dummyGame);
        gameDAO.createGame(gameData);
        GameData retrieved = gameDAO.getGame(3);
        assertNotNull(retrieved);
        assertEquals(gameData.gameID(), retrieved.gameID());
        assertEquals(gameData.whiteUsername(), retrieved.whiteUsername());
        assertEquals(gameData.blackUsername(), retrieved.blackUsername());
        assertEquals(gameData.gameName(), retrieved.gameName());
        assertEquals(dummyGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testGetGame_Negative_NotFound() throws DataAccessException {
        GameData retrieved = gameDAO.getGame(999);
        assertNull(retrieved);
    }


    @Test
    public void testListGames_Positive() throws DataAccessException {
        GameData game1 = new GameData(4, "white1", "black1", "Game 1", dummyGame);
        GameData game2 = new GameData(5, "white2", "black2", "Game 2", dummyGame);
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);
        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void testListGames_Negative_Empty() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }


    @Test
    public void testUpdateGame_Positive() throws DataAccessException {
        GameData gameData = new GameData(6, "whiteUser", "blackUser", "Original Game", dummyGame);
        gameDAO.createGame(gameData);
        // Create an updated ChessGame instance.
        ChessGame updatedGame = new ChessGame(); // Assume this instance represents a changed game state.
        GameData updatedData = new GameData(6, "whiteUser", "blackUser", "Updated Game", updatedGame);
        gameDAO.updateGame(updatedData);
        GameData retrieved = gameDAO.getGame(6);
        assertNotNull(retrieved);
        assertEquals("Updated Game", retrieved.gameName());
        assertEquals(updatedGame.toString(), retrieved.game().toString());
    }

    @Test
    public void testUpdateGame_Negative_NonExistent() throws DataAccessException {
        // Attempt to update a game that doesn't exist.
        ChessGame newGame = new ChessGame();
        GameData nonExistent = new GameData(999, "white", "black", "Nonexistent Game", newGame);
        gameDAO.updateGame(nonExistent);
        GameData retrieved = gameDAO.getGame(999);
        assertNull(retrieved);
    }
}
