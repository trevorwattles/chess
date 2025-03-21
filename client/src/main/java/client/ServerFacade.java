package client;

import model.AuthData;
import server.request.RegisterRequest;
import server.request.LoginRequest;

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

    public void listGames()  {

    }

    public void createGame()  {

    }

    public void joinGame() {
    }


    public void observeGame() {

    }

    public void clear() throws ResponseException {
        communicator.clear();
    }



}
