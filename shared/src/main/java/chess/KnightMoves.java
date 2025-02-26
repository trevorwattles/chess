package chess;

public class KnightMoves extends AbstractMoveGenerator {
    @Override
    protected int[][] getDirections() {
        return new int[][]{
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
    }

    @Override
    protected boolean isSlidingPiece() {
        return false;
    }
}
