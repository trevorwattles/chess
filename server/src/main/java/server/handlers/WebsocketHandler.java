package server.handlers;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MoveCommand;
import websocket.commands.ResignCommand;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static websocket.commands.UserGameCommand.CommandType.*;

@WebSocket
public class WebsocketHandler {
    private Gson gson = new Gson();
    private static List<Session> sessions = new CopyOnWriteArrayList<>();
    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        System.out.println("New connection: " + session.getRemoteAddress().getAddress());
        try {
            session.getRemote().sendString("{\"serverMessageType\": \"NOTIFICATION\", \"message\": \"Welcome!\"}");
        } catch (Exception e) {
            System.err.println("Error sending welcome message: " + e.getMessage());
        }
    }
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        System.out.println("Connection closed: " + session.getRemoteAddress().getAddress() +
                " - Code: " + statusCode + ", Reason: " + reason);
    }
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException {
        System.out.println("Received: " + message);
        websocket.commands.UserGameCommand baseCommand = new Gson().fromJson(message, websocket.commands.UserGameCommand.class);
        switch (baseCommand.getCommandType()) {
            case CONNECT:
                ConnectCommand connectCommand = new Gson().fromJson(message, ConnectCommand.class);
                handleConnectCommand(session, connectCommand);
                break;
            case MAKE_MOVE:
                MoveCommand moveCommand = new Gson().fromJson(message, MoveCommand.class);
                handleMoveCommand(session, moveCommand);
                break;
            case LEAVE:
                LeaveCommand leaveCommand = new Gson().fromJson(message, LeaveCommand.class);
                handleLeaveCommand(session, leaveCommand);
                break;
            case RESIGN:
                ResignCommand resignCommand = new Gson().fromJson(message, ResignCommand.class);
                handleResignCommand(session, resignCommand);
                break;
            default:
                System.out.println("Unknown command received.");
                break;
        }
    }

    private void handleConnectCommand(Session session, ConnectCommand cmd) throws DataAccessException {

    }

    private void handleLeaveCommand(Session session, LeaveCommand cmd) {
    }

    private void handleMoveCommand(Session session, MoveCommand cmd) {
    }

    private void handleResignCommand(Session session, ResignCommand cmd) {
    }

}
