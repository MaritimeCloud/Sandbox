package net.maritimecloud.sandbox.auth.ticket;

import javax.websocket.CloseReason;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Base64;

/**
 * The server will receive a ticket, in the form of a basic authentication header, from
 * the client and turn it into a principal
 */
@ServerEndpoint(value="/ticket/", configurator = TicketServerEndpoint.ServerEndpointConfigurator.class)
public class TicketServerEndpoint {

    Principal principal;

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        principal = (Principal) sess.getUserProperties().get(Main.PRINCIPAL_KEY);
        System.out.println("********** Server Endpoint Principal: " + principal);
    }
    
    @OnMessage
    public void onWebSocketText(String message) {
        System.out.println("Server Endpoint Received TEXT message: " + message);
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Server Endpoint Socket Closed: " + reason);
    }
    
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

    /** Configurator class. Fetches a basic authentication header from the websocket upgrade request and sets up a Principal */
    public static class ServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig config,
                                    HandshakeRequest request,
                                    HandshakeResponse response) {
            // First check if the servlet container handled the authentication
            Principal principal = request.getUserPrincipal();
            if (principal == null && request.getHeaders().containsKey("Authorization")) {
                String auth = request.getHeaders().get("Authorization").get(0);
                auth = auth.substring("Basic ".length());
                auth = new String(Base64.getDecoder().decode(auth), Charset.forName("UTF-8"));
                final String name =auth.substring(0, auth.indexOf(":"));
                principal = new Principal() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String toString() {
                        return name;
                    }
                };
            }

            config.getUserProperties().put(Main.PRINCIPAL_KEY, principal);

            super.modifyHandshake(config, request, response);
        }
    }

}
