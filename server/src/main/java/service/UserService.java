package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import dataaccess.RequestException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final MemoryUserDAO userDAO;
    private final MemoryAuthDAO authDAO;

    public UserService() {
        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
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
    public AuthData loginUser(UserData userData) throws RequestException {
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
        return null;
    }
    public void clear(){
        userDAO.clear();
        authDAO.clear();
    }
}

