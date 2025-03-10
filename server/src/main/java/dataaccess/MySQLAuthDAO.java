package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Drop the table if it already exists (for testing purposes)
            String dropSql = "DROP TABLE IF EXISTS auth";
            try (PreparedStatement dropPs = conn.prepareStatement(dropSql)) {
                dropPs.executeUpdate();
            }

            // Create the table with the correct schema
            String createSql = "CREATE TABLE IF NOT EXISTS auth (" +
                    "auth_token VARCHAR(255) PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL" +
                    ")";
            try (PreparedStatement createPs = conn.prepareStatement(createSql)) {
                createPs.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing auth table: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM auth";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing auth table: " + e.getMessage());
        }
    }

    @Override
    public void createAuth(AuthData auth) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO auth (auth_token, username) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username()); // Ensure you bind the username value!
                ps.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error inserting auth record: " + e.getMessage());
        }
    }


    @Override
    public AuthData getAuth(String authToken) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT auth_token, username FROM auth WHERE auth_token = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("auth_token"), rs.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving auth record: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM auth WHERE auth_token = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, authData.authToken());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting auth record: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
