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
        // Instead of maintaining a local sessions list, update the Server's gameSessionsMap.
        // For initial connection, you can set gameID to 0 (or another default value).
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
        // Implement move logic here.
    }

    private void handleLeaveCommand(Session session, LeaveCommand cmd) {
        // Implement leave logic here.
    }

    private void handleResignCommand(Session session, ResignCommand cmd) {
        // Implement resign logic here.
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
}
