package chess;

public class BishopMoves extends AbstractMoveGenerator {
    @Override
    protected int[][] getDirections() {
        return new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
    }

    @Override
    protected boolean isSlidingPiece() {
        return true;
    }
}

