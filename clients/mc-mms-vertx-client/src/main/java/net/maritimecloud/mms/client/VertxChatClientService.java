package net.maritimecloud.mms.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A Vert.x chat interface
 */
public class VertxChatClientService implements ChatClientService<ServerWebSocket>, Handler<ServerWebSocket> {

    private static final Logger LOG = LoggerFactory.getLogger(VertxChatClientService.class);

    private EventBus eventBus;
    private final MmsChatClientService mmsChatClientService;
    private final Map<String, ServerWebSocket> wsClients = new ConcurrentHashMap<>();
    private final String html;

    /** Constructor */
	public VertxChatClientService(MmsChatClientService mmsChatClientService) {
        this.mmsChatClientService = Objects.requireNonNull(mmsChatClientService);

        html = loadHtml("/ws2.html");

        Vertx vertx = VertxFactory.newVertx();
        eventBus = vertx.eventBus();

        // Set up web server
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if (req.path().equals("/")) {
                        req.response().putHeader("Content-Length", String.valueOf(html.length()));
                        req.response().write(html);
                    }
                })
                .listen(8080);

        // Set up websocket handler
        vertx.createHttpServer()
                .websocketHandler(this)
                .listen(8090);
    }

    /** Loads the html from the class path */
    private String loadHtml(String file) {
        StringBuilder out = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file)))) {
            String line;
            while ((line = in.readLine()) != null) {
                out.append(line).append("\n");
            }
        } catch (Exception e) {
            LOG.error("Error reading html ", e);
        }
        return out.toString();
    }

    /**  {@inheritDoc} */
    @Override
    public synchronized void addChatClient(String id, final ServerWebSocket ws, final Consumer<ChatMessage> consumer) {
        if (hasClient(id)) {
            throw new IllegalArgumentException("Chat client for " + id + " already exists");
        }
        wsClients.put(id, ws);
        LOG.info("Added client for ID " + id);

        mmsChatClientService.addChatClient(id, null, m -> eventBus.send(ws.textHandlerID(), m.toJson()));
    }

    /**  {@inheritDoc} */
    @Override
    public synchronized void removeClient(String id) {
        if (hasClient(id)) {
            try {
                wsClients.remove(id).close();
            } catch (Exception e) {
            }
            LOG.info("Removed client for ID " + id);
        }

        mmsChatClientService.removeClient(id);
    }

    /**  {@inheritDoc} */
    @Override
    public boolean hasClient(String id) {
        return id != null && wsClients.containsKey(id);
    }

    /**  {@inheritDoc} */
    @Override
    public void sendChatMessage(ChatMessage msg) {
        mmsChatClientService.sendChatMessage(msg);
        LOG.info("Sent message: " + msg);
    }

    /** Returns the MMSI associated with the websocket with the given handler id */
    private String getIdFromWebsocket(String wsId) {
        return wsClients.entrySet()
                .stream()
                .filter(c -> c.getValue().textHandlerID().equals(wsId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /** Closes the websocket and any associated client */
    private void closeWebsocket(ServerWebSocket ws) {
        String id = getIdFromWebsocket(ws.textHandlerID());
        if (id != null) {
            removeClient(id);
        } else {
            try {
                ws.close();
            } catch (Exception e) {
            }
        }
    }

    /** Handles the server websocket */
    @Override
    public void handle(final ServerWebSocket ws) {
        ws.dataHandler(data -> {
            try {
                JsonObject json = new JsonObject(data.toString());
                String messageType = json.getString("type");
                String endpointType = json.getString("endpointType");
                if (messageType != null) {
                    if (messageType.equalsIgnoreCase("connect")) {
                        int mmsi = json.getNumber("source").intValue();
                        addChatClient(String.valueOf(mmsi), ws, null);
                    }
                } else if (endpointType.equalsIgnoreCase("dma.messaging.MaritimeTextingService.sendMessage")) {
                    ChatMessage msg = ChatMessage.fromJson(json);
                    sendChatMessage(msg);
                }
            } catch (org.vertx.java.core.json.DecodeException ex) {
                LOG.error("Error handling websocket ", ex);
                closeWebsocket(ws);

            }
        });
        ws.closeHandler(p -> closeWebsocket(ws));
    }
}
