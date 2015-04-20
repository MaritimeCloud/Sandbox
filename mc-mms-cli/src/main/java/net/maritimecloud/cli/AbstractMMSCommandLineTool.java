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
package net.maritimecloud.cli;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;
import dk.dma.commons.app.AbstractCommandLineTool;
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
import java.util.Arrays;
import java.util.List;

/**
 * Base class for MMS command line tools.
 * <p>
 * This class will define command line arguments for setting verbosity,
 * binary/text wire protocol and the URL of the MMS server
 */
public abstract class AbstractMMSCommandLineTool extends AbstractCommandLineTool {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractMMSCommandLineTool.class);

    /** Default parameter - the MMS server URL */
    @SuppressWarnings("all")
    @Parameter(description = "ws[s]://hostname[:port]", required = true)
    private List<String> server = new ArrayList<>();

    @Parameter(names={ "-v", "--verbose" }, description = "Verbosity")
    boolean verbose = false;

    @Parameter(names={ "-bin" }, description = "Will use a binary Protobuf protocol instead of a text-based JSON protocol")
    boolean binary = false;

    /** {@inheritDoc} */
    @Override
    protected final void run(Injector injector) throws Exception {

        // Set the wire protocol
        System.setProperty("net.maritimecloud.mms.use_binary", String.valueOf(binary));

        // Run using sub-class implementation
        runMMSCommand(injector);
    }

    /**
     * Run the MMS command line tool
     * @param injector the Google Guice injector
     */
    protected abstract void runMMSCommand(Injector injector) throws Exception;

    /**
     * Utility method for creating an MMS client based on the given configuration.
     * The configuration will be tied to a specific MMSI.
     *
     * @param conf the MMS client configuration
     * @return the MMS client
     */
    protected MmsClient createMmsClient(MmsClientConfiguration conf) throws Exception {

        if (conf.getPositionReader() == null) {
            // Hook up a dummy position reader
            conf.setPositionReader(new PositionReader() {
                @Override
                public PositionTime getCurrentPosition() {
                    return PositionTime.create(0.0, 0.0, System.currentTimeMillis());
                }
            });
        }

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

        // Set the host
        conf.setHost(getHostURL());

        MmsClient mmsClient = conf.build();
        if (mmsClient == null) {
            throw new Exception("Failed creating MMS client");
        }

        return mmsClient;
    }

    /**
     * Determines the MMS host URL from the default command line parameter
     * @return the MMS host URL
     */
    public String getHostURL() {
        String host = server.get(0);
        if (!host.toLowerCase().startsWith("ws://") && !host.toLowerCase().startsWith("wss://")) {
            host = "ws://" + host;
        }
        if (!host.matches(".*:\\d+")) {
            host += host.toLowerCase().startsWith("ws://") ? ":80" : ":443";
        }
        return host;
    }

    /**
     * Utility method for parsing an MMSI spec. Format:
     * <ul>
     *     <li>m1,m2,m3-m4</li>
     * </ul>
     * @param mmsiSpec the MMSI list
     * @return the list of individual MMSI
     */
    public List<MmsiId> parseMmsiSpec(String mmsiSpec) {
        List<MmsiId> result = new ArrayList<>();
        if (mmsiSpec != null) {
            Arrays.asList(mmsiSpec.split(",")).forEach(m -> {
                if (m.matches("\\d+-\\d+")) {
                    int m0 = Integer.valueOf(m.substring(0, m.indexOf("-")));
                    int m1 = Integer.valueOf(m.substring(m.indexOf("-") + 1, m.length()));
                    for (int mmsi = Math.min(m0, m1); mmsi <= Math.max(m0, m1); mmsi++) {
                        result.add(new MmsiId(mmsi));
                    }
                } else {
                    result.add(new MmsiId(Integer.valueOf(m)));
                }
            });
        }
        return result;
    }

    /**
     * Utility method for sleeping the current thread the given amount of time
     * @param ms the time to sleep
     */
    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOG.error("Alas, cannot even sleep properly");
        }
    }
}
