/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.internal.client.mms;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.client.configuration.BroadcastConfiguration;
import net.maritimecloud.internal.client.configuration.ClientConfiguration;
import net.maritimecloud.internal.client.destination.Destination;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.destination.FileManager;
import net.maritimecloud.internal.client.serialization.Serializer;
import net.maritimecloud.internal.mms.client.DefaultMmsClient;
import net.maritimecloud.internal.mms.client.broadcast.BroadcastDeserializer;
import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.internal.msdl.dynamic.DynamicBroadcastMessage;
import net.maritimecloud.internal.util.logging.Logger;
import net.maritimecloud.msdl.model.BroadcastMessageDeclaration;
import net.maritimecloud.net.BroadcastConsumer;
import net.maritimecloud.net.BroadcastMessage;
import net.maritimecloud.net.BroadcastSubscription;
import net.maritimecloud.net.DispatchedMessage;
import net.maritimecloud.net.mms.MmsBroadcastOptions;
import net.maritimecloud.util.Binary;
import net.maritimecloud.util.geometry.Area;

import org.cakeframework.container.concurrent.ScheduleAtFixedRate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 *
 * @author Kasper Nielsen
 */
public class BroadcastManager {

    /** The logger. */
    private static final Logger LOG = Logger.get(BroadcastManager.class);

    /** The database containing all valid MSDL files. */
    private final DefaultMsdlDatabase database;

    /** The MMS client. */
    private final DefaultMmsClient mmsClient;

    /** A cache of statuses, that times out after 1 hour. */
    private final Cache<String, SendBroadcastStatus> statuses = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS).build();

    /** A map of current subscriptions. */
    private final ConcurrentHashMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    private final Serializer serializer;

    private final DestinationManager dm;

    private final FileManager fm;

    public BroadcastManager(ClientConfiguration configuration, DefaultMmsClient client, DefaultMsdlDatabase database,
            Serializer serializer, DestinationManager dm, FileManager fm) throws URISyntaxException {
        this.mmsClient = requireNonNull(client);
        this.database = requireNonNull(database);
        this.serializer = requireNonNull(serializer);
        this.dm = requireNonNull(dm);
        this.fm = requireNonNull(fm);
        for (BroadcastConfiguration.BroadcastGroup g : configuration.getBroadcasts().getGroups()) {
            for (URL u : g.getDestinations()) {
                Destination d = dm.create(u.toString());
                for (BroadcastMessageDeclaration b : g.getBroadcastTypes()) {
                    broadcastSubscribe(b, d, null);
                }
            }
        }

        fm.subscribe(configuration.getHome().resolve(FileManager.FILEIO_OUT_BROADCAST), f -> broadcastFile(f));
    }

    void broadcastFile(Path p) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            String s = new String(bytes, StandardCharsets.UTF_8);
            new BroadcastDispatch(dm, serializer, this).fromJSONFile(fm, s, p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BroadcastSubscription broadcastSubscribe(BroadcastMessageDeclaration bmd, Destination destination, Area area) {
        // Takes care of parsing any broadcast message that is received into an internal format
        BroadcastDeserializer bd = (t, r) -> DynamicBroadcastMessage.readFrom(bmd, r);

        // Takes care of writing a message from the internal to the requested destination.
        BroadcastConsumer<DynamicBroadcastMessage> bc = (h, m) -> {
            String msg = serializer.writeBroadcast(m, h);
            if (msg != null) { // returns null if the message could not be written
                destination.send("broadcast_" + h.getMessageId().hexString(), msg);
            }
        };

        LOG.info("Subscribed broadcast of type '" + bmd.getFullName() + "' to " + destination);
        return mmsClient.broadcastSubscribe(bd, bmd.getFullName(), bc, area);
    }

    /** Helps cleanup old statuses. */
    @ScheduleAtFixedRate(value = 10, unit = TimeUnit.MINUTES)
    public void cleanup() {
        statuses.cleanUp();
    }

    public Optional<BroadcastMessageDeclaration> getBroadcastDefinition(String broadcastType) {
        return Optional.ofNullable(database.getBroadcastMessage(broadcastType));
    }

    /**
     * Returns the current status of the broadcast with the specified id. The broadcast is tracked for 1 hour after it
     * has been sent.
     *
     * @param broadcastHexId
     *            the id of the broadcast
     * @return an optional status
     */
    public Optional<SendBroadcastStatus> getBroadcastStatus(String broadcastHexId) {
        return Optional.ofNullable(statuses.getIfPresent(broadcastHexId));
    }

    public Optional<Subscription> getSubscription(String subscriptionId) {
        return Optional.ofNullable(subscriptions.get(subscriptionId));
    }

    public Binary sendBroadcast(BroadcastMessage m, MmsBroadcastOptions options, String attachmentId) {
        DispatchedMessage dm = mmsClient.broadcast(m, options);
        statuses.put(dm.getMessageId().hexString(), new SendBroadcastStatus(dm));
        if (attachmentId != null) {

        }
        return dm.getMessageId();
    }

    public static class SendBroadcastStatus {
        final DispatchedMessage m;

        SendBroadcastStatus(DispatchedMessage m) {
            this.m = m;
        }

        public DispatchedMessage getMessage() {
            return m;
        }
    }

    public static class Subscription {
        final Destination d;

        Subscription(BroadcastSubscription bs, Destination d) {
            this.d = d;
        }

        public void cancel() {

        }
    }
}
