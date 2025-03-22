package client;

import model.GameData;
import model.AuthData;
import model.ListGamesResponse;
import server.request.RegisterRequest;
import server.request.LoginRequest;

import java.util.ArrayList;
import java.util.List;

public class ServerFacade {

    private final HttpCommunicator communicator;

    public ServerFacade(String serverURL) {
        this.communicator = new HttpCommunicator(serverURL);
    }

    public AuthData register(RegisterRequest request) throws ResponseException {
        return communicator.register(request);
    }

    public AuthData login(LoginRequest request) throws ResponseException {
        return communicator.login(request);
    }

    public void logout() throws ResponseException {
        communicator.logout();
    }

    public GameData createGame(String gameName) throws ResponseException {
        return communicator.createGame(gameName);
    }

    public List<GameData> listGames() throws ResponseException {
        ListGamesResponse response = communicator.listGames();
        return new ArrayList<>(response.games);
    }

    public void joinGame(int gameID, String playerColor) throws ResponseException {
        communicator.joinGame(gameID, playerColor);
    }



    public void observeGame() {

    }

    public void clear() throws ResponseException {
        communicator.clear();
    }



}
