package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
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


}
