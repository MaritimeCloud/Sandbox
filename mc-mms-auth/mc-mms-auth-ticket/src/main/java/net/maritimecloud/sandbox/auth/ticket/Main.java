package net.maritimecloud.sandbox.auth.ticket;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * Test communicating a ticket over websockets via a header in the upgrade request.
 */
public class Main {

    public static final String PRINCIPAL_KEY = "mms.Principal";
    public static CountDownLatch SERVER_START = new CountDownLatch(1);
    public static CountDownLatch TICKET_TEST = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        // Set up server
        new Thread(Main::setupServer).start();

        // Set up client
        SERVER_START.await();
        setupClient();
    }

    /** Sets up websocket client and tests it */
    private static void setupClient() {
        URI uri = URI.create("ws://localhost:8080/ticket/");

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try  {
                // Attempt Connect
                Session session = container.connectToServer(TicketClientEndpoint.class, uri);
                // Send a message
                session.getBasicRemote().sendText("Hello");
                // Close session
                session.close();
            } finally  {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle) {
                    ((LifeCycle)container).stop();
                }
            }
            TICKET_TEST.countDown();

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }

    }


    /** Sets up websocket server */
    public static void setupServer() {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        try {
            // Initialize javax.websocket layer
            javax.websocket.server.ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(TicketServerEndpoint.class);

            server.start();

            SERVER_START.countDown();
            TICKET_TEST.await();
            server.stop();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }



}
