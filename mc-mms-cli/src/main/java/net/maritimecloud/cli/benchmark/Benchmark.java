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
import net.maritimecloud.cli.AbstractMMSCommandLineTool;
import net.maritimecloud.cli.CliMmsClient;
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.mms.MmsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User for benchmarking the MMS server.
 * <p>
 * Example usage:
 * <pre>java -jar mc-mms-cli-0.3-SNAPSHOT.jar benchmark -ids 8880000,9990000-9990005 -n 3 -ttl 1000 -v wss://mms-test.e-navigation.net</pre>
 * <p>
 * To a certain extent, the command has been modelled over the Apache Benchmark command:
 * http://httpd.apache.org/docs/2.2/programs/ab.html
 */
public class Benchmark extends AbstractMMSCommandLineTool {

    static final Logger LOG = Logger.getLogger(Benchmark.class.getName());

    /** Parameter for specifying a single MMSI ID */
    @Parameter(names="-id", description = "MMSI ID")
    Integer id = null;

    /** Parameter for specifying a list of MMSI ID's */
    @Parameter(names="-ids", description = "List of MMSI ID's, in the form m1,m2,m3-m4")
    String ids = null;

    /** Parameter for specifying the time-to-live of the client connection */
    @Parameter(names="-ttl", description = "Time-to-live of the client connection in milliseconds")
    long ttl = 4000L; // milliseconds

    /** Parameter for specifying the time limit of the benchmark test in seconds */
    @Parameter(names="-t", description = "Time limit of the benchmark test in seconds")
    long timeLimit = 60 * 60L; // Seconds

    /** Parameter for specifying the number of concurrent clients */
    @Parameter(names="-c", description = "The number of concurrent clients. By default this will be identical to the number of MMSI ID's")
    Integer c = null;

    /** Parameter for specifying the number of connections for each client */
    @Parameter(names="-n", description = "The number of connections for each client")
    int n = 1;

    BenchmarkStats stats = new BenchmarkStats();
    List<CliMmsClient> clients = new ArrayList<>();
    boolean aborted;

    /** {@inheritDoc} */
    @Override
    protected void runMMSCommand(Injector injector) throws Exception {

        // Compute the list of MMSI ID's
        List<MmsiId> mmsiList = parseMmsiSpec(ids);
        if (id != null) {
            mmsiList.add(new MmsiId(id));
        }

        // Compute the level of concurrency
        if (c == null) {
            c = mmsiList.size();
        }
        ExecutorService pool = Executors.newFixedThreadPool(c);

        // Submit the benchmark jobs to the pool
        mmsiList.forEach(mmsi -> pool.execute(new MmsClientBenchmarkJob(mmsi)));

        // Await completion of the test
        try {
            pool.shutdown();
            if (!pool.awaitTermination(timeLimit, TimeUnit.SECONDS)) {
                aborted = true;
                LOG.log(Level.SEVERE, "Benchmark test did not teminate in " + timeLimit + " seconds. Shutting down");
                pool.shutdownNow();
            } else {
                LOG.info("Benchmark test completed successfully");
            }
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Error completing benchmark", e);
        }

        // Update and print statistics
        clients.forEach(stats::update);
        LOG.info(stats.toString());
    }

    /**
     * Main method of the Benchmark class
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new Benchmark().execute(args);
    }

    /**
     * The benchmark job for a single MMS client
     */
    class MmsClientBenchmarkJob implements Runnable {

        MmsiId id;

        /**
         * Constructor
         * @param id the MMSI of the client
         */
        public MmsClientBenchmarkJob(MmsiId id) {
            this.id = id;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            // Repeat the connection test n times
            for (int x = 0; !aborted && x < n; x++) {
                try {
                    // Launch the client
                    CliMmsClient cliClient = createMmsClient(id);
                    MmsClient mmsClient = cliClient.getMmsClient();
                    clients.add(cliClient);
                    if (aborted) {
                        return;
                    }
                    stats.newConnection();
                    LOG.info("Connected successfully to cloud server: " + getHostURL() + " with shipId " + id);

                    // Keep the client alive for at least the requested amount of time
                    // However, ensure that the client is connected before continuing
                    long t0 = System.currentTimeMillis();
                    while (!aborted) {
                        long dt = System.currentTimeMillis() - t0;
                        if (cliClient.isConnected() && dt > ttl) {
                            break;
                        }
                        Thread.sleep(5);
                    }
                    if (aborted) {
                        return;
                    }

                    // Shut down the client
                    mmsClient.shutdown();
                    mmsClient.awaitTermination(2, TimeUnit.SECONDS);
                    if (aborted) {
                        return;
                    }
                    LOG.info("Terminated the MMS client " + mmsClient.getClientId());

                    // Sleep a bit to ensure that the connection is shut down
                    Thread.sleep(100);

                } catch (Exception ex) {
                    stats.newError();
                    if (aborted) {
                        return;
                    }
                    LOG.log(Level.SEVERE, "Failed connecting to cloud server: " + getHostURL() + " with shipId " + id, ex);
                }
            }
        }
    }

    /** Compiles the benchmark statistics */
    class BenchmarkStats {
        long t0 = System.currentTimeMillis();
        int connections;
        int errors;
        int messagesReceived;
        int messagesSent;

        /** Register a new connection */
        public synchronized void newConnection() {
            connections++;
        }

        /** Register a new error */
        public void newError() {
            errors++;
        }

        /**
         * Update the statistics with the statistics of the given client
         * @param client the client to update the statistics for
         */
        public void update(CliMmsClient client) {
            this.messagesSent += client.getMessagesSent();
            this.messagesReceived += client.getMessagesReceived();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Stats:"
                    + "\n  Duration: " + ((System.currentTimeMillis() - t0) / 1000L) + " seconds"
                    + "\n  MMS Connections: " + connections
                    + "\n  MMS Errors: " + errors
                    + "\n  Messages Received: " + messagesReceived
                    + "\n  Messages Sent: " + messagesSent
                    ;
        }
    }
}
