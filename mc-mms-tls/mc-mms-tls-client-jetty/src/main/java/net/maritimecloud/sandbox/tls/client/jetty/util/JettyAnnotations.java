package net.maritimecloud.sandbox.tls.client.jetty.util;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class JettyAnnotations {

    public final CountDownLatch done = new CountDownLatch(1);

    @OnWebSocketConnect
    public void onWebSocketConnect(Session sess) throws IOException {
        System.out.println("Client: Socket Connected: ");
        sess.getRemote().sendString("1");
    }

    @OnWebSocketMessage
    public void onWebSocketText(String message) {
        System.out.println("Client: Received TEXT message: " + message);
        if (message.equals("2")) {
            done.countDown();
        }
    }

    public void assertDone() {
        try {
            if (!done.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("No PingPong");
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @OnWebSocketClose
    public void onWebSocketClose(int i, String reason) {
        System.out.println("Client: Socket Closed: " + reason);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

}
