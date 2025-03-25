package client;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import server.request.RegisterRequest;
import server.request.LoginRequest;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearServer() throws ResponseException {
        serverFacade.clear();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);  // A simple placeholder test
    }

    @Test
    public void testRegisterSuccess() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("testUser1", "password123", "Test@gmail.com");
        var auth = serverFacade.register(registerRequest);
        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void testRegisterDuplicateUsername() throws ResponseException {
        RegisterRequest request = new RegisterRequest("duplicateUser", "password123", "test@example.com");

        var auth = serverFacade.register(request);
        Assertions.assertNotNull(auth);

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.register(request);
        });
    }

    @Test
    public void testLoginSuccess() throws ResponseException {
        serverFacade.register(new RegisterRequest("loginUser", "securePass123", "login@example.com"));

        LoginRequest loginRequest = new LoginRequest("loginUser", "securePass123");
        var auth = serverFacade.login(loginRequest);

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void testLoginWrongPassword() throws ResponseException {
        serverFacade.register(new RegisterRequest("badLoginUser", "rightPassword", "badlogin@example.com"));

        LoginRequest loginRequest = new LoginRequest("badLoginUser", "wrongPassword");

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(loginRequest);
        });
    }

    @Test
    public void testLoginNonexistentUser() {
        LoginRequest loginRequest = new LoginRequest("ghostUser", "doesntMatter");

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(loginRequest);
        });
    }

    @Test
    public void testLogoutAfterLogin() throws ResponseException {
        serverFacade.register(new RegisterRequest("logoutTestUser", "password", "logout@user.com"));

        serverFacade.login(new LoginRequest("logoutTestUser", "password"));

        Assertions.assertDoesNotThrow(() -> {
            serverFacade.logout();
        });
    }

    @Test
    public void testLogoutWithoutLogin() {
        ResponseException exception = Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.logout();
        });
        Assertions.assertEquals(401, exception.getStatusCode());
    }



    @Test
    public void testCreateGameSuccess() throws ResponseException {
        serverFacade.register(new RegisterRequest("creatorUser", "password", "creator@example.com"));
        serverFacade.login(new LoginRequest("creatorUser", "password"));

        GameData game = serverFacade.createGame("My Cool Game");
        Assertions.assertNotNull(game);
        Assertions.assertTrue(game.gameID() > 0);
        Assertions.assertEquals("My Cool Game", game.gameName());
    }
    @Test
    public void testCreateGameWithoutLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.createGame("Unauthorized Game");
        });
    }
    @Test
    public void testListGamesSuccess() throws ResponseException {
        serverFacade.register(new RegisterRequest("listUser", "pass123", "list@user.com"));
        serverFacade.login(new LoginRequest("listUser", "pass123"));

        serverFacade.createGame("Test Game");

        var games = serverFacade.listGames();

        Assertions.assertNotNull(games);
        Assertions.assertFalse(games.isEmpty(), "Game list should not be empty");
        Assertions.assertEquals("Test Game", games.iterator().next().gameName());
    }
    @Test
    public void testListGamesAfterBadCreateGame() throws ResponseException {
        serverFacade.register(new RegisterRequest("badGameUser", "pass123", "bad@game.com"));
        serverFacade.login(new LoginRequest("badGameUser", "pass123"));

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.createGame("");
        });

        var games = serverFacade.listGames();
        Assertions.assertTrue(games.isEmpty(), "No games should exist after a failed creation");
    }
    @Test
    public void testJoinGameSuccess() throws ResponseException {
        serverFacade.register(new RegisterRequest("joinUser", "password", "join@game.com"));
        serverFacade.login(new LoginRequest("joinUser", "password"));

        GameData createdGame = serverFacade.createGame("Joinable Game");
        int gameID = createdGame.gameID();

        Assertions.assertDoesNotThrow(() -> {
            serverFacade.joinGame(gameID, "WHITE");
        });
    }

    @Test
    public void testJoinGameWithInvalidGameID() throws ResponseException {
        serverFacade.register(new RegisterRequest("badGameUser", "password", "bad@game.com"));
        serverFacade.login(new LoginRequest("badGameUser", "password"));

        int invalidGameID = 99999;

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(invalidGameID, "WHITE");
        });
    }
    @Test
    public void testObserveGameSuccess() throws ResponseException {
        serverFacade.register(new RegisterRequest("observer", "pass", "obs@example.com"));
        serverFacade.login(new LoginRequest("observer", "pass"));
        serverFacade.createGame("A Game to Observe");

        Assertions.assertDoesNotThrow(() -> serverFacade.observeGame(1));
    }
    @Test
    public void testObserveNonexistentGame() throws ResponseException {
        serverFacade.register(new RegisterRequest("observerFail", "pass", "fail@obs.com"));
        serverFacade.login(new LoginRequest("observerFail", "pass"));

        Assertions.assertThrows(ResponseException.class, () -> serverFacade.observeGame(999));
    }


}
