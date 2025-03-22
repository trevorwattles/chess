import ui.BeforeLoginREPL;
import client.ServerFacade;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        ServerFacade serverFacade = new ServerFacade(serverUrl);
        Scanner scanner = new Scanner(System.in);

        System.out.println("♕ 240 Chess Client");
        new BeforeLoginREPL(serverFacade, scanner).run();
    }
}
