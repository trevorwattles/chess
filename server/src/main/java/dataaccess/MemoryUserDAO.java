package dataaccess;

import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    private final HashSet<UserData> database = new HashSet<>();
    @Override
    public void clear() {
        database.clear();
    }

    @Override
    public void createUser(UserData user) {
        database.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : database) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        throw new DataAccessException("User: " + username + " not found");
    }
}
