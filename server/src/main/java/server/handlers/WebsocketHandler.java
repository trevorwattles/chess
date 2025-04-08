package server.handlers;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import server.Server;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {
    private static final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        Server.gameSessionsMap.put(session, 0);
        System.out.println("New connection: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Server.gameSessionsMap.remove(session);
        System.out.println("Connection closed: " + session.getRemoteAddress().getAddress() +
                " - Code: " + statusCode + ", Reason: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);
        try {
            UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);
            switch (baseCommand.getCommandType()) {
                case CONNECT -> {
                    ConnectCommand connectCommand = gson.fromJson(message, ConnectCommand.class);
                    Server.gameSessionsMap.replace(session, connectCommand.getGameID());
                    handleConnectCommand(session, connectCommand);
                }
                case MAKE_MOVE -> {
                    MoveCommand moveCommand = gson.fromJson(message, MoveCommand.class);
                    handleMoveCommand(session, moveCommand);
                }
                case LEAVE -> {
                    LeaveCommand leaveCommand = gson.fromJson(message, LeaveCommand.class);
                    handleLeaveCommand(session, leaveCommand);
                    Server.gameSessionsMap.remove(session);
                }
                case RESIGN -> {
                    ResignCommand resignCommand = gson.fromJson(message, ResignCommand.class);
                    handleResignCommand(session, resignCommand);
                }
                default -> System.out.println("Unknown command received.");
            }
        } catch (Exception e) {
            sendError(session, "Invalid message format.", e);
        }
    }

    private void handleConnectCommand(Session session, ConnectCommand cmd) throws IOException {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            if (game.game() == null && game.whiteUsername() != null && game.blackUsername() != null) {
                game = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        new ChessGame()
                );
                Server.gameService.updateGame(cmd.getAuthToken(), game);
            }

            String role;
            if (auth.username().equals(game.whiteUsername())) role = "white";
            else if (auth.username().equals(game.blackUsername())) role = "black";
            else role = "observer";

            Notification notif = new Notification("%s has joined the game as %s"
                    .formatted(auth.username(), role));
            broadcastMessage(session, notif, false);
            sendMessage(session, new LoadGame(game.game()));

            if (game.whiteUsername() == null || game.blackUsername() == null) {
                Notification waiting = new Notification("Waiting for another player to join...");
                broadcastMessage(session, waiting, true);
            } else {
                Notification start = new Notification("Both players are present. " +
                        "It is " + game.game().getTeamTurn() + "'s turn.");
                broadcastMessage(session, start, true);
            }

        } catch (Exception e) {
            sendError(session, "Error: Not authorized", e);
        }
    }

    private void handleMoveCommand(Session session, MoveCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            if (game == null || game.game() == null) {
                sendError(session, "Game not found or not initialized.", null);
                return;
            }

            ChessPosition start = new ChessPosition(
                    cmd.getMove().getStartPosition().getRow(),
                    cmd.getMove().getStartPosition().getColumn());
            ChessPosition end = new ChessPosition(
                    cmd.getMove().getEndPosition().getRow(),
                    cmd.getMove().getEndPosition().getColumn());
            ChessMove move = new ChessMove(start, end, cmd.getMove().getPromotionPiece());

            ChessGame.TeamColor playerColor = getPlayerColor(auth.username(), game);
            if (playerColor == null) {
                sendError(session, "Observers cannot make moves.", null);
                return;
            }

            if (game.game().isOver()) {
                if (game.game().isInCheckmate(playerColor)) {
                    sendError(session, "Move not allowed, game is over by checkmate.", null);
                } else if (game.game().isInStalemate(playerColor)) {
                    sendError(session, "Move not allowed, game is drawn by stalemate.", null);
                } else {
                    sendError(session, "Game is already over.", null);
                }
                return;
            }

            if (!game.game().getTeamTurn().equals(playerColor)) {
                sendError(session, "It is not your turn.", null);
                return;
            }

            game.game().makeMove(move);

            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK
                    : ChessGame.TeamColor.WHITE;

            String notification;
            if (game.game().isInCheckmate(opponent)) {
                game.game().setOver(true);
                notification = "Checkmate! " + auth.username() + " wins!";
            } else if (game.game().isInStalemate(opponent)) {
                game.game().setOver(true);
                notification = "Stalemate! The game is a draw.";
            } else if (game.game().isInCheck(opponent)) {
                notification = "Check! " + opponent + " is in check.";
            } else {
                notification = auth.username() + " has made a move.";
            }

            broadcastMessage(session, new Notification(notification), false);
            broadcastMessage(session, new LoadGame(game.game()), true);

            if (!game.game().isOver()) {
                broadcastMessage(session,
                        new Notification("It is now " + game.game().getTeamTurn() + "'s turn."),
                        false);
            }

            Server.gameService.updateGame(cmd.getAuthToken(), game);

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("check")) {
                sendError(session, "You are in check, invalid move.", e);
            } else if (msg.contains("invalid move")) {
                sendError(session, "Invalid move. Try again.", e);
            } else {
                sendError(session, "Error processing move", e);
            }
        }
    }

    private void handleLeaveCommand(Session session, LeaveCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());

            String username = auth.username();
            broadcastMessage(session, new Notification(username + " has left the game."), false);

            if (username.equals(game.whiteUsername())) {
                game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            } else if (username.equals(game.blackUsername())) {
                game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            }

            Server.gameSessionsMap.remove(session);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch (Exception e) {
            sendError(session, "Error processing leave command", e);
        }
    }

    private void handleResignCommand(Session session, ResignCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            ChessGame chessGame = game.game();

            if (chessGame.isOver()) {
                sendError(session, "Error: Game is already over", new Exception("Resign after game over"));
                return;
            }

            ChessGame.TeamColor playerColor = getPlayerColor(auth.username(), game);
            if (playerColor == null) {
                sendError(session, "Error: Observers cannot resign", new Exception("Observer resignation"));
                return;
            }

            chessGame.setOver(true);
            ChessGame.TeamColor oppColor = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            broadcastMessage(session,
                    new Notification(auth.username() + " has resigned. Team " + oppColor + " wins!"),
                    true);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch (Exception e) {
            sendError(session, "Error processing resign command", e);
        }
    }

    public void sendMessage(Session session, Object messageObj) throws IOException {
        String json = gson.toJson(messageObj);
        session.getRemote().sendString(json);
    }

    public void broadcastMessage(Session sender, Object messageObj, boolean includeSender) throws IOException {
        Integer gameID = Server.gameSessionsMap.get(sender);
        if (gameID == null) {
            System.err.println("Sender session not associated with any game.");
            return;
        }

        String json = gson.toJson(messageObj);
        for (Session session : Server.gameSessionsMap.keySet()) {
            if (gameID.equals(Server.gameSessionsMap.get(session))) {
                if (includeSender || !session.equals(sender)) {
                    session.getRemote().sendString(json);
                }
            }
        }
    }

    private void sendError(Session session, String errorMessage, Exception e) {
        if (e != null) e.printStackTrace();
        Map<String, Object> error = new ConcurrentHashMap<>();
        error.put("serverMessageType", ServerMessage.ServerMessageType.ERROR);
        error.put("errorMessage", errorMessage);
        try {
            session.getRemote().sendString(gson.toJson(error));
        } catch (IOException ex) {
            System.err.println("Error sending error message: " + ex.getMessage());
        }
    }

    private ChessGame.TeamColor getPlayerColor(String username, GameData game) {
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            return ChessGame.TeamColor.WHITE;
        } else if (game.blackUsername() != null && game.blackUsername().equals(username)) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }
}
