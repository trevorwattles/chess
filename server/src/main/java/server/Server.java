package server;

import dataaccess.*;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import service.ClearService;
import spark.*;

public class Server {

    UserHandler userHandler;
    GameHandler gameHandler;
    ClearHandler clearHandler;

    AuthDAO authDAO;
    GameDAO gameDAO;
    UserDAO userDAO;

    public Server() {
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
        this.userDAO = new MySQLUserDAO();

        this.userHandler = new UserHandler(userDAO, authDAO);
        this.clearHandler = new ClearHandler(userDAO, authDAO, gameDAO);
        this.gameHandler = new GameHandler(gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

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
