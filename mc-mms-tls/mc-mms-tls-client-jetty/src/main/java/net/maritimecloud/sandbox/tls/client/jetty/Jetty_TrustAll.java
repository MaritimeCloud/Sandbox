package net.maritimecloud.sandbox.tls.client.jetty;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.jetty.util.JettyAnnotations;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class Jetty_TrustAll extends AbstractClient {
    public static void main(String[] args) throws Exception {
        // Setup SSL context
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        client.start();
        try {
            JettyAnnotations se = new JettyAnnotations();
            client.connect(se, WSS);
            se.assertDone();
        } finally {
            client.stop();
        }
    }
}
