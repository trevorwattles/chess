package server.handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import service.GameService;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHandler implements Route {
    private final GameService gameService;

    public GameHandler() {
        this.gameService = new GameService();
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String path = req.pathInfo();
            Map<String, String> requestData = new Gson().fromJson(req.body(), Map.class);
            String authToken = req.headers("Authorization");

            if ("/game".equals(path) && "POST".equals(req.requestMethod())) {
                // Create a new game
                String gameName = requestData.get("gameName");
                int gameID = gameService.createGame(authToken, gameName);

                res.status(200);
                return new Gson().toJson(Map.of("gameID", gameID));

            } else if ("/game".equals(path) && "GET".equals(req.requestMethod())) {
                // List all games
                List<GameData> games = gameService.listGames(authToken);
                res.status(200);
                return new Gson().toJson(Map.of("games", games));

            } else if ("/game/join".equals(path) && "POST".equals(req.requestMethod())) {
                // Join a game
                int gameID = Integer.parseInt(requestData.get("gameID"));
                String playerColor = requestData.get("playerColor");

                gameService.joinGame(authToken, gameID, playerColor);
                res.status(200);
                return "{}"; // Empty JSON response for success
            }

            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: Bad request"));

        } catch (DataAccessException e) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
