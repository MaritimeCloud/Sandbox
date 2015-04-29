package net.maritimecloud.sandbox.tls.server.util;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websockettest/")
public class SimpleEndpoint {

    Session sess;

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        System.out.println("Socket Connected: ");
        this.sess = sess;
    }

    @OnMessage
    public void onWebSocketText(String message) throws IOException {
        System.out.println("Received TEXT message: " + message);
        try {
            int i = Integer.parseInt(message);
            sess.getBasicRemote().sendText("" + (i + 1));
        } catch (NumberFormatException ignore) {}

    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
}
