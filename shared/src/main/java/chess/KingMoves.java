package chess;

import java.util.ArrayList;
import java.util.Collection;
import static chess.ChessPiece.PieceType.BISHOP;

public class KingMoves implements PieceMoves {
    private Collection<ChessMove> moves;
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (!board.isInBounds(row,col)) {
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
            }
        }
        return moves;
    }
}
