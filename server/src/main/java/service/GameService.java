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

    public GameData getGameData(String authToken, int gameID) {

    }

    public void updateGame(String authToken, GameData gameData)  {

    }

    public int createGame(String authToken, String gameName){
    }

    public boolean joinGame(String authToken, int gameID, String color) {
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
}
