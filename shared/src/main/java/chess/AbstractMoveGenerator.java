package chess;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractMoveGenerator implements PieceMoves {
    protected abstract int[][] getDirections();
    protected abstract boolean isSlidingPiece();

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] directions = getDirections();

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            do {
                row += direction[0];
                col += direction[1];

                if (row > 8 || col > 8 || row < 1 || col < 1) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(newPosition);

                if (piece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (piece.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            } while (isSlidingPiece());
        }

        return moves;
    }
}
