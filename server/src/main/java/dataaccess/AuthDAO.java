package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public void clear();
    public void createAuth(AuthData auth);
    public AuthData getAuth(String authToken) throws DataAccessException;
    public void deleteAuth(AuthData authData);
}
