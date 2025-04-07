package server.handlers;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
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
                    System.out.println("Parsed gameID: " + connectCommand.getGameID());
                    int gameID = connectCommand.getGameID();
                    Server.gameSessionsMap.replace(session, gameID);
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
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("✔ Initialized ChessGame for game ID: " + cmd.getGameID());
            }
            String role;
            if (auth.username().equals(game.whiteUsername())) {
                role = "white";
            } else if (auth.username().equals(game.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }
            Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), role));
            broadcastMessage(session, notif, false);
            LoadGame load = new LoadGame(game.game());
            sendMessage(session, load);
        } catch (Exception e) {
            sendError(session, "Error: Not authorized", e);
        }
    }

    private void handleMoveCommand(Session session, MoveCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            if (game == null || game.game() == null) {
                sendError(session, "Error: Game not found or not initialized", new Exception("Game or ChessGame is null"));
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
                sendError(session, "Error: Observers cannot make moves", new Exception("Observer move attempted"));
                return;
            }
            if (game.game().isOver()) {
                sendError(session, "Error: Game is already over", new Exception("Game over"));
                return;
            }
            if (!game.game().getTeamTurn().equals(playerColor)) {
                sendError(session, "Error: It is not your turn", new Exception("Wrong turn"));
                return;
            }
            game.game().makeMove(move);
            // Check post-move state.
            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
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
            // Broadcast move notification to everyone except the mover.
            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", notification);
            broadcastMessage(session, notif, false);
            // Broadcast updated game state to all (including sender).
            Map<String, Object> loadGameMessage = new ConcurrentHashMap<>();
            loadGameMessage.put("serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.put("game", game.game());
            broadcastMessage(session, loadGameMessage, true);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch (Exception e) {
            sendError(session, "Error processing move", e);
        }
    }

    private void handleLeaveCommand(Session session, LeaveCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            String username = auth.username();
            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", username + " has left the game.");
            broadcastMessage(session, notif, false);
            if (username.equals(game.whiteUsername())) {
                game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            } else if (username.equals(game.blackUsername())) {
                game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            }
            Server.gameSessionsMap.remove(session);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch(Exception e) {
            sendError(session, "Error processing leave command", e);
        }
    }

    private void handleResignCommand(Session session, ResignCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            ChessGame chessGame = game.game();
            if (chessGame.isOver()) {
                sendError(session, "Error: Game is already over", new Exception("Resign attempted after game over"));
                return;
            }
            ChessGame.TeamColor playerColor = getPlayerColor(auth.username(), game);
            if (playerColor == null) {
                sendError(session, "Error: Observers cannot resign", new Exception("Observer resignation attempted"));
                return;
            }
            chessGame.setOver(true);
            ChessGame.TeamColor oppColor = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String notificationText = auth.username() + " has resigned. Team " + oppColor + " wins!";
            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", notificationText);
            broadcastMessage(session, notif, true);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch(Exception e) {
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
            if (Server.gameSessionsMap.get(session).equals(gameID)) {
                if (includeSender || !session.equals(sender)) {
                    session.getRemote().sendString(json);
                }
            }
        }
    }

    private void sendError(Session session, String errorMessage, Exception e) {
        e.printStackTrace();
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
