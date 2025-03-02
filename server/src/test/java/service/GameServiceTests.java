package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

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
    public void testListGamesSuccess() throws DataAccessException {
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
    public void testListGamesFailInvalidAuthToken() {
        String invalidToken = "invalidToken";

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(invalidToken);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testListGamesSuccessEmptyGamesList() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        HashSet<GameData> result = gameService.listGames(validToken);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListGamesFailNullAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames(null);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testListGamesFailEmptyAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("");
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }
    @Test
    public void testGetGameDataSuccess() throws DataAccessException {
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
    public void testGetGameDataFailInvalidAuthToken() {
        String invalidToken = "invalidToken";

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(invalidToken, 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testGetGameDataFailGameNotFound() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(validToken, 99);
        });

        assertEquals("Game data not found", thrown.getMessage());
    }

    @Test
    public void testGetGameDataFailNullAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData(null, 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testGetGameDataFailEmptyAuthToken() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.getGameData("", 1);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }
    @Test
    public void testUpdateGameSuccess() throws DataAccessException {
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
    public void testUpdateGameFailInvalidAuthToken() {
        String invalidToken = "invalidToken";
        GameData updatedGame = new GameData(1, "player1", "player2", "Updated Chess Match", null);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(invalidToken, updatedGame);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testUpdateGameFailGameNotFound() throws DataAccessException {
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
    public void testUpdateGameFailNullGameData() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(validToken, null);
        });

        assertEquals("Error: invalid game data", thrown.getMessage());
    }

    @Test
    public void testUpdateGameFailInvalidGameID() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        GameData invalidGame = new GameData(-1, "player1", "player2", "Invalid Game", null);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame(validToken, invalidGame);
        });

        assertEquals("Error: invalid game data", thrown.getMessage());
    }
    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        String gameName = "New Chess Game";
        int gameID = gameService.createGame(validToken, gameName);

        GameData createdGame = gameDAO.getGame(gameID);

        assertNotNull(createdGame);
        assertEquals(gameID, createdGame.gameID());
        assertEquals(gameName, createdGame.gameName());
        assertNull(createdGame.whiteUsername());
        assertNull(createdGame.blackUsername());
    }

    @Test
    public void testCreateGameFailInvalidAuthToken() {
        String invalidToken = "invalidToken";
        String gameName = "Invalid Game";

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(invalidToken, gameName);
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testCreateGameFailNullGameName() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(validToken, null);
        });

        assertEquals("Error: invalid game name", thrown.getMessage());
    }

    @Test
    public void testCreateGameFailEmptyGameName() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(validToken, "  ");
        });

        assertEquals("Error: invalid game name", thrown.getMessage());
    }

    @Test
    public void testCreateGameFailDuplicateGameNamesAllowed() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        String gameName = "Chess Match";

        int gameID1 = gameService.createGame(validToken, gameName);
        System.out.println("Game 1 created with ID: " + gameID1);

        int gameID2 = gameService.createGame(validToken, gameName);
        System.out.println("Game 2 created with ID: " + gameID2);

        // Fetch and print all stored games
        List<GameData> allGames = gameDAO.listGames();
        System.out.println("Total games stored: " + allGames.size());
        for (GameData game : allGames) {
            System.out.println("Game ID: " + game.gameID() + ", Name: " + game.gameName());
        }

        assertNotEquals(gameID1, gameID2, "Game IDs should be unique even if the names are the same");
    }
    @Test
    public void testJoinGameSuccessWhite() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        String gameName = "Chess Match";
        int gameID = gameService.createGame(validToken, gameName);

        boolean joined = gameService.joinGame(validToken, gameID, "WHITE");

        GameData updatedGame = gameDAO.getGame(gameID);

        assertTrue(joined);
        assertNotNull(updatedGame);
        assertEquals(username, updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    public void testJoinGameSuccessBlack() throws DataAccessException {
        String validToken = "validToken456";
        String username = "testUser2";
        authDAO.createAuth(new AuthData(validToken, username));

        String gameName = "Chess Game 2";
        int gameID = gameService.createGame(validToken, gameName);

        boolean joined = gameService.joinGame(validToken, gameID, "BLACK");

        GameData updatedGame = gameDAO.getGame(gameID);

        assertTrue(joined);
        assertNotNull(updatedGame);
        assertEquals(username, updatedGame.blackUsername());
        assertNull(updatedGame.whiteUsername());
    }

    @Test
    public void testJoinGameFailInvalidAuthToken() {
        String invalidToken = "invalidToken";
        int gameID = 1;

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(invalidToken, gameID, "WHITE");
        });

        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testJoinGameFailGameNotFound() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        int nonExistentGameID = 999;

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(validToken, nonExistentGameID, "WHITE");
        });

        assertEquals("Game data not found", thrown.getMessage());
    }

    @Test
    public void testJoinGameFailInvalidColor() throws DataAccessException {
        String validToken = "validToken123";
        String username = "testUser";
        authDAO.createAuth(new AuthData(validToken, username));

        String gameName = "Chess Match";
        int gameID = gameService.createGame(validToken, gameName);

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(validToken, gameID, "BLUE");
        });

        assertEquals("Error: invalid color choice", thrown.getMessage());
    }

    @Test
    public void testJoinGameFailSeatAlreadyTaken() throws DataAccessException {
        String token1 = "token1";
        String token2 = "token2";
        String user1 = "player1";
        String user2 = "player2";

        authDAO.createAuth(new AuthData(token1, user1));
        authDAO.createAuth(new AuthData(token2, user2));

        String gameName = "Chess Game";
        int gameID = gameService.createGame(token1, gameName);

        gameService.joinGame(token1, gameID, "WHITE");

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(token2, gameID, "WHITE");
        });

        assertEquals("Error: white seat already taken", thrown.getMessage());
    }

    @Test
    public void testJoinGameFailBothSeatsTaken() throws DataAccessException {
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";
        String user1 = "player1";
        String user2 = "player2";
        String user3 = "player3";

        authDAO.createAuth(new AuthData(token1, user1));
        authDAO.createAuth(new AuthData(token2, user2));
        authDAO.createAuth(new AuthData(token3, user3));

        String gameName = "Chess Game";
        int gameID = gameService.createGame(token1, gameName);

        gameService.joinGame(token1, gameID, "WHITE");
        gameService.joinGame(token2, gameID, "BLACK");

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(token3, gameID, "WHITE");
        });

        assertEquals("Error: white seat already taken", thrown.getMessage());

        thrown = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(token3, gameID, "BLACK");
        });

        assertEquals("Error: black seat already taken", thrown.getMessage());
    }



}
