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


    public void updateGame(String authToken, GameData gameData)  {

    }

    public int createGame(String authToken, String gameName){
        return 0;
    }

    public boolean joinGame(String authToken, int gameID, String color) {
        return false;
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
}
