package net.maritimecloud.sandbox.tls.client;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import net.maritimecloud.sandbox.tls.client.util.JavaXListener;

import org.eclipse.jetty.util.component.LifeCycle;

public class Standalone_JavaX_Trust_SystemProperties extends AbstractClient {
    public static void main(String[] args) throws Exception {

        // Needed because the server uses a self-signed certificate
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            // Attempt Connect
            JavaXListener es = new JavaXListener();
            try (Session session = container.connectToServer(es, WSS)) {
                es.assertDone();
            }
        } finally {
            if (container instanceof LifeCycle) { // proper shutdown
                ((LifeCycle) container).stop();
            }
        }
    }
}
