package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws RequestException, DataAccessException {
        if (userData == null ||
                userData.username() == null || userData.password() == null || userData.email() == null ||
                userData.username().isBlank() || userData.password().isBlank() || userData.email().isBlank()) {
            throw new RequestException("Error: bad request");
        }

        if (userDAO.getUser(userData.username()) != null) {
            throw new RequestException("Error: already taken");
        }

        userDAO.createUser(userData);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, userData.username());

        authDAO.createAuth(authData);

        return authData;
    }
    public AuthData loginUser(UserData userData) throws RequestException, DataAccessException {
        if (userData == null || userData.username() == null || userData.password() == null ||
                userData.username().isBlank() || userData.password().isBlank()) {
            throw new RequestException("Error: bad request");
        }

        System.out.println("Attempting login for user: " + userData.username());

        UserData existingUser = userDAO.getUser(userData.username());

        if (existingUser == null) {
            throw new RequestException("Error: unauthorized");
        }

        if (!existingUser.password().equals(userData.password())) {
            throw new RequestException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, userData.username());

        authDAO.createAuth(authData);

        System.out.println("Login successful: " + userData.username() + " | AuthToken: " + authToken);

        return authData;
    }

    public void logoutUser(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException("Error: unauthorized");
        }

        System.out.println("Attempting logout for authToken: " + authToken);

        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        authDAO.deleteAuth(authData);

        if (authDAO.getAuth(authToken) != null) {
            throw new DataAccessException("Error: unauthorized");
        }

        System.out.println("Logout successful for user: " + authData.username());
    }

    public AuthData getAuthData(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException("Error: unauthorized");
        }

        System.out.println("Fetching AuthData for authToken: " + authToken);

        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return authData;
    }

    public void clear(){
        userDAO.clear();
        authDAO.clear();
    }
}

