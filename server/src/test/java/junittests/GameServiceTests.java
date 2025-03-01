package junittests;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        gameDAO.clear();
        authDAO.clear();
    }


    @Test
    public void testListGames_Success() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        gameDAO.createGame(new GameData(1, "player1", "player2", "Game1", null));
        gameDAO.createGame(new GameData(2, "player3", null, "Game2", null));

        HashSet<GameData> result = gameService.listGames(validToken);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testListGames_Fail_InvalidAuthToken() {
        String invalidToken = "invalidToken";

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(invalidToken);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testListGames_Success_EmptyGamesList() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        HashSet<GameData> result = gameService.listGames(validToken);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListGames_Fail_NullAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(null);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testListGames_Fail_EmptyAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("");
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }
    @Test
    public void testGetGameData_Success() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        GameData game = new GameData(1, "player1", "player2", "Chess Match", null);
        gameDAO.createGame(game);

        GameData result = gameService.getGameData(validToken, 1);

        assertNotNull(result);
        assertEquals(1, result.gameID());
        assertEquals("Chess Match", result.gameName());
        assertEquals("player1", result.whiteUsername());
        assertEquals("player2", result.blackUsername());
    }

    @Test
    public void testGetGameData_Fail_InvalidAuthToken() {
        String invalidToken = "invalidToken";

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(invalidToken, 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testGetGameData_Fail_GameNotFound() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(validToken, 99);
        });

        assertEquals("Game data not found", thrown.getMessage());
    }

    @Test
    public void testGetGameData_Fail_NullAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(null, 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testGetGameData_Fail_EmptyAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData("", 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }
    @Test
    public void testUpdateGame_Success() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        GameData originalGame = new GameData(1, "player1", "player2", "Chess Match", null);
        gameDAO.createGame(originalGame);

        GameData updatedGame = new GameData(1, "player1", "player2", "Updated Chess Match", null);
        gameService.updateGame(validToken, updatedGame);

        GameData result = gameDAO.getGame(1);

        assertNotNull(result);
        assertEquals(1, result.gameID());
        assertEquals("Updated Chess Match", result.gameName());
    }

    @Test
    public void testUpdateGame_Fail_InvalidAuthToken() {
        String invalidToken = "invalidToken";
        GameData updatedGame = new GameData(1, "player1", "player2", "Updated Chess Match", null);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(invalidToken, updatedGame);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testUpdateGame_Fail_GameNotFound() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        GameData nonExistentGame = new GameData(99, "player1", "player2", "Non-Existent Game", null);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(validToken, nonExistentGame);
        });

        assertEquals("Game data not found", thrown.getMessage());
    }

    @Test
    public void testUpdateGame_Fail_NullGameData() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(validToken, null);
        });

        assertEquals("Error: invalid game data", thrown.getMessage());
    }

    @Test
    public void testUpdateGame_Fail_InvalidGameID() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        GameData invalidGame = new GameData(-1, "player1", "player2", "Invalid Game", null);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(validToken, invalidGame);
        });

        assertEquals("Error: invalid game data", thrown.getMessage());
    }


}
