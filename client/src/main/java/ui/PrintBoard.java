package ui;

import chess.*;

import static chess.ChessGame.TeamColor.*;
import static ui.EscapeSequences.*;

import java.util.Set;
import java.util.function.Function;

public class PrintBoard {

    private static final String HIGHLIGHT_BG_COLOR = SET_BG_COLOR_YELLOW;

    public static void printWhiteBoard(ChessGame game) {
        printBoard(game, WHITE);
    }

    public static void printBlackBoard(ChessGame game) {
        printBoard(game, BLACK);
    }

    private static void printBoard(ChessGame game, ChessGame.TeamColor perspective) {
        printBoardInternal(game, perspective, pos -> {
            int row = pos.getRow();
            int col = pos.getColumn();
            return (row + col) % 2 != 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        });
    }

    public static void printHighlightedWhiteBoard(ChessGame game, Set<ChessPosition> highlightedPositions) {
        printHighlightedBoard(game, WHITE, highlightedPositions);
    }

    public static void printHighlightedBlackBoard(ChessGame game, Set<ChessPosition> highlightedPositions) {
        printHighlightedBoard(game, BLACK, highlightedPositions);
    }

    private static void printHighlightedBoard(ChessGame game, ChessGame.TeamColor perspective, Set<ChessPosition> highlightedPositions) {
        printBoardInternal(game, perspective, pos -> {
            if (highlightedPositions.contains(pos)) {
                return HIGHLIGHT_BG_COLOR;
            }
            int row = pos.getRow();
            int col = pos.getColumn();
            return (row + col) % 2 != 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        });
    }

    private static void printBoardInternal(ChessGame game,
                                           ChessGame.TeamColor perspective,
                                           Function<ChessPosition, String> bgColorFunction) {
        ChessBoard board = game.getBoard();

        System.out.println(ERASE_SCREEN);

        boolean whitePerspective = (perspective == WHITE);

        int rowStart = whitePerspective ? 8 : 1;
        int rowEnd = whitePerspective ? 0 : 9;
        int rowStep = whitePerspective ? -1 : 1;

        int colStart = whitePerspective ? 1 : 8;
        int colEnd = whitePerspective ? 9 : 0;
        int colStep = whitePerspective ? 1 : -1;

        // Column headers
        System.out.print("   ");
        for (int col = colStart; col != colEnd; col += colStep) {
            char colLabel = (char) ('a' + col - 1);
            System.out.print(" " + colLabel + "\u2003");
        }
        System.out.println();

        // Rows
        for (int row = rowStart; row != rowEnd; row += rowStep) {
            System.out.print(" " + row + " ");

            for (int col = colStart; col != colEnd; col += colStep) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                String bg = bgColorFunction.apply(position);

                if (piece == null) {
                    System.out.print(bg + EMPTY + RESET_TEXT_COLOR + RESET_BG_COLOR);
                } else {
                    String symbol = getPieceSymbol(piece);
                    System.out.print(bg + symbol + RESET_TEXT_COLOR + RESET_BG_COLOR);
                }
            }

            System.out.println(" " + row);
        }

        // Column headers again
        System.out.print("   ");
        for (int col = colStart; col != colEnd; col += colStep) {
            char colLabel = (char) ('a' + col - 1);
            System.out.print(" " + colLabel + "\u2003");
        }
        System.out.println();
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> (piece.getTeamColor() == WHITE ? WHITE_KING : BLACK_KING);
            case QUEEN -> (piece.getTeamColor() == WHITE ? WHITE_QUEEN : BLACK_QUEEN);
            case ROOK -> (piece.getTeamColor() == WHITE ? WHITE_ROOK : BLACK_ROOK);
            case BISHOP -> (piece.getTeamColor() == WHITE ? WHITE_BISHOP : BLACK_BISHOP);
            case KNIGHT -> (piece.getTeamColor() == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT);
            case PAWN -> (piece.getTeamColor() == WHITE ? WHITE_PAWN : BLACK_PAWN);
        };
    }
}
