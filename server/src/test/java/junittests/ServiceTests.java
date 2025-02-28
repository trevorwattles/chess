package junittests;

import dataaccess.DataAccessException;
import dataaccess.RequestException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        userService.clear();
    }

    @Test
    public void testCreateUser_Success() throws RequestException, DataAccessException {

        UserData newUser = new UserData("testUser", "password123", "test@example.com");

        AuthData authData = userService.createUser(newUser);

        assertNotNull(authData);
        assertEquals("testUser", authData.username());
        assertNotNull(authData.authToken());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    public void testCreateUser_Fail_NullInput() {
        RequestException thrown = assertThrows(RequestException.class, () -> {
            userService.createUser(null);
        });

        assertEquals("Error: bad request", thrown.getMessage());
    }

    @Test
    public void testCreateUser_Fail_MissingFields() {
        UserData userWithoutUsername = new UserData(null, "password123", "email@example.com");
        RequestException thrown1 = assertThrows(RequestException.class, () -> {
            userService.createUser(userWithoutUsername);
        });
        assertEquals("Error: bad request", thrown1.getMessage());

        UserData userWithoutPassword = new UserData("testUser", null, "email@example.com");
        RequestException thrown2 = assertThrows(RequestException.class, () -> {
            userService.createUser(userWithoutPassword);
        });
        assertEquals("Error: bad request", thrown2.getMessage());

        UserData userWithoutEmail = new UserData("testUser", "password123", null);
        RequestException thrown3 = assertThrows(RequestException.class, () -> {
            userService.createUser(userWithoutEmail);
        });
        assertEquals("Error: bad request", thrown3.getMessage());
    }

    @Test
    public void testCreateUser_Fail_UsernameTaken() throws RequestException, DataAccessException {
        UserData existingUser = new UserData("existingUser", "password123", "existing@example.com");
        userService.createUser(existingUser);

        RequestException thrown = assertThrows(RequestException.class, () -> {
            userService.createUser(existingUser);
        });

        assertEquals("Error: already taken", thrown.getMessage());
    }
    @Test
    public void testLoginUser_Success() throws RequestException, DataAccessException {
        // Given a valid registered user
        UserData newUser = new UserData("testUser", "password123", "test@example.com");
        userService.createUser(newUser);

        // When logging in with correct credentials
        AuthData authData = userService.loginUser(new UserData("testUser", "password123", null));

        // Then it should return valid auth data
        assertNotNull(authData);
        assertEquals("testUser", authData.username());
        assertNotNull(authData.authToken());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    public void testLoginUser_Fail_UserNotFound() {
        // When attempting to login with a non-existent user
        RequestException thrown = assertThrows(RequestException.class, () -> {
            userService.loginUser(new UserData("nonExistentUser", "password123", null));
        });

        // Then it should return "Error: unauthorized"
        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testLoginUser_Fail_IncorrectPassword() throws RequestException, DataAccessException {
        // Given a valid registered user
        UserData newUser = new UserData("testUser", "correctPassword", "test@example.com");
        userService.createUser(newUser);

        // When attempting to login with an incorrect password
        RequestException thrown = assertThrows(RequestException.class, () -> {
            userService.loginUser(new UserData("testUser", "wrongPassword", null));
        });

        // Then it should return "Error: unauthorized"
        assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testLoginUser_Fail_MissingFields() {
        // When missing a username
        RequestException thrown1 = assertThrows(RequestException.class, () -> {
            userService.loginUser(new UserData(null, "password123", null));
        });
        assertEquals("Error: bad request", thrown1.getMessage());

        // When missing a password
        RequestException thrown2 = assertThrows(RequestException.class, () -> {
            userService.loginUser(new UserData("testUser", null, null));
        });
        assertEquals("Error: bad request", thrown2.getMessage());
    }
}
