package server.handlers;
import com.google.gson.Gson;
import model.AuthData;
import spark.Request;
import spark.Response;

import dataaccess.RequestException;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();
    public UserHandler() {
        this.userService = new UserService();
    }

    public Object register(Request req, Response res) throws RequestException {
        return null;
    }
    public Object login(Request request, Response response) throws RequestException {
        return null;
    }
    public Object logout(Request request, Response response) throws RequestException {
        return null;
    }

}
