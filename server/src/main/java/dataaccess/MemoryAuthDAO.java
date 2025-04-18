package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Objects;

public class MemoryAuthDAO implements AuthDAO {
    private final HashSet<AuthData> database = new HashSet<>();


    @Override
    public void clear() {
        database.clear();
    }

    @Override
    public void createAuth(AuthData auth) {
        database.add(auth);
    }


    @Override
    public AuthData getAuth(String authToken){
        for (AuthData auth : database) {
            if(Objects.equals(auth.authToken(), authToken)) {
                return auth;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) {
        for (AuthData auth : database) {
            if(Objects.equals(auth.authToken(), authData.authToken())) {
                database.remove(auth);
                break;
            }
        }
    }
}
