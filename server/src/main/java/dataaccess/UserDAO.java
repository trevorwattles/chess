package dataaccess;

import model.UserData;

public interface UserDAO {
    public void clear();
    public void createUser(UserData user);
    public UserData getUser(String username) throws DataAccessException;
}
