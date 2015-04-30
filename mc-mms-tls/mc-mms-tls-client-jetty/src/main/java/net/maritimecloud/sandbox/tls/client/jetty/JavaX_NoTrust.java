package net.maritimecloud.sandbox.tls.client.jetty;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.jetty.util.JavaXAnnotations;

import org.eclipse.jetty.util.component.LifeCycle;

public class JavaX_NoTrust extends AbstractClient {
    public static void main(String[] args) throws Exception {
        // Needed because the server uses a self-signed certificate

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            JavaXAnnotations es = new JavaXAnnotations();
            try (Session session = container.connectToServer(es, CUSTOM_WSS)) {
                es.assertDone();
            }
        } finally {
            if (container instanceof LifeCycle) { // proper shutdown
                ((LifeCycle) container).stop();
            }
        }
    }
}
