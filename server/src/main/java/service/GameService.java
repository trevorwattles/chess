package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final MemoryGameDAO gameDAO;
    private final MemoryAuthDAO authDAO;

    public GameService() {
        this.gameDAO = new MemoryGameDAO();
        this.authDAO = new MemoryAuthDAO();
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (gameName == null || gameName.trim().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // Create a game without using ChessGame
        GameData newGame = new GameData(0, null, null, gameName, null);
        gameDAO.createGame(newGame);

        return newGame.gameID();
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return gameDAO.listGames();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = authDAO.getAuth(authToken).username();
        if ("WHITE".equals(playerColor) && game.whiteUsername() == null) {
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), null);
        } else if ("BLACK".equals(playerColor) && game.blackUsername() == null) {
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), null);
        } else {
            throw new DataAccessException("Error: already taken");
        }

        gameDAO.updateGame(game);
    }
}
