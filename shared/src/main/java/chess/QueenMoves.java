package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoves implements PieceMoves {
    private Collection<ChessMove> moves;
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {
                {1, 0},
                {1, 1},
                {0, 1},
                {-1, 1},
                {-1, 0},
                {-1, -1},
                {0, -1},
                {1, -1}
        };

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
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
            }
        }
        return moves;
    }
}
