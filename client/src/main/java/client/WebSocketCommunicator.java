package client;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;

import java.net.URI;
import java.util.function.Consumer;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class WebSocketCommunicator {
    private final String serverUrl;
    private final String authToken;
    private Session session;
    private final Gson gson = new Gson();

    private Consumer<ChessGame> gameUpdateHandler;
    private Consumer<String> notificationHandler;
    private Consumer<String> errorHandler;

    public WebSocketCommunicator(String serverUrl, String authToken) {
        this.serverUrl = serverUrl.replace("http:", "ws:") + "/ws";
        this.authToken = authToken;
    }

    public void connect(int gameID) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        GameplaySocket socket = new GameplaySocket();
        session = container.connectToServer(socket, new URI(serverUrl));

        ConnectCommand connectCmd = new ConnectCommand(authToken, gameID);
        sendCommand(connectCmd);
    }


    public void makeMove(int gameID, ChessMove move) throws Exception {
        MoveCommand moveCmd = new MoveCommand(authToken, gameID, move);
        sendCommand(moveCmd);
    }

    public void leaveGame(int gameID) throws Exception {
        LeaveCommand leaveCmd = new LeaveCommand(authToken, gameID);
        sendCommand(leaveCmd);
    }

    public void resignGame(int gameID) throws Exception {
        ResignCommand resignCmd = new ResignCommand(authToken, gameID);
        sendCommand(resignCmd);
    }

    private void sendCommand(UserGameCommand command) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new Exception("WebSocket not connected");
        }
        String json = gson.toJson(command);
        session.getAsyncRemote().sendText(json);
    }

    public void setGameUpdateHandler(Consumer<ChessGame> handler) {
        this.gameUpdateHandler = handler;
    }

    public void setNotificationHandler(Consumer<String> handler) {
        this.notificationHandler = handler;
    }

    public void setErrorHandler(Consumer<String> handler) {
        this.errorHandler = handler;
    }

    @ClientEndpoint
    public class GameplaySocket {
        @OnOpen
        public void onOpen(Session session) {
            System.out.println("Connected to WebSocket server");
        }

        @OnMessage
        public void onMessage(String msg) {
            try {
                ServerMessage baseMessage = gson.fromJson(msg, ServerMessage.class);

                switch (baseMessage.getServerMessageType()) {
                    case LOAD_GAME:
                        if (gameUpdateHandler != null) {
                            LoadGame loadGame = gson.fromJson(msg, LoadGame.class);
                            gameUpdateHandler.accept(loadGame.getGame());
                        }
                        break;
                    case NOTIFICATION:
                        if (notificationHandler != null) {
                            Notification notification = gson.fromJson(msg, Notification.class);
                            notificationHandler.accept(notification.getMessage());
                        }
                        break;
                    case ERROR:
                        if (errorHandler != null) {
                            ErrorMessage error = gson.fromJson(msg, ErrorMessage.class);
                            errorHandler.accept(error.getErrorMessage());
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing WebSocket message: " + e.getMessage());
            }
        }
    }
}
