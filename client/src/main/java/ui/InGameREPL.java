package ui;

import chess.*;
import client.ServerFacade;
import model.GameData;
import websocket.commands.MoveCommand;

import java.util.Scanner;

import static java.lang.System.out;


public class InGameREPL {
   ServerFacade facade;
   ChessGame chessGame;
   int gameID;
   public static ChessGame.TeamColor teamColor;

    public InGameREPL(ServerFacade facade, GameData gameData, ChessGame.TeamColor teamColor) {
        this.facade = facade;
        this.chessGame = gameData.game();
        this.gameID = gameData.gameID();
        InGameREPL.teamColor = teamColor;
    }


    public void run() {
        out.println("You're now in the game! Type 'help' for available commands.");
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        while (running) {
            out.print("ingame> ");
            String[] input = scanner.nextLine().trim().split("\\s+");
            if (input.length == 0 || input[0].isEmpty()) continue;

            switch (input[0].toLowerCase()) {
                case "help" -> printHelp();
                case "redraw" -> printBoard();
                case "move" -> handleMove(input);
                case "resign" -> {
                    out.print("Are you sure you want to resign? (yes/no): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.equals("yes")) {
                        // send a resign command over WebSocket here
                        out.println("You resigned.");
                        running = false;
                    } else {
                        out.println("Resignation cancelled.");
                    }
                }
                case "leave" -> {
                    // send leave command over WebSocket
                    out.println("You left the game.");
                    running = false;
                }
                case "highlight" -> handleHighlight(input);
                default -> {
                    out.println("Unknown command. Type 'help' for a list of commands.");
                    printHelp();
                }
            }
        }

        new AfterLoginREPL(facade, new Scanner(System.in)).run();

    }


    private void printHelp() {
        out.println("In-game commands:");
        out.println("  help                 - Show this help message");
        out.println("  redraw               - Redraw the chess board");
        out.println("  move <from> <to>     - Move a piece (e.g., 'move a2 a3')");
        out.println("  move <from> <to> <promotion> - Promote a pawn (e.g., 'move a7 a8 queen')");
        out.println("  resign               - Resign from the current game");
        out.println("  leave                - Leave the current game");
        out.println("  highlight <position> - Show legal moves (e.g., 'highlight b1')");
    }


    private void printBoard() {
        if (teamColor == ChessGame.TeamColor.WHITE) {
            PrintBoard.printWhiteBoard(chessGame);
        } else {
            PrintBoard.printBlackBoard(chessGame);
        }
    }


    private void handleMove(String[] input) {

    }


    private void handleHighlight(String[] input) {

    }

    private ChessPiece.PieceType getPiece(String name) {
        return switch (name.toLowerCase()) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}
