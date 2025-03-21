package client;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Map;

public class HttpCommunicator {

    private final String serverURL;

    public HttpCommunicator(String serverURL) {

        this.serverURL = serverURL;
    }

    public boolean register(String username, String password, String email) {
        return false;
    }


    public boolean login(String username, String password) {
        return false;
    }

    public boolean logout() {
        return false;
    }

    public boolean createGame(String gameName) {
        return false;
    }

    public Collection<Map<String, Object>> listGames() {
        return null;
    }

    public boolean joinGame(int gameID, String playerColor) {
        return false;
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

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
