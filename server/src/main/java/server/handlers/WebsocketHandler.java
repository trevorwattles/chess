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

import javax.management.Notification;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@WebSocket
public class WebsocketHandler {
    private static final Gson gson = new Gson();
    private static final List<Session> sessions = new CopyOnWriteArrayList<>();
    private static final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        gameSessions.put(session, 0);
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
        sessions.remove(session);
        gameSessions.remove(session);
        System.out.println("Connection closed: " + session.getRemoteAddress().getAddress() +
                " - Code: " + statusCode + ", Reason: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException {
        System.out.println("Received: " + message);
        UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);

        switch (baseCommand.getCommandType()) {
            case CONNECT -> handleConnectCommand(session, gson.fromJson(message, ConnectCommand.class));
            case MAKE_MOVE -> handleMoveCommand(session, gson.fromJson(message, MoveCommand.class));
            case LEAVE -> handleLeaveCommand(session, gson.fromJson(message, LeaveCommand.class));
            case RESIGN -> handleResignCommand(session, gson.fromJson(message, ResignCommand.class));
            default -> System.out.println("Unknown command received.");
        }
    }

    private void handleConnectCommand(Session session, ConnectCommand cmd) {
        try {
            AuthData auth = Server.userService.getAuthData(cmd.getAuthToken());
            GameData game = Server.gameService.getGameData(cmd.getAuthToken(), cmd.getGameID());

            gameSessions.put(session, cmd.getGameID());

            String role;
            if (auth.username().equals(game.whiteUsername())) {
                role = "white";
            } else if (auth.username().equals(game.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }

            // Manually send the notification message
            Map<String, Object> notif = new ConcurrentHashMap<>();
            notif.put("serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            notif.put("message", auth.username() + " has joined the game as " + role);
            session.getRemote().sendString(gson.toJson(notif));

            // Manually send the load game message
            Map<String, Object> loadGameMessage = new ConcurrentHashMap<>();
            loadGameMessage.put("serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME);
            loadGameMessage.put("game", game.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }




    private void handleMoveCommand(Session session, MoveCommand cmd) {

    }

    private void handleLeaveCommand(Session session, LeaveCommand cmd) {

    }

    private void handleResignCommand(Session session, ResignCommand cmd) {

    }


    private void sendError(Session session, String errorMessage) {
        Map<String, Object> error = new ConcurrentHashMap<>();
        error.put("serverMessageType", ServerMessage.ServerMessageType.ERROR);
        error.put("errorMessage", errorMessage);
        try {
            session.getRemote().sendString(gson.toJson(error));
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

}