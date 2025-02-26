package chess;

public class KingMoves extends AbstractMoveGenerator {
    @Override
    protected int[][] getDirections() {
        return new int[][]{
                {-1, 0}, {1, 1}, {1, -1}, {0, 1}, {-1, 1}, {-1, -1}, {1, 0}, {0, -1}
        };
    }

    @Override
    protected boolean isSlidingPiece() {
        return false;
    }
}
