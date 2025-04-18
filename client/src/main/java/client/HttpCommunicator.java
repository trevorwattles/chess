package client;

import com.google.gson.Gson;
import model.AuthData;
import model.ListGamesResponse;
import server.request.JoinGameRequest;
import server.request.LoginRequest;
import server.request.RegisterRequest;
import server.request.CreateGameRequest;
import model.GameData;

import java.io.*;
import java.net.*;

public class HttpCommunicator {

    private final String serverURL;
    private static String authToken;

    public HttpCommunicator(String serverURL) {

        this.serverURL = serverURL;
    }

    public AuthData register(RegisterRequest request) throws ResponseException {
        AuthData authData = this.makeRequest("POST", "/user", request, AuthData.class);
        authToken = authData.authToken();
        return authData;
    }



    public AuthData login(LoginRequest request) throws ResponseException {
        AuthData authData = this.makeRequest("POST", "/session", request, AuthData.class);
        authToken = authData.authToken();
        return authData;
    }


    public void logout() throws ResponseException {
        this.makeRequest("DELETE", "/session", null, null);
        authToken = null;
    }

    public GameData createGame(String gameName) throws ResponseException {
        CreateGameRequest request = new CreateGameRequest(gameName);
        return this.makeRequest("POST", "/game", request, GameData.class);
    }

    public ListGamesResponse listGames() throws ResponseException {
        return this.makeRequest("GET", "/game", null, ListGamesResponse.class);
    }


    public void joinGame(int gameID, String playerColor) throws ResponseException {
        var request = new JoinGameRequest(gameID, playerColor);
        this.makeRequest("PUT", "/game", request, null);
    }

    public void clear() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null);
    }
    public String getServerUrl() {
        return serverURL;
    }

    public static String getAuthToken() {
        return authToken;
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }
    private void throwIfNotSuccessful(HttpURLConnection http) throws ResponseException, IOException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            if (status == 403) {
                throw new ResponseException(403, "Already taken");
            }
            else if (status == 401) {
                throw new ResponseException(401, "Unauthorized");
            }
            else if (status == 400) {
                throw new ResponseException(400, "Bad request");
            }
        }
    }
    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
    private static boolean isSuccessful(int status) {
        return status / 100 == 2;
    }



}
