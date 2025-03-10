package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    // Constructor: Drops (for testing) and creates the user table if it doesn't exist.
    public MySQLUserDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Drop the table if it already exists (useful in a test environment)
            String dropSql = "DROP TABLE IF EXISTS user";
            try (PreparedStatement dropPs = conn.prepareStatement(dropSql)) {
                dropPs.executeUpdate();
            }
            // Create the user table with the desired schema.
            // Here we assume two columns: username as the primary key and password.
            String createSql = "CREATE TABLE IF NOT EXISTS user (" +
                    "username VARCHAR(255) PRIMARY KEY," +
                    "password VARCHAR(255) NOT NULL" +
                    ")";
            try (PreparedStatement createPs = conn.prepareStatement(createSql)) {
                createPs.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error initializing user table: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM user";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing user table: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO user (username, password) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.username());
                ps.setString(2, user.password());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user record: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT username, password, email FROM user WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user record: " + e.getMessage());
        }
        return null;
    }

}
