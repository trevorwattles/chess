package ui;

import client.ResponseException;
import client.ServerFacade;
import server.request.LoginRequest;
import server.request.RegisterRequest;

import java.util.Scanner;

public class BeforeLoginREPL {

    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public BeforeLoginREPL(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Welcome to Chess! Type 'help' for a list of commands.");
        boolean running = true;

        while (running) {
            System.out.print("prelogin> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "quit" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                case "register" -> handleRegister();
                case "login" -> handleLogin();
                default -> System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help     - Show this help message");
        System.out.println("  quit     - Exit the application");
        System.out.println("  login    - Log into your account");
        System.out.println("  register - Create a new account");
    }

    private void handleRegister() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try {
            serverFacade.register(new RegisterRequest(username, password, email));
            System.out.println("Registration successful. Logged in as " + username);
            new AfterLoginREPL(serverFacade, scanner).run();

        } catch (ResponseException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            serverFacade.login(new LoginRequest(username, password));
            System.out.println("Login successful. Welcome, " + username + "!");
            new AfterLoginREPL(serverFacade, scanner).run();
        } catch (ResponseException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}

