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
package net.maritimecloud.cli.benchmark;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;
import dk.dma.commons.app.AbstractCommandLineTool;
import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import net.maritimecloud.net.mms.MmsConnection;
import net.maritimecloud.net.mms.MmsConnectionClosingCode;
import net.maritimecloud.util.geometry.PositionReader;
import net.maritimecloud.util.geometry.PositionTime;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User for benchmarking the MMS server.
 */
public class Benchmark extends AbstractCommandLineTool {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Benchmark.class);

    /** Default parameter */
    @SuppressWarnings("all")
    @Parameter(description = "ws[s]://hostname[:port]", required = true)
    private List<String> server = new ArrayList<>();

    @Parameter(names="-id", description = "MMSI ID")
    Integer id = 1;

    @Parameter(names={ "-v", "--verbose" }, description = "Verbosity")
    boolean verbose = false;

    @Parameter(names={ "-bin" }, description = "Will use a binary Protobuf protocol instead of a text-based JSON protocol")
    boolean binary = false;

    /** {@inheritDoc} */
    @Override
    protected void run(Injector injector) throws Exception {

        // Set the wire protocol
        System.setProperty("net.maritimecloud.mms.use_binary", String.valueOf(binary));


        MaritimeId mmsi = new MmsiId(id);
        MmsClientConfiguration conf = MmsClientConfiguration.create(mmsi);

        // Hook up a position reader
        conf.setPositionReader(new PositionReader() {
            @Override
            public PositionTime getCurrentPosition() {
                return PositionTime.create(0.0, 0.0, System.currentTimeMillis());
            }
        });

        // Check if we need to log the MaritimeCloudConnection activity
        if (verbose) {
            conf.addListener(new MmsConnection.Listener() {
                @Override
                public void connecting(URI host) {
                    LOG.info("Connecting to " + host);
                }

                @Override
                public void connected(URI host) {
                    LOG.info("Connected to " + host);
                }

                @Override
                public void binaryMessageReceived(byte[] message) {
                    LOG.info("Received binary message: " + (message == null ? 0 : message.length) + " bytes");
                }

                @Override
                public void binaryMessageSend(byte[] message) {
                    LOG.info("Sending binary message: " + (message == null ? 0 : message.length) + " bytes");
                }

                @Override
                public void textMessageReceived(String message) {
                    LOG.info("Received text message: " + message);
                }

                @Override
                public void textMessageSend(String message) {
                    LOG.info("Sending text message: " + message);
                }

                @Override
                public void disconnected(MmsConnectionClosingCode closeReason) {
                    LOG.info("Disconnected with reason: " + closeReason);
                }
            });
        }

        try {

            // Set the host
            String host = server.get(0);
            if (!host.toLowerCase().startsWith("ws://") && !host.toLowerCase().startsWith("wss://")) {
                host = "ws://" + host;
            }
            if (!host.matches(".*:\\d+")) {
                host += host.toLowerCase().startsWith("ws://") ? ":80" : ":443";
            }
            conf.setHost(host);

            MmsClient mmsClient = conf.build();
            if (mmsClient == null) {
                throw new Exception("Failed creating MMS client");
            }

            LOG.info("Connected successfully to cloud server: " + host + " with shipId " + mmsi);
            Thread.sleep(2000L);

            mmsClient.shutdown();
            mmsClient.awaitTermination(2, TimeUnit.SECONDS);

        } catch (Exception e) {
            LOG.error("Failed to connect to server: " + e);
        }

    }

    public static void main(String[] args) throws Exception {
        new Benchmark().execute(args);
    }
}
