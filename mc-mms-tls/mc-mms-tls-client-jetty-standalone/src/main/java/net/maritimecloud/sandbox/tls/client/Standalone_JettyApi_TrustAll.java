package net.maritimecloud.sandbox.tls.client;

import net.maritimecloud.sandbox.tls.client.util.JettyListener;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class Standalone_JettyApi_TrustAll extends AbstractClient {
    public static void main(String[] args) throws Exception {
        // Setup SSL context
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        client.start();
        try {
            JettyListener se = new JettyListener();
            client.connect(se, WSS);
            se.assertDone();
        } finally {
            client.stop();
        }
    }
}
