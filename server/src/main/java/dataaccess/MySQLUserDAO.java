package dataaccess;

import model.UserData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String dropSql = "DROP TABLE IF EXISTS user";
            try (PreparedStatement dropPs = conn.prepareStatement(dropSql)) {
                dropPs.executeUpdate();
            }
            String createSql = "CREATE TABLE IF NOT EXISTS user (" +
                    "username VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL" +
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
            String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.username());
                ps.setString(2, user.password());
                ps.setString(3, user.email());
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
