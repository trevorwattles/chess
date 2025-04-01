package server.handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WebsocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session session) {}
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {}
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {}

}
