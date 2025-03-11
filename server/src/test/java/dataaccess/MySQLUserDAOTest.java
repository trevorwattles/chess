package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;

public class MySQLUserDAOTest {

    private MySQLUserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void testCreateUserPositive() {
        UserData user = new UserData("john", "password123", "john@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user), "Creating a new user should not throw an exception");
        UserData retrieved = null;
        try {
            retrieved = userDAO.getUser("john");
        } catch (DataAccessException e) {
            fail("Exception thrown when retrieving user: " + e.getMessage());
        }
        assertNotNull(retrieved, "User should be retrieved after creation");
        assertEquals("john", retrieved.username());
        assertNotEquals("password123", retrieved.password(), "Stored password should not be clear text");
        assertTrue(BCrypt.checkpw("password123", retrieved.password()), "Stored password hash does not match the clear text password");
        assertEquals("john@example.com", retrieved.email());
    }

    @Test
    public void testGetUserNegativeNotFound() {
        try {
            UserData retrieved = userDAO.getUser("nonexistent");
            assertNull(retrieved, "Retrieving a non-existent user should return null");
        } catch (DataAccessException e) {
            fail("Exception thrown when retrieving non-existent user: " + e.getMessage());
        }
    }

    @Test
    public void testCreateUserNegativeDuplicate() {
        UserData user = new UserData("alice", "pass", "alice@example.com");
        assertDoesNotThrow(() -> userDAO.createUser(user), "Creating the user the first time should succeed");
        Exception exception = assertThrows(DataAccessException.class, () -> userDAO.createUser(user),
                "Creating a duplicate user should throw a DataAccessException");
        String expectedMessage = "Error inserting user record";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage),
                "Expected exception message to contain '" + expectedMessage + "'");
    }
}
