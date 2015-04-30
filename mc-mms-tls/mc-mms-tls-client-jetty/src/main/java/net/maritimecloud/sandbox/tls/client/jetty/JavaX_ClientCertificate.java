package net.maritimecloud.sandbox.tls.client.jetty;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.jetty.util.JavaXAnnotations;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * IMPORTANT: The clien certificate test does NOT work yet!
 *
 * Use a client SSL certificate.
 * Must be tested against the SSLServerWithClientCertificateValidation server class.
 */
public class JavaX_ClientCertificate extends AbstractClient {
    public static void main(String[] args) throws Exception {

        // Debug
        //System.setProperty("javax.net.debug", "all");

        // Needed because the server uses a self-signed certificate
        System.setProperty("javax.net.ssl.trustStore", getResourcePath("client-truststore.jks"));
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // Send a client certificate from our keystore
        System.setProperty("javax.net.ssl.keyStore", getResourcePath("client-keystore.jks"));
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            JavaXAnnotations es = new JavaXAnnotations();
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
