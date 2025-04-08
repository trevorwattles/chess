package ui;

import chess.*;
import client.WebSocketCommunicator;

import java.util.*;

public class InGameREPL {
    private final Scanner scanner;
    private final WebSocketCommunicator communicator;
    private final int gameID;
    private final String playerColor; // "WHITE", "BLACK", or "OBSERVER"
    private ChessGame currentGame;
    private boolean running = true;

    public InGameREPL(Scanner scanner, WebSocketCommunicator communicator, int gameID, String playerColor) {
        this.scanner = scanner;
        this.communicator = communicator;
        this.gameID = gameID;
        this.playerColor = playerColor;

        // Initialize with a default chess game
        this.currentGame = new ChessGame();

        // Set up handlers for different message types
        setupMessageHandlers();
    }

    private void setupMessageHandlers() {
        // Handle game updates (redraw board)
        communicator.setGameUpdateHandler(game -> {
            currentGame = game;
            System.out.println("\nGame state updated");
            redrawBoard();
        });

        // Handle notifications
        communicator.setNotificationHandler(message -> {
            System.out.println("\n" + message);
            System.out.print("ingame> ");
        });


        communicator.setErrorHandler(errorMsg -> {
            String lower = errorMsg.toLowerCase();

            if (lower.contains("check")) {
                System.out.println("\nYou are in check — invalid move.");
            } else if (lower.contains("not your turn")) {
                System.out.println("\nIt's not your turn.");
            } else if (lower.contains("observers cannot")) {
                System.out.println("\nObservers cannot perform this action.");
            } else if (lower.contains("game is already over")) {
                System.out.println("\nThe game has already ended.");
            } else if (lower.contains("invalid move")) {
                System.out.println("\nInvalid move. Try again.");
            } else {
                System.out.println("\nERROR: " + errorMsg);
            }

            System.out.print("ingame> ");
        });

    }

    public void run() {
        try {
            communicator.connect(gameID);
            System.out.println("Connected to game " + gameID);

            redrawBoard();

            printHelp();

            while (running) {
                String input = scanner.nextLine().trim();
                processCommand(input);
                System.out.print("ingame> ");
            }



        } catch (Exception e) {
            System.out.println("Error in game session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help                           - Show this help message");
        System.out.println("  redraw                         - Redraw the chess board");
        System.out.println("  leave                          - Leave the game and return to main menu");
        System.out.println("  move <from> <to>               - Make a move (e.g., 'move e2 e4')");
        System.out.println("  resign                         - Resign from the game");
        System.out.println("  highlight <position>           - Highlight legal moves for a piece (e.g., 'highlight e2')");
    }

    private void redrawBoard() {
        if (currentGame == null) {
            currentGame = new ChessGame();
            System.out.println("Displaying initial board state.");
        }

        if (playerColor.equalsIgnoreCase("WHITE")) {
            PrintBoard.printWhiteBoard(currentGame);
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            PrintBoard.printBlackBoard(currentGame);
        } else {
            PrintBoard.printWhiteBoard(currentGame);
        }
        System.out.print("ingame> ");
    }

    private void processCommand(String input) {
        try {
            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase();

            switch (command) {
                case "help":
                    printHelp();
                    break;

                case "redraw":
                    redrawBoard();
                    break;

                case "leave":
                    communicator.leaveGame(gameID);
                    running = false;
                    break;

                case "move":
                    if (playerColor.equalsIgnoreCase("OBSERVER")) {
                        System.out.println("Observers cannot make moves.");
                        return;
                    }

                    if (tokens.length != 3) {
                        System.out.println("Invalid move format. Use: move <from> <to>");
                        return;
                    }

                    handleMoveCommand(tokens[1], tokens[2]);
                    break;

                case "resign":
                    if (playerColor.equalsIgnoreCase("OBSERVER")) {
                        System.out.println("Observers cannot resign.");
                        return;
                    }

                    System.out.print("Are you sure you want to resign? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (confirm.startsWith("y")) {
                        communicator.resignGame(gameID);
                    }
                    break;

                case "highlight":
                    if (tokens.length != 2) {
                        System.out.println("Invalid highlight format. Use: highlight <position>");
                        return;
                    }

                    highlightLegalMoves(tokens[1]);
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }

    private void handleMoveCommand(String fromStr, String toStr) throws Exception {
        ChessPosition from = parsePosition(fromStr);
        ChessPosition to = parsePosition(toStr);

        if (from == null || to == null) {
            System.out.println("Invalid position format. Use algebraic notation (e.g., 'e2')");
            return;
        }

        ChessPiece.PieceType promotionPiece = null;
        if (currentGame != null) {
            ChessPiece piece = currentGame.getBoard().getPiece(from);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                int lastRank = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;
                if (to.getRow() == lastRank) {
                    promotionPiece = promptForPromotion();
                }
            }
        }

        ChessMove move = new ChessMove(from, to, promotionPiece);
        communicator.makeMove(gameID, move);
    }

    private ChessPiece.PieceType promptForPromotion() {
        while (true) {
            System.out.print("Promote pawn to (Q)ueen, (R)ook, (B)ishop, or k(N)ight: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.isEmpty()) continue;

            char choice = input.charAt(0);
            switch (choice) {
                case 'Q': return ChessPiece.PieceType.QUEEN;
                case 'R': return ChessPiece.PieceType.ROOK;
                case 'B': return ChessPiece.PieceType.BISHOP;
                case 'N': return ChessPiece.PieceType.KNIGHT;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void highlightLegalMoves(String posStr) {
        ChessPosition position = parsePosition(posStr);

        if (position == null) {
            System.out.println("Invalid position format. Use algebraic notation (e.g., 'e2')");
            return;
        }

        if (currentGame == null) {
            System.out.println("No game data available yet.");
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            System.out.println("No piece at position " + posStr);
            return;
        }

        Collection<ChessMove> legalMoves = currentGame.validMoves(position);
        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves for piece at " + posStr);
            return;
        }

        Set<ChessPosition> highlightPositions = new HashSet<>();
        highlightPositions.add(position);

        for (ChessMove move : legalMoves) {
            highlightPositions.add(move.getEndPosition());
        }

        if (playerColor.equalsIgnoreCase("WHITE")) {
            printHighlightedBoard(currentGame, ChessGame.TeamColor.WHITE, highlightPositions);
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            printHighlightedBoard(currentGame, ChessGame.TeamColor.BLACK, highlightPositions);
        } else {
            printHighlightedBoard(currentGame, ChessGame.TeamColor.WHITE, highlightPositions);
        }

        System.out.println("Highlighted " + legalMoves.size() + " legal moves for " + piece.getPieceType() + " at " + posStr);
    }

    private void printHighlightedBoard(ChessGame game, ChessGame.TeamColor perspective, Set<ChessPosition> highlightPositions) {
        if (perspective == ChessGame.TeamColor.WHITE) {
            PrintBoard.printHighlightedWhiteBoard(game, highlightPositions);
        } else {
            PrintBoard.printHighlightedBlackBoard(game, highlightPositions);
        }
    }


    private ChessPosition parsePosition(String posStr) {
        if (posStr == null || posStr.length() != 2) {
            return null;
        }

        char file = Character.toLowerCase(posStr.charAt(0));
        char rank = posStr.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        int col = file - 'a' + 1;  // Convert 'a'-'h' to 1-8
        int row = rank - '0';      // Convert '1'-'8' to 1-8

        return new ChessPosition(row, col);
    }
}