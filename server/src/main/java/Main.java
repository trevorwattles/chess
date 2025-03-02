import dataaccess.DataAccessException;
import server.Server;

public class Main {

    public static void main(String[] args) throws DataAccessException {

        System.out.println("♕ 240 Chess Server");
        Server server = new Server();
        server.run(8080);

    }
}