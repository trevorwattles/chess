package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLAuthDAO implements AuthDAO {

    // Constructor to ensure the auth table exists
    public MySQLAuthDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Create the auth table if it doesn't exist.
            // Here we assume the auth table has one column: auth_token as the primary key.
            // You can add more columns as needed.
            String sql = "CREATE TABLE IF NOT EXISTS auth (" +
                    "auth_token VARCHAR(255) PRIMARY KEY" +
                    ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            // You may want to throw a custom DataAccessException here.
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
            String sql = "INSERT INTO auth (auth_token) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, auth.authToken());
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
