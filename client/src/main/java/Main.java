import ui.BeforeLoginREPL;
import client.ServerFacade;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client");

        String serverUrl = "http://localhost:8080"; // Update this if your server runs on a different port
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        Scanner scanner = new Scanner(System.in);

        new BeforeLoginREPL(serverFacade, scanner).run();
    }
}
