package ui;

import client.ServerFacade;
import model.GameData;
import java.util.List;
import java.util.Scanner;

public class AfterLoginREPL {

    private final ServerFacade facade;
    private final Scanner scanner;

    public AfterLoginREPL(ServerFacade facade, Scanner scanner) {
        this.facade = facade;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        printHelp();

        while (true) {
            System.out.print("\npostlogin> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;
                case "logout":
                    handleLogout();
                    return;
                case "create game":
                    handleCreateGame();
                    break;
                case "list games":
                    handleListGames();
                    break;
                case "play game":
                    handlePlayGame();
                    break;
                case "observe game":
                    handleObserveGame();
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help          - Show this help message");
        System.out.println("  logout        - Logout and return to prelogin menu");
        System.out.println("  create game   - Create a new chess game");
        System.out.println("  list games    - List all available games");
        System.out.println("  play game     - Join a game as a player");
        System.out.println("  observe game  - Join a game as an observer");
    }

    private void handleLogout() {
        try {
            facade.logout();
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Failed to logout: " + e.getMessage());
        }
    }

    private void handleCreateGame() {
        System.out.print("Enter a name for your new game: ");
        String name = scanner.nextLine();
        try {
            facade.createGame(name);
            System.out.println("Game '" + name + "' created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            List<GameData> games = facade.listGames();
            if (games.isEmpty()) {
                System.out.println("No games found.");
            } else {
                System.out.println("Available games:");
                for (int i = 0; i < games.size(); i++) {
                    GameData g = games.get(i);
                    System.out.printf("%d. %s | White: %s | Black: %s\n",
                            i + 1,
                            g.gameName(),
                            g.whiteUsername() != null ? g.whiteUsername() : "-",
                            g.blackUsername() != null ? g.blackUsername() : "-");
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }

    private void handlePlayGame() {
        handleJoinGame("player");
    }

    private void handleObserveGame() {
        handleJoinGame("observer");
    }

    private void handleJoinGame(String role) {
        try {
            List<GameData> games = facade.listGames();
            if (games.isEmpty()) {
                System.out.println("No games available.");
                return;
            }

            System.out.println("Select a game number:");
            for (int i = 0; i < games.size(); i++) {
                GameData g = games.get(i);
                System.out.printf("%d. %s\n", i + 1, g.gameName());
            }

            int selection = Integer.parseInt(scanner.nextLine()) - 1;
            if (selection < 0 || selection >= games.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            GameData selectedGame = games.get(selection);
            if (role.equals("observer")) {
                facade.observeGame(selectedGame.gameID());
                System.out.println("Now observing game: " + selectedGame.gameName());
            } else {
                System.out.print("Choose color (WHITE or BLACK): ");
                String color = scanner.nextLine().toUpperCase();
                if (!color.equals("WHITE") && !color.equals("BLACK")) {
                    System.out.println("Invalid color.");
                    return;
                }
                facade.joinGame(selectedGame.gameID(), color);
                System.out.println("Joined game: " + selectedGame.gameName() + " as " + color);
            }

            // Placeholder for future board rendering
            System.out.println("\n[Initial Chessboard Display Here]\n");

        } catch (Exception e) {
            System.out.println("Failed to " + role + " game: " + e.getMessage());
        }
    }
}
