package server.handlers;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import spark.Request;
import spark.Response;
import service.GameService;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.Map;
import java.util.Set;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final AuthDAO authDAO;

    public GameHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameService = new GameService(gameDAO, authDAO);
        this.authDAO = authDAO;
    }

    public Object listGames(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                resp.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }
            if (authDAO.getAuth(authToken) == null) {
                resp.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized - invalid token"));
            }
            Set<GameData> games = gameService.listGames(authToken);
            resp.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (DataAccessException e) {
            resp.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }



    public Object createGame(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                resp.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            Map<String, String> requestBody = gson.fromJson(req.body(), Map.class);
            String gameName = requestBody.get("gameName");
            if (gameName == null || gameName.trim().isEmpty()) {
                resp.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            int gameID = gameService.createGame(authToken, gameName);

            GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
            resp.status(200);
            return gson.toJson(newGame);

        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                resp.status(401);
            } else {
                resp.status(500);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }


    public Object joinGame(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                resp.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            Map<String, Object> requestBody = gson.fromJson(req.body(), Map.class);
            String playerColor = (String) requestBody.get("playerColor");
            Double gameIDDouble = (Double) requestBody.get("gameID");
            if (playerColor == null || gameIDDouble == null) {
                resp.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            if (!playerColor.equalsIgnoreCase("WHITE") &&
                    !playerColor.equalsIgnoreCase("BLACK") &&
                    !playerColor.equalsIgnoreCase("OBSERVER")) {
                resp.status(400);
                return gson.toJson(Map.of("message", "Error: invalid color choice"));
            }


            int gameID = gameIDDouble.intValue();
            boolean success = gameService.joinGame(authToken, gameID, playerColor);

            if (success) {
                resp.status(200);
                return gson.toJson(Map.of()); // Empty JSON object for success
            } else {
                resp.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                resp.status(401);
            } else if (e.getMessage().contains("already taken")) {
                resp.status(403);
            } else {
                resp.status(500);
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}
