package ui;

import chess.ChessGame;
import client.HttpCommunicator;
import client.ServerFacade;
import client.WebSocketCommunicator;
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

            System.out.print("> ");
            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input) - 1;
                if (selection < 0 || selection >= games.size()) {
                    System.out.println("Failed to join game \"" + input + "\". Does not exist.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Failed to join game \"" + input + "\". Does not exist.");
                return;
            }

            GameData selectedGame = games.get(selection);

            System.out.print("Choose color (WHITE or BLACK): ");
            String color = scanner.nextLine().toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Invalid color.");
                return;
            }

            facade.joinGame(selectedGame.gameID(), color);
            System.out.println("Joined game: " + selectedGame.gameName() + " as " + color);

            String serverUrl = facade.getCommunicator().getServerUrl();
            String authToken = ((HttpCommunicator)facade.getCommunicator()).getAuthToken();

            WebSocketCommunicator wsCommunicator = new WebSocketCommunicator(serverUrl, authToken);

            new InGameREPL(scanner, wsCommunicator, selectedGame.gameID(), color).run();

        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private void handleObserveGame() {
        try {
            List<GameData> games = facade.listGames();
            if (games.isEmpty()) {
                System.out.println("No games available.");
                return;
            }

            System.out.println("Select a game number to observe:");
            for (int i = 0; i < games.size(); i++) {
                GameData g = games.get(i);
                System.out.printf("%d. %s\n", i + 1, g.gameName());
            }

            System.out.print("> ");
            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input) - 1;
                if (selection < 0 || selection >= games.size()) {
                    System.out.println("Failed to observe game \"" + input + "\". Does not exist.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Failed to observe game \"" + input + "\". Does not exist.");
                return;
            }

            GameData selectedGame = games.get(selection);

            facade.observeGame(selectedGame.gameID());
            System.out.println("Now observing game: " + selectedGame.gameName());

            String serverUrl = facade.getCommunicator().getServerUrl();
            String authToken = ((HttpCommunicator) facade.getCommunicator()).getAuthToken();

            WebSocketCommunicator wsCommunicator = new WebSocketCommunicator(serverUrl, authToken);

            new InGameREPL(scanner, wsCommunicator, selectedGame.gameID(), "OBSERVER").run();

        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
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

            System.out.print("> ");
            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input) - 1;
                if (selection < 0 || selection >= games.size()) {
                    System.out.println("Failed to join game \"" + input + "\". Does not exist.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Failed to join game \"" + input + "\". Does not exist.");
                return;
            }

            GameData selectedGame = games.get(selection);

            if (role.equals("observer")) {
                facade.observeGame(selectedGame.gameID());
                System.out.println("Now observing game: " + selectedGame.gameName());

                ChessGame game = new ChessGame();
                PrintBoard.printWhiteBoard(game);

            } else {
                System.out.print("Choose color (WHITE or BLACK): ");
                String color = scanner.nextLine().toUpperCase();
                if (!color.equals("WHITE") && !color.equals("BLACK")) {
                    System.out.println("Invalid color.");
                    return;
                }

                facade.joinGame(selectedGame.gameID(), color);
                System.out.println("Joined game: " + selectedGame.gameName() + " as " + color);

                ChessGame game = new ChessGame();

                if (color.equals("WHITE")) {
                    PrintBoard.printWhiteBoard(game);
                } else {
                    PrintBoard.printBlackBoard(game);
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to " + role + " game: " + e.getMessage());
        }
    }
}
