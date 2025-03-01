package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.RequestException;
import spark.Request;
import spark.Response;
import spark.Route;
import service.GameService;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameService = new GameService(gameDAO, authDAO);
    }
    public Object listGames(Request req, Response resp) throws Exception {
        return null;
    }
    public Object createGame(Request req, Response resp) throws RequestException, Exception {
        return null;
    }
    public Object joinGame(Request req, Response resp) throws RequestException, Exception {
        return null;
    }

}
