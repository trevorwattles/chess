package client;

import dataaccess.DataAccessException;
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
        Assertions.assertTrue(true);
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

        // First registration should succeed
        var auth = serverFacade.register(request);
        Assertions.assertNotNull(auth);

        // Second registration with same username should fail
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.register(request);
        });
    }
    @Test
    public void testLogin_Success() throws ResponseException {
        // Register first
        serverFacade.register(new RegisterRequest("loginUser", "securePass123", "login@example.com"));

        // Create LoginRequest and call login
        LoginRequest loginRequest = new LoginRequest("loginUser", "securePass123");
        var auth = serverFacade.login(loginRequest);

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void testLogin_WrongPassword() throws ResponseException {
        serverFacade.register(new RegisterRequest("badLoginUser", "rightPassword", "badlogin@example.com"));

        LoginRequest loginRequest = new LoginRequest("badLoginUser", "wrongPassword");

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(loginRequest);
        });
    }

    @Test
    public void testLogin_NonexistentUser() {
        LoginRequest loginRequest = new LoginRequest("ghostUser", "doesntMatter");

        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(loginRequest);
        });
    }





}


