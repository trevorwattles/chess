package server;

import dataaccess.*;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.api.Session;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.concurrent.ConcurrentHashMap;


public class Server {


    public static UserService userService;
    public static GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;
    ClearHandler clearHandler;

    AuthDAO authDAO;
    GameDAO gameDAO;
    UserDAO userDAO;


    public static ConcurrentHashMap<Session, Integer> gameSessionsMap = new ConcurrentHashMap<>();

    public Server() {
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
        this.userDAO = new MySQLUserDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        this.userHandler = new UserHandler(userDAO, authDAO);
        this.clearHandler = new ClearHandler(userDAO, authDAO, gameDAO);
        this.gameHandler = new GameHandler(gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", server.handlers.WebsocketHandler.class);

        Spark.delete("/db", clearHandler::clear);
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
