package net.maritimecloud.sandbox.tls.client.jetty.util;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class JavaXAnnotations {

    public final CountDownLatch done = new CountDownLatch(1);

    @OnOpen
    public void onWebSocketConnect(Session sess) throws IOException {
        System.out.println("Client: Socket Connected: ");
        sess.getBasicRemote().sendText("1");
    }

    @OnMessage
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

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        System.out.println("Client: Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

}
