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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.internal.client.configuration.ClientConfiguration;
import net.maritimecloud.internal.client.configuration.ServiceConfiguration.ServiceGroup;
import net.maritimecloud.internal.client.destination.Destination;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.destination.FileManager;
import net.maritimecloud.internal.client.serialization.Serializer;
import net.maritimecloud.internal.mms.client.DefaultMmsClient;
import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.internal.msdl.dynamic.AbstractAsynchronousDynamicEndpointImplementation;
import net.maritimecloud.internal.msdl.dynamic.DynamicMessage;
import net.maritimecloud.message.ValueSerializer;
import net.maritimecloud.msdl.model.EndpointDefinition;
import net.maritimecloud.msdl.model.EndpointMethod;
import net.maritimecloud.net.BroadcastMessage;
import net.maritimecloud.net.DispatchedMessage;
import net.maritimecloud.net.EndpointInvocationFuture;
import net.maritimecloud.net.EndpointRegistration;
import net.maritimecloud.net.MessageHeader;
import net.maritimecloud.net.mms.MmsBroadcastOptions;
import net.maritimecloud.util.Binary;

/**
 *
 * @author Kasper Nielsen
 */
public class EndpointManager {

    /** The database containing all valid MSDL files. */
    private final DefaultMsdlDatabase database;

    /** The MMS client. */
    private final DefaultMmsClient mmsClient;

    /** A map of current subscriptions. */
    private final ConcurrentHashMap<String, Registration> registrations = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Registration> invocations = new ConcurrentHashMap<>();

    final ConcurrentHashMap<String, Ip> inv = new ConcurrentHashMap<>();

    private final DestinationManager dm;

    final Serializer serializer;

    private final FileManager fm;

    public EndpointManager(DefaultMmsClient client, DefaultMsdlDatabase database, ClientConfiguration configuration,
            DestinationManager dm, Serializer serializer, FileManager fm) throws URISyntaxException {
        this.mmsClient = requireNonNull(client);
        this.database = requireNonNull(database);
        this.dm = requireNonNull(dm);
        this.fm = requireNonNull(fm);

        // for (ServiceGroup s : configuration.getEndpoints().getGroups()) {
        // for ()
        // EndpointMethod m = getEndpointDefinition(e.getKey()).orElseThrow(
        // () -> new IllegalArgumentException("Unknown service endpoint '" + e.getKey() + "'"));
        // Destination d;
        // try {
        // d = dm.create(e.getValue());
        // } catch (URISyntaxException e1) {
        // throw new IllegalArgumentException("Illegal endpoint '" + e.getValue() + "'");
        // }
        // }
        this.serializer = requireNonNull(serializer);

        for (ServiceGroup g : configuration.getEndpoints().getGroups()) {
            URL u = g.getDestination();
            Destination d = dm.create(u.toString());
            for (EndpointDefinition b : g.getEndpoints()) {
                endpointSubscribe(b, d);
            }
        }
        fm.subscribe(configuration.getHome().resolve(FileManager.FILEIO_OUT_ENDPOINT), f -> endpointFile(f));
        fm.subscribe(configuration.getHome().resolve(FileManager.FILEIO_IN_ENDPOINT), f -> endpointFileIn(f));
    }

    public EndpointRegistration endpointSubscribe(EndpointDefinition bmd, Destination destination) {

        // Takes care of parsing any broadcast message that is received into an internal format

        AbstractAsynchronousDynamicEndpointImplementation i = new AbstractAsynchronousDynamicEndpointImplementation(bmd) {

            @Override
            protected void invoke(EndpointMethod em, MessageHeader h, DynamicMessage dm, CompletableFuture<Object> cf) {

                String msg = serializer.writeService(em, h, dm);

                if (msg != null) { // returns null if the message could not be written
                    inv.put("endpoint_" + h.getMessageId().hexString() + ".response", new Ip(cf, em));
                    destination.send("endpoint_" + h.getMessageId().hexString(), msg);
                }


                // Watch for file
            }
        };


        //
        // AbstractDynamicEndpointImplementation i = new AbstractDynamicEndpointImplementation(bmd) {
        //
        // @Override
        // protected ValueReader invoke(EndpointMethod paramEndpointMethod, MessageHeader h,
        // DynamicMessage paramDynamicMessage) {
        // String msg = serializer.writeService(paramEndpointMethod, h, paramDynamicMessage);
        // if (msg != null) { // returns null if the message could not be written
        // destination.send("endpoint_" + h.getMessageId().hexString(), msg);
        // }
        // return null;
        // }
        // };
        // // Takes care of writing a message from the internal to the requested destination.
        // BroadcastConsumer<DynamicBroadcastMessage> bc = (h, m) -> {
        // String msg = serializer.writeBroadcast(m, h);
        // if (msg != null) { // returns null if the message could not be written
        // destination.send("broadcast_" + h.getMessageId().hexString(), msg);
        // }
        // };

        // LOG.info("Subscribed broadcast of type '" + bmd.getFullName() + "' to " + destination);

        // mmsClient.en
        return mmsClient.endpointRegister(i);
    }

    void endpointFileIn(Path p) {
        if (p.getFileName().toString().endsWith(".response")) {
            Ip cf = inv.get(p.getFileName().toString());

            if (cf == null) {
                // log it??
                return;
            }

            try {
                byte[] bytes = Files.readAllBytes(p);
                // Void hack
                String s = new String(bytes, StandardCharsets.UTF_8);

                if (cf.m.getReturnType() == null) {
                    cf.cf.complete(null);
                } else {
                    ValueSerializer resultSerializer = DynamicMessage.valueSerializer(cf.m.getReturnType());
                    // TODO fix
                    cf.cf.complete(123);
                }


                Files.delete(p);
            } catch (IOException e) {
                e.printStackTrace();
                cf.cf.completeExceptionally(e);
            }
        }
    }

    void endpointFile(Path p) {
        if (p.getFileName().toString().endsWith(".request")) {
            try {
                byte[] bytes = Files.readAllBytes(p);
                String s = new String(bytes, StandardCharsets.UTF_8);
                new ServiceInvoke(dm, serializer, this, mmsClient).fromJSONFile(fm, s, p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<EndpointMethod> getEndpointDefinition(String endpointDefinition) {
        return Optional.ofNullable(database.getEndpointMethod(endpointDefinition));
    }

    public Binary invokeService(MaritimeId id, BroadcastMessage m, MmsBroadcastOptions options, String attachmentId) {
        DispatchedMessage dm = mmsClient.broadcast(m, options);
        // statuses.put(dm.getMessageId().hexString(), new Status(dm));
        if (attachmentId != null) {

        }
        return dm.getMessageId();
    }


    private void register(String name, String uri) {

        database.getEndpointMethod(name);

        if (registrations.containsKey(name)) {
            throw new IllegalArgumentException("A service for '" + name + "' has already been registered");
        }
    }

    public static class Ip {
        final EndpointMethod m;

        final CompletableFuture<Object> cf;

        Ip(CompletableFuture<Object> cf, EndpointMethod m) {
            this.m = requireNonNull(m);
            this.cf = requireNonNull(cf);
        }

    }

    public static class InvokedEndpoint {
        final EndpointInvocationFuture<?> m;

        InvokedEndpoint(EndpointInvocationFuture<?> m) {
            this.m = m;
        }

        public EndpointInvocationFuture<?> getMessage() {
            return m;
        }
    }

    public static class Registration {
        final Destination d;

        Registration(EndpointRegistration e, Destination d) {
            this.d = d;
        }

        public void cancel() {

        }
    }
}
