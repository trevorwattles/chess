package server.handlers;

import chess.ChessGame;
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

        Map<String, Object> welcomeMessage = new ConcurrentHashMap<>();
        welcomeMessage.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
        welcomeMessage.put("message", "Welcome!");

        try {
            session.getRemote().sendString(gson.toJson(welcomeMessage));
        } catch (IOException e) {
            System.err.println("Error sending welcome message: " + e.getMessage());
        }
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

    private void handleConnectCommand(Session session, ConnectCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());

            String role;
            if (auth.username().equals(game.whiteUsername())) {
                role = "white";
            } else if (auth.username().equals(game.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }

            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", auth.username() + " has joined the game as " + role);

            broadcastMessage(session, notif, true);

            Map<String, Object> loadGameMessage = new ConcurrentHashMap<>();
            loadGameMessage.put("serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.put("game", game.game());

            sendMessage(session, loadGameMessage);
        } catch (Exception e) {
            sendError(session, "Error while sending game data", e);
        }
    }


    private void handleMoveCommand(Session session, MoveCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());
            ChessGame chessGame = game.game();
            ChessGame.TeamColor playerColor = getPlayerColor(auth.username(), game);
            if(playerColor == null){
                sendError(session, "Error: Observers cannot make moves", new Exception("Observer move attempted"));
                return;
            }
            if(chessGame.isOver()){
                sendError(session, "Error: Game is already over", new Exception("Game over"));
                return;
            }
            if(!chessGame.getTeamTurn().equals(playerColor)){
                sendError(session, "Error: It is not your turn", new Exception("Wrong turn"));
                return;
            }
            chessGame.makeMove(cmd.getMove());
            ChessGame.TeamColor oppColor = (playerColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String notificationText;
            if(chessGame.isInCheckmate(oppColor)){
                chessGame.setOver(true);
                notificationText = "Checkmate! " + auth.username() + " wins!";
            } else if(chessGame.isInStalemate(oppColor)){
                chessGame.setOver(true);
                notificationText = "Stalemate! The game is a draw.";
            } else if(chessGame.isInCheck(oppColor)){
                notificationText = "Check! " + oppColor + " is in check.";
            } else {
                notificationText = auth.username() + " has made a move.";
            }
            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", notificationText);
            broadcastMessage(session, notif, true);
            Map<String, Object> loadGameMessage = new ConcurrentHashMap<>();
            loadGameMessage.put("serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.put("game", chessGame);
            broadcastMessage(session, loadGameMessage, true);
            Server.gameService.updateGame(cmd.getAuthToken(), game);
        } catch(Exception e) {
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
            broadcastMessage(session, notif, true);
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
            ChessGame.TeamColor playerColor = getPlayerColor(auth.username(), game);
            if (playerColor == null) {
                sendError(session, "Error: Observers cannot resign", new Exception("Observer resignation attempted"));
                return;
            }
            chessGame.setOver(true);
            ChessGame.TeamColor oppColor = (playerColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
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
