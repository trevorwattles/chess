package chess;

public class QueenMoves extends AbstractMoveGenerator {
    @Override
    protected int[][] getDirections() {
        return new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, // Diagonals
                {0, 1}, {1, 0}, {0, -1}, {-1, 0}   // Straight lines
        };
    }

    @Override
    protected boolean isSlidingPiece() {
        return true;
    }
}
