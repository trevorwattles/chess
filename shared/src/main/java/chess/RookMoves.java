package chess;

public class RookMoves extends AbstractMoveGenerator {
    @Override
    protected int[][] getDirections() {
        return new int[][]{
                {0, 1}, {1, 0}, {0, -1}, {-1, 0}
        };
    }

    @Override
    protected boolean isSlidingPiece() {
        return true;
    }
}
