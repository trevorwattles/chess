package ui;

import chess.*;
import client.ServerFacade;
import model.GameData;

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
