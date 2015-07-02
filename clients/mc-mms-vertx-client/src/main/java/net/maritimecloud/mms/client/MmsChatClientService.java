package net.maritimecloud.mms.client;

import dma.messaging.AbstractMaritimeTextingService;
import dma.messaging.MaritimeText;
import dma.messaging.MaritimeTextingService;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Main MMS Chat interface.
 */
public class MmsChatClientService implements ChatClientService<MmsClient> {

    private static final Logger LOG = LoggerFactory.getLogger(MmsChatClientService.class);
    public static String MMS_HOST = "ws://mms.sandbox04.maritimecloud.net:80";

    private Map<String, MmsClient> mmsClients = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public synchronized void addChatClient(final String id, MmsClient dummy, final Consumer<ChatMessage> consumer) {
        if (hasClient(id)) {
            throw new IllegalArgumentException("Chat client for " + id + " already exists");
        }

        // Start the client - throws a RuntimeException in case of failure
        MmsClient mmsClient = MmsClientConfiguration.create(new MmsiId(Integer.valueOf(id)))
                .setHost(MMS_HOST)
                .setPositionReader(new PositionReader() {
                    @Override
                    public PositionTime getCurrentPosition() {
                        return PositionTime.create(55, 11, System.currentTimeMillis());
                    }
                })
                .build();

        // Listen for incoming chat messages
        mmsClient.endpointRegister(new AbstractMaritimeTextingService() {
            @Override
            protected void sendMessage(MessageHeader header, MaritimeText msg) {
                consumer.accept(ChatMessage.fromMms(header.getSender().getId(), id, msg));
            }
        });

        mmsClients.put(id, mmsClient);
        LOG.info("Added client with ID " + id);
    }

    /**  {@inheritDoc} */
    @Override
    public synchronized void removeClient(String id) {
        if (hasClient(id)) {
            shutdownClient(mmsClients.remove(id));
            LOG.info("Removed client with ID " + id);
        }
    }

    /**  {@inheritDoc} */
    @Override
    public boolean hasClient(String id) {
        return id != null && mmsClients.containsKey(id);
    }

    /**  {@inheritDoc} */
    @Override
    public void sendChatMessage(ChatMessage msg) {
        if (hasClient(msg.getSenderId())) {
            MmsClient sender = mmsClients.get(msg.getSenderId());
            MaritimeId targetId = new MmsiId(Integer.valueOf(msg.getReceiverId()));
            sender.endpointCreate(targetId, MaritimeTextingService.class)
                .sendMessage(msg.toMms());
            LOG.info("Sent message: " + msg);
        }
    }

    /**
     * Stops the MMS connections.
     */
    @SuppressWarnings("unused")
    public synchronized void shutdown() {
        mmsClients.values()
                .parallelStream()
                .forEach(this::shutdownClient);
        mmsClients.clear();
    }

    /** Shuts down the given client */
    private void shutdownClient(MmsClient mmsClient) {
        try {
            mmsClient.shutdown();
            mmsClient.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
