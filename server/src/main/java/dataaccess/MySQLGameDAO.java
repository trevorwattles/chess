package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameDAO implements GameDAO {
    private static final Gson gson = new Gson();

    public MySQLGameDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String createSql = "CREATE TABLE IF NOT EXISTS game (" +
                    "game_id INT PRIMARY KEY, " +
                    "white_username VARCHAR(255) NOT NULL, " +
                    "black_username VARCHAR(255), " +
                    "game_name VARCHAR(255) NOT NULL, " +
                    "game_state TEXT NOT NULL" +
                    ")";
            try (PreparedStatement createPs = conn.prepareStatement(createSql)) {
                createPs.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error initializing game table: " + e.getMessage());
        }
    }


    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO game (game_id, white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, game.gameID());
                ps.setString(2, game.whiteUsername());
                ps.setString(3, game.blackUsername());
                ps.setString(4, game.gameName());
                ps.setString(5, gson.toJson(game));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting game record: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM game WHERE game_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("game_id");
                        String white = rs.getString("white_username");
                        String black = rs.getString("black_username");
                        String gameName = rs.getString("game_name");
                        String gameState = rs.getString("game_state");
                        ChessGame chessGame = gson.fromJson(gameState, ChessGame.class);
                        return new GameData(id, white, black, gameName, chessGame);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game record: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM game";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("game_id");
                        String white = rs.getString("white_username");
                        String black = rs.getString("black_username");
                        String gameName = rs.getString("game_name");
                        String gameState = rs.getString("game_state");
                        ChessGame chessGame = gson.fromJson(gameState, ChessGame.class);
                        games.add(new GameData(id, white, black, gameName, chessGame));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing game records: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE game SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, game.gameName());
                ps.setString(4, gson.toJson(game.game()));
                ps.setInt(5, game.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game record: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM game";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing game table: " + e.getMessage());
        }
    }
}
