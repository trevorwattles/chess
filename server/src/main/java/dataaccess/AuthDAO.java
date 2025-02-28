package dataaccess;

import model.AuthData;

public interface AuthDAO {
    public void clear();
    public void createAuth(String authToken);
    public AuthData getAuth() throws DataAccessException;
    public void deleteAuth(AuthData authData);
}
