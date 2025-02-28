package server.handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;
import service.ClearService;
import dataaccess.DataAccessException;

import java.util.HashMap;
import java.util.Map;

public class ClearHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        ClearService clearService = new ClearService();

        try {
            // Call the service to clear all data
            clearService.clear();

            // Set HTTP response status to 200 (OK)
            res.status(200);
            return new Gson().toJson(new HashMap<>()); // Return an empty JSON object `{}`

        } catch (DataAccessException e) {
            // Handle errors by returning HTTP 500 and an error message
            res.status(500);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error: " + e.getMessage());
            return new Gson().toJson(errorResponse);
        }
    }
}

