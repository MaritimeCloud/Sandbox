package net.maritimecloud.sandbox.tls.client.tomcat;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.tomcat.util.JavaXAnnotations;
import org.apache.tomcat.websocket.WsWebSocketContainer;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class JavaX_SystemProperties extends AbstractClient {
    public static void main(String[] args) throws Exception {

        // Needed because the server uses a self-signed certificate
        System.setProperty("javax.net.ssl.trustStore", getResourcePath("client-truststore.jks"));
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            JavaXAnnotations es = new JavaXAnnotations();
            try (Session session = container.connectToServer(es, WSS)) {
                es.assertDone();
            }
        } finally {
            if (container instanceof WsWebSocketContainer) { // proper shutdown
                ((WsWebSocketContainer) container).destroy();
            }
        }
    }
}
