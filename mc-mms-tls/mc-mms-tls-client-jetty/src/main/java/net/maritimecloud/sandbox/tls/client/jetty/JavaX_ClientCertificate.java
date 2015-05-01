package net.maritimecloud.sandbox.tls.client.jetty;

import net.maritimecloud.sandbox.tls.client.AbstractClient;
import net.maritimecloud.sandbox.tls.client.jetty.util.JavaXAnnotations;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jsr356.ClientContainer;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Use a client SSL certificate.
 * Must be tested against the SSLServerWithClientCertificateValidation server class.
 *
 * <p>The Jetty Websocket client container support for SSL is truly messed up!
 * There is no way to properly configure the associated SslContextFactory</p>
 *
 * <p>Hence, we create out own CustomJettyClientContainerProvider class and use it programmatically.
 * Could also have been loaded via the java service loader mechanism, methinks.</p>
 */
public class JavaX_ClientCertificate extends AbstractClient {
    public static void main(String[] args) throws Exception {

        // Debug
        //System.setProperty("javax.net.debug", "all");


        // The usual way to instantiate a container
        //WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        // Create our own container that handles keystore and truststore properly
        WebSocketContainer container = new CustomJettyClientContainerProvider().getContainer();
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

    /**
     * Create our own ClientContainerProvider and configure the SslContextFactory
     * of the underlying Jetty ClientContainer.
     */
    public static class CustomJettyClientContainerProvider extends ContainerProvider {

        /** {@inheritDoc} */
        @Override
        protected WebSocketContainer getContainer() {
            try {
                ClientContainer container = new ClientContainer();
                SslContextFactory sslContextFactory = container.getClient().getSslContextFactory();

                // Needed because the server uses a self-signed certificate
                sslContextFactory.setTrustStorePath(getResourcePath("client-truststore.jks"));
                sslContextFactory.setTrustStorePassword("changeit");

                // Send a client certificate from our keystore
                sslContextFactory.setKeyStorePath(getResourcePath("client-keystore.jks"));
                sslContextFactory.setKeyManagerPassword("changeit");


                container.start();
                return container;
            } catch (Exception e) {
                throw new RuntimeException("Unable to start Client Container",e);
            }
        }
    }
}
