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
        return null;
    }
    public void logoutUser(String authToken) throws DataAccessException {    }
    public AuthData getAuthData(String authToken) throws DataAccessException {
        return null;
    }
    public void clear(){
        userDAO.clear();
        authDAO.clear();
    }
}

