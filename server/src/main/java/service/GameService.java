package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }
    public HashSet<GameData> listGames(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        List<GameData> games = gameDAO.listGames();
        return new HashSet<>(games);
    }

    public GameData getGameData(String authToken, int gameID) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        return game;
    }


    public void updateGame(String authToken, GameData gameData) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (gameData == null || gameData.gameID() <= 0) {
            throw new DataAccessException("Error: invalid game data");
        }

        GameData existingGame = gameDAO.getGame(gameData.gameID());
        if (existingGame == null) {
            throw new DataAccessException("Error: game not found");
        }

        gameDAO.updateGame(gameData);
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (gameName == null || gameName.trim().isEmpty()) {
            throw new DataAccessException("Error: invalid game name");
        }

        GameData newGame = new GameData(0, authData.username(), null, gameName, null);
        gameDAO.createGame(newGame);

        List<GameData> allGames = gameDAO.listGames();
        int maxGameID = allGames.stream().mapToInt(GameData::gameID)
                .max().orElseThrow(() -> new DataAccessException("Error: failed to create game"));

        return maxGameID;
    }





    public boolean joinGame(String authToken, int gameID, String color) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }

        if (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color)) {
            throw new DataAccessException("Error: invalid color choice");
        }

        if ("WHITE".equalsIgnoreCase(color)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: white seat already taken");
            }
            game = new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: black seat already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game());
        }

        gameDAO.updateGame(game);

        return true;
    }


    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
}
