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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.internal.client.configuration.BroadcastConfiguration.BroadcastGroup;
import net.maritimecloud.internal.client.configuration.ClientConfiguration;
import net.maritimecloud.internal.client.configuration.ServiceConfiguration.ServiceGroup;
import net.maritimecloud.internal.msdl.db.DefaultMsdlDatabase;
import net.maritimecloud.internal.msdl.db.MsdlDatabaseConfiguration;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;

import testproject.Hello;
import testproject.HelloWorld;

/**
 *
 * @author Kasper Nielsen
 */
public class AbstractRestTest extends AbstractNetworkTest {
    InternalClient ic;

    int cliPort;

    protected MaritimeId id = MaritimeId.create("mmsi:123123");

    protected ClientConfiguration setupUpClient() throws IOException {
        ClientConfiguration cc = new ClientConfiguration(testroot).setMmsHost("localhost:" + clientPort)
                .setPort(cliPort).setId(id);

        DefaultMsdlDatabase db = MsdlDatabaseConfiguration.create(cc.getMsdlRoot().toAbsolutePath());

        cc.getBroadcasts().addGroup(
                new BroadcastGroup().addDestination(broadcastIn.toFile()).addType(
                        db.getBroadcastMessage(HelloWorld.NAME)));

        cc.getEndpoints().addGroup(
                new ServiceGroup().setDestination(endpointIn.toFile())
                        .addEndpoint(db.getEndpointDefinition(Hello.NAME))

                );

        return cc;
    }

    @Before
    public void setup2() throws IOException {
        cliPort = ThreadLocalRandom.current().nextInt(30000, 40000);
        ic = new InternalClient(setupUpClient());
        ic.startBlocking();
    }

    @After
    public void tearDown2() throws InterruptedException {
        ic.shutdown();
        assertTrue(ic.awaitTerminated(1, TimeUnit.SECONDS));
    }

    protected String get(String url, int expectedReturnCode) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:" + cliPort + "/" + url);
        // httpGet. .setEntity(new StringEntity(msg));
        try (CloseableHttpResponse response2 = httpclient.execute(httpGet)) {
            assertEquals(expectedReturnCode, response2.getStatusLine().getStatusCode());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            return EntityUtils.toString(entity2);
        }
    }

    protected String post(String url, int expectedReturnCode, String msg) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:" + cliPort + "/" + url);
        httpPost.setEntity(new StringEntity(msg));
        try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
            assertEquals(expectedReturnCode, response2.getStatusLine().getStatusCode());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            return EntityUtils.toString(entity2);
        }
    }
}
