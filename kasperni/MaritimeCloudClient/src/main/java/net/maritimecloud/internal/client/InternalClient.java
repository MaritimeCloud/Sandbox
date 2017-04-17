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
package net.maritimecloud.internal.client;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.internal.client.configuration.ClientConfiguration;
import net.maritimecloud.internal.client.destination.ConnectionLogger;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.destination.FileManager;
import net.maritimecloud.internal.client.mms.BroadcastManager;
import net.maritimecloud.internal.client.mms.EndpointManager;
import net.maritimecloud.internal.client.rest.BadRequestMapper;
import net.maritimecloud.internal.client.rest.NotFoundMapper;
import net.maritimecloud.internal.client.rest.RestBroadcast;
import net.maritimecloud.internal.client.rest.RestConnection;
import net.maritimecloud.internal.client.rest.RestEndpoint;
import net.maritimecloud.internal.client.serialization.JsonSerializer;
import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.internal.msdl.db.MsdlDatabaseConfiguration;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.util.geometry.Position;
import net.maritimecloud.util.geometry.PositionReader;

import org.cakeframework.container.Container;
import org.cakeframework.container.Container.State;
import org.cakeframework.container.ContainerConfiguration;
import org.cakeframework.container.Containers;
import org.cakeframework.integration.jetty.JettyConfiguration;
import org.cakeframework.util.Configuration;

/**
 *
 * @author Kasper Nielsen
 */
public class InternalClient {

    private final Container container;


    public InternalClient(ClientConfiguration configuration) throws IOException {
        ContainerConfiguration conf = new ContainerConfiguration();

        conf.addService(configuration);
        conf.addService(this);


        conf.addService(BroadcastManager.class);
        conf.addService(EndpointManager.class);
        // Configure MSDL database
        // conf.getMap("msdl");

        DefaultMsdlDatabase db = MsdlDatabaseConfiguration.create(configuration.getMsdlRoot().toAbsolutePath());
        conf.addService(db);


        // Configure MMS
        MmsClientConfiguration mcc = MmsClientConfiguration.create(configuration.getId());
        ConnectionLogger cl = new ConnectionLogger();
        mcc.addListener(cl);
        mcc.setPositionReader(PositionReader.fixedPosition(Position.create(1, 1)));
        mcc.setHost(configuration.getMmsHost());

        mcc.properties().setDescription(configuration.getDescription());
        mcc.properties().setName(configuration.getName());
        mcc.properties().setOrganization(configuration.getOrganization());

        mcc.addListener(new MmsConnection.Listener() {
            @Override
            public void textMessageReceived(String message) {
                System.out.println("Received:" + message);
            }

            @Override
            public void textMessageSend(String message) {
                System.out.println("Sending :" + message);
            }
        });

        if (configuration.getPositionSupplier() != null) {
            mcc.setPositionReader(PositionReader.fromString(configuration.getPositionSupplier()));
        }


        conf.addService(mcc.build());

        conf.addService(FileManager.class);
        conf.addService(DestinationManager.class);
        conf.addService(JsonSerializer.class);

        conf.addService(cl);

        // Configure REST
        JettyConfiguration.configure(conf).setPort(configuration.getPort());
        conf.addResource(RestBroadcast.class);
        conf.addResource(RestEndpoint.class);
        conf.addResource(RestConnection.class);
        conf.addResource(BadRequestMapper.class);
        conf.addResource(NotFoundMapper.class);


        container = conf.create();
    }

    public boolean awaitTerminated(long timeout, TimeUnit unit) throws InterruptedException {
        return container.awaitState(State.TERMINATED, timeout, unit);
    }


    public <T> T getService(Class<T> service) {
        return container.getService(service);
    }

    public void shutdown() {
        container.shutdown();
    }

    public void startBlocking() {
        container.start().join();
    }

    public static void maidn(String[] args) throws Exception {

        args = new String[] { "/Users/kasperni/dma-workspace/MaritimeCloudClient/client/conf/mc-client.yaml" };

        ContainerConfiguration cc = configure(null, args);

        // Lets start and await CTRL-C
        Containers.startAndAwaitTermination(cc);

    }

    static ContainerConfiguration configure(Path clientHome, String[] args) throws Exception {
        Configuration conf = Configuration.fromFile(args[0]);

        MaritimeId id = MaritimeId.create(conf.getString("id"));

        ContainerConfiguration cc = new ContainerConfiguration();
        cc.addService(clientHome);
        cc.addService(id);

        // Configure Almanac

        // Configure MSDL database
        // conf.getMap("msdl");
        cc.addService(MsdlDatabaseConfiguration.create(Paths
                .get("/Users/kasperni/dma-workspace/MaritimeCloudClient/src/main/msdl")));

        // Configure MMS
        cc.addService(MmsClientConfiguration.create("mmsi:123456")
                .setPositionReader(PositionReader.fixedPosition(Position.create(1, 1))).build());

        // Configure REST
        JettyConfiguration.configure(cc).setPort(9090);
        cc.addResource(RestBroadcast.class);
        cc.addResource(RestEndpoint.class);
        cc.addResource(BadRequestMapper.class);
        cc.addResource(NotFoundMapper.class);

        return cc;

    }
}
