package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import spark.Request;
import spark.Response;
import service.ClearService;
import dataaccess.DataAccessException;

import java.util.HashMap;
import java.util.Map;

public class ClearHandler {
    private final ClearService clearService;
    public ClearHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    public Object clear(Request req, Response res) throws DataAccessException {
            clearService.clear();

            res.status(200);
            return new Gson().toJson(new HashMap<>());
    }
}

