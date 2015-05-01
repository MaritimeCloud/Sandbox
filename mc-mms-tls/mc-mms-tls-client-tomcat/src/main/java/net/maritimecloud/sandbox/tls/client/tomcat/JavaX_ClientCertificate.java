package net.maritimecloud.sandbox.tls.client.tomcat;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.tomcat.util.JavaXEndpoint;
import org.apache.tomcat.websocket.WsWebSocketContainer;

import javax.net.ssl.SSLContext;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Use a client SSL certificate.
 * Must be tested against the SSLServerWithClientCertificateValidation server class.
 *
 * <p>As with the Jetty-version, we need to use a proprietary Tomcat API in order to configure
 * the SSLContext with proper keystore and truststore details.</p>
 * <p>A side-effect is that we can only test with the JavaXEndpoint class, not the JavaXAnnotations class :-(</p>
 */
public class JavaX_ClientCertificate extends AbstractClient {

    public static void main(String[] args) throws Exception {

        // Debug
        //System.setProperty("javax.net.debug", "all");

        // Sadly, we need to use a Tomcat-specific API to configure the SSL context
        // with proper keystore and truststore details.
        // TODO: Are there potential side-effects if this is done in a general web-application
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(loadKeyManagers(), loadTrustStore(), null);
        config.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", context);


        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            JavaXEndpoint es = new JavaXEndpoint();
            try (Session session = container.connectToServer(es, config, WSS)) {
                es.assertDone();
            }
        } finally {
            if (container instanceof WsWebSocketContainer) { // proper shutdown
                ((WsWebSocketContainer) container).destroy();
            }
        }
    }
}
