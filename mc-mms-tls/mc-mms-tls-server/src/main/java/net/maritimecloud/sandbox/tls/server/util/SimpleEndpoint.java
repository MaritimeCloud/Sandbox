package net.maritimecloud.sandbox.tls.server.util;

import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

@ServerEndpoint(value = "/websockettest/")
public class SimpleEndpoint {

    Session sess;
    Principal principal;

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        System.out.println("Socket Connected: ");
        this.sess = sess;

        try {
            // TODO: Is there a non-jetty way of doing this?
            WebSocketSession jettySession = (WebSocketSession)sess;
            ServletUpgradeRequest upgradeRequest = (ServletUpgradeRequest)jettySession.getUpgradeRequest();
            X509Certificate[] certs = upgradeRequest.getCertificates();
            if (certs != null && certs.length > 0) {
                X509Certificate cert = certs[0];
                principal = cert.getSubjectDN();
                System.out.println("Client Connected with trusted SSL certificate: " + principal);
            }
        } catch (Exception e) {
            System.err.println("Error inspecting client certificates " + e);
        }

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
