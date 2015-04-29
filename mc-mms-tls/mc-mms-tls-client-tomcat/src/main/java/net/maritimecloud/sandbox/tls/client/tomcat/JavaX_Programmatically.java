package net.maritimecloud.sandbox.tls.client.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.tomcat.util.JavaXEndpoint;

import org.apache.tomcat.websocket.WsWebSocketContainer;

public class JavaX_Programmatically extends AbstractClient {

    static TrustManager[] loadTrustStore() throws Exception {
        String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory instance = TrustManagerFactory.getInstance(defaultAlgorithm);
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        try (FileInputStream instream = new FileInputStream(new File("src/main/resources/client-truststore.jks"))) {
            trustStore.load(instream, "changeit".toCharArray());
        }

        instance.init(trustStore);
        return instance.getTrustManagers();
    }

    public static void main(String[] args) throws Exception {
        // Setup SSL context
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, loadTrustStore(), null);
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
