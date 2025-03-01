package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import spark.Request;
import spark.Response;

import dataaccess.DataAccessException;
import dataaccess.RequestException;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.userService = new UserService(userDAO, authDAO);
    }

    public Object register(Request req, Response res) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);
            AuthData authData = userService.createUser(userData);
            res.status(200);
            return gson.toJson(authData);
        } catch (RequestException e) {
            if (e.getMessage().equals("Error: already taken")) {
                res.status(403); // Forbidden when username is already taken
            } else {
                res.status(400); // Bad request for other input issues
            }
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public Object login(Request req, Response res) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);
            AuthData authData = userService.loginUser(userData);
            res.status(200);
            return gson.toJson(authData);
        } catch (RequestException e) {
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            userService.logoutUser(authToken);
            res.status(200);
            return gson.toJson(Map.of("message", "Logout successful"));
        } catch (DataAccessException e) {
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}
