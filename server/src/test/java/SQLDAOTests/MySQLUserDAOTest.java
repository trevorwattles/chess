package SQLDAOTests;

import dataaccess.MySQLUserDAO;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLUserDAOTest {

    private MySQLUserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void testCreateUser_Positive() {
        UserData user = new UserData("john", "password123", "john@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user), "Creating a new user should not throw an exception");

        UserData retrieved = null;
        try {
            retrieved = userDAO.getUser("john");
        } catch (DataAccessException e) {
            fail("Exception thrown while retrieving user: " + e.getMessage());
        }
        assertNotNull(retrieved, "User should be retrieved after creation");
        assertEquals("john", retrieved.username());
        assertEquals("password123", retrieved.password());
        assertEquals("john@example.com", retrieved.email());
    }

    @Test
    public void testCreateUser_Negative_Duplicate() {
        UserData user = new UserData("alice", "pass", "alice@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user), "Creating the user the first time should succeed");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
        }, "Creating a duplicate user should throw a DataAccessException");
        String expectedMessage = "Error inserting user record";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage),
                "Expected exception message to contain '" + expectedMessage + "'");
    }

    @Test
    public void testGetUser_Positive() {
        UserData user = new UserData("bob", "secret", "bob@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user), "User creation should not throw an exception");

        UserData retrieved = null;
        try {
            retrieved = userDAO.getUser("bob");
        } catch (DataAccessException e) {
            fail("Exception thrown while retrieving user: " + e.getMessage());
        }
        assertNotNull(retrieved, "Retrieved user should not be null");
        assertEquals("bob", retrieved.username());
        assertEquals("secret", retrieved.password());
        assertEquals("bob@example.com", retrieved.email());
    }

    @Test
    public void testGetUser_Negative_NotFound() {
        try {
            UserData retrieved = userDAO.getUser("nonexistent");
            assertNull(retrieved, "Retrieving a non-existent user should return null");
        } catch (DataAccessException e) {
            fail("Exception thrown while retrieving non-existent user: " + e.getMessage());
        }
    }
}
