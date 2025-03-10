package SQLDAOTests;

import dataaccess.MySQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLAuthDAOTest {

    private MySQLAuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        // Initialize the DAO and clear the auth table before each test.
        authDAO = new MySQLAuthDAO();
        authDAO.clear();
    }

    @Test
    public void testClear_Positive() {
        // Insert an auth record and then clear the table.
        AuthData auth = new AuthData("token123", "user123");
        authDAO.createAuth(auth);
        // Ensure the record exists.
        assertNotNull(authDAO.getAuth("token123"), "Auth record should exist before clearing.");

        // Clear the table.
        authDAO.clear();
        // The record should now be gone.
        assertNull(authDAO.getAuth("token123"), "Auth record should be null after clearing the table.");
    }

    @Test
    public void testCreateAuth_Positive() {
        AuthData auth = new AuthData("tokenPositive", "userPositive");
        authDAO.createAuth(auth);
        AuthData retrieved = authDAO.getAuth("tokenPositive");
        assertNotNull(retrieved, "Auth record should be retrieved after creation.");
        assertEquals("tokenPositive", retrieved.authToken());
        assertEquals("userPositive", retrieved.username());
    }

    @Test
    public void testCreateAuth_Negative_Duplicate() {
        AuthData auth = new AuthData("duplicateToken", "userDuplicate");
        authDAO.createAuth(auth);
        // Attempt to insert the same auth token twice.
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authDAO.createAuth(auth);
        }, "Creating a duplicate auth should throw an exception.");
        // Optionally, check that the exception message indicates an error on insertion.
        assertTrue(exception.getMessage().contains("Error inserting auth record"));
    }

    @Test
    public void testGetAuth_Positive() {
        AuthData auth = new AuthData("tokenGet", "userGet");
        authDAO.createAuth(auth);
        AuthData retrieved = authDAO.getAuth("tokenGet");
        assertNotNull(retrieved, "Should retrieve an auth record that exists.");
        assertEquals("tokenGet", retrieved.authToken());
        assertEquals("userGet", retrieved.username());
    }

    @Test
    public void testGetAuth_Negative_NotFound() {
        AuthData retrieved = authDAO.getAuth("nonExistentToken");
        assertNull(retrieved, "Retrieving a non-existent auth record should return null.");
    }

    @Test
    public void testDeleteAuth_Positive() {
        AuthData auth = new AuthData("tokenDelete", "userDelete");
        authDAO.createAuth(auth);
        // Ensure it exists before deletion.
        assertNotNull(authDAO.getAuth("tokenDelete"), "Auth record should exist before deletion.");

        authDAO.deleteAuth(auth);
        // After deletion, it should no longer exist.
        assertNull(authDAO.getAuth("tokenDelete"), "Auth record should be null after deletion.");
    }

    @Test
    public void testDeleteAuth_Negative_NonExistent() {
        // Deleting an auth record that doesn't exist should not throw an exception.
        AuthData auth = new AuthData("nonExistent", "noUser");
        assertDoesNotThrow(() -> authDAO.deleteAuth(auth), "Deleting a non-existent auth should not throw an exception.");
    }
}
