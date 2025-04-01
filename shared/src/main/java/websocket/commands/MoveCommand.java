package websocket.commands;

public class MoveCommand extends UserGameCommand {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;

    public MoveCommand(String authToken, Integer gameID, int startRow, int startCol, int endRow, int endCol) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }
}
