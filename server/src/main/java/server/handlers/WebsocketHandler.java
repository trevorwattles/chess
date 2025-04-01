package server.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@WebSocket
public class WebsocketHandler {
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
    public void onMessage(Session session, String message) {}

}
