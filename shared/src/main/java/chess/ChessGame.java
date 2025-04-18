package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    private boolean gameOver;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currPiece = board.getPiece(startPosition);
        if (currPiece == null) {
            return Collections.emptyList();
        }

        List<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : currPiece.pieceMoves(board, startPosition)) {
            ChessPiece tempPiece = board.getPiece(move.getEndPosition());
            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), currPiece);

            if (!isInCheck(currPiece.getTeamColor())) {
                validMoves.add(move);
            }
            board.addPiece(move.getEndPosition(), tempPiece);
            board.addPiece(startPosition, currPiece);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        if (movingPiece == null) {
            throw new InvalidMoveException("No piece at the starting position.");
        }

        if (getTeamTurn() != movingPiece.getTeamColor()) {
            throw new InvalidMoveException("It's not your turn.");
        }

        Collection<ChessMove> availableMoves = validMoves(move.getStartPosition());

        if (availableMoves == null || !availableMoves.contains(move)) {
            throw new InvalidMoveException("Illegal move attempted.");
        }

        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), movingPiece);
        board.addPiece(move.getStartPosition(), null);

        setTeamTurn((getTeamTurn() == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
    }


    public ChessPosition findKing(TeamColor teamColor) {
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(y, x));

                if (currPiece != null &&
                        currPiece.getTeamColor() == teamColor &&
                        currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(y, x);
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        if (kingPosition == null) {
            return false;
        }

        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece opponentPiece = board.getPiece(new ChessPosition(y, x));
                if (opponentPiece == null || opponentPiece.getTeamColor() == teamColor) {continue;} // Skip irrelevant pieces

                Collection<ChessMove> possibleMoves = opponentPiece.pieceMoves(board, new ChessPosition(y, x)); // Extracted outside
                for (ChessMove move : possibleMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(y, x));
                if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                    if (!validMoves(new ChessPosition(y, x)).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //check to see if player has no valid moves for any of its pieces thus leading to stalemate
        if (isInCheckmate(teamColor)) {
            return false;
        }
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(y, x));
                if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                    if(validMoves(new ChessPosition(y, x)).size() != 0) {
                        return false;
                    }
                }
            }
        }
       return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }
    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public boolean isOver() {
        return gameOver;
    }

    public void setOver(boolean over) {
        gameOver = over;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return gameOver == chessGame.gameOver && teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board, gameOver);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                ", gameOver=" + gameOver +
                '}';
    }
}
