package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;


public class PawnMoves implements PieceMoves {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        // Determine movement direction based on the pawn's team color
        int direction = myPiece.getTeamColor() == WHITE? 1 : -1;

        // Single square forward move
        ChessPosition forwardOne = new ChessPosition(row + direction, col);
        if (board.getPiece(forwardOne) == null && ((myPiece.getTeamColor() == WHITE && row != 7) ||
                (myPiece.getTeamColor() == BLACK && row != 2))) {
            moves.add(new ChessMove(myPosition, forwardOne, null));
            // Double square forward move (only from starting position)
            if ((myPiece.getTeamColor() == WHITE && row == 2) ||
                    (myPiece.getTeamColor() == BLACK && row == 7)) {
                ChessPosition forwardTwo = new ChessPosition(row + 2 * direction, col);
                if (board.getPiece(forwardTwo) == null) {
                    moves.add(new ChessMove(myPosition, forwardTwo, null));
                }
            }
        }
        if (((myPiece.getTeamColor() == WHITE && row == 7) ||
                (myPiece.getTeamColor() == BLACK && row == 2)) && board.getPiece(forwardOne) == null) {
            moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.KNIGHT));
        }

        // Capturing moves (diagonals)
        int[][] captureDirections = {{direction, 1}, {direction, -1}};
        for (int[] capture : captureDirections) {
            int newRow = row + capture[0];
            int newCol = col + capture[1];
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition capturePosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(capturePosition);
                if ((targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) &&
                        (myPiece.getTeamColor() == WHITE && capturePosition.getRow() != 8 ||
                                myPiece.getTeamColor() == BLACK && capturePosition.getRow() != 1)){
                    moves.add(new ChessMove(myPosition, capturePosition, null));
                }
                if ((targetPiece != null && targetPiece.getTeamColor() != myPiece.getTeamColor()) &&
                        ((myPiece.getTeamColor() == WHITE && capturePosition.getRow() == 8 ||
                                myPiece.getTeamColor() == BLACK && capturePosition.getRow() == 1))) {
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.KNIGHT));
                }
            }
        }


        return moves;
    }
}

