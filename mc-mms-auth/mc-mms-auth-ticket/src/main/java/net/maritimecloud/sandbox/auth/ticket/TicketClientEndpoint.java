package net.maritimecloud.sandbox.auth.ticket;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The client will set a ticket, in the form of a basic authentication header, when
 * it calls a server endpoint
 */
@ClientEndpoint(configurator = TicketClientEndpoint.ClientEndpointConfigurator.class)
public class TicketClientEndpoint {

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        System.out.println("Client Endpoint Socket Connected: " + sess);
    }
    
    @OnMessage
    public void onWebSocketText(String message) {
        System.out.println("Client Endpoint Received TEXT message: " + message);
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Client Endpoint Socket Closed: " + reason);
    }
    
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }


    /** Configurator class. Adds a basic authentication header to the websocket upgrade request */
    public static class ClientEndpointConfigurator extends
            ClientEndpointConfig.Configurator {
        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            String auth = "Basic " +
                    Base64.getEncoder().encodeToString("user:pass".getBytes(Charset.forName("UTF-8")));
            headers.put("Authorization", Collections.singletonList(auth));
            System.out.println("Client Setting headers " + headers);
            super.beforeRequest(headers);
        }
    }
}
