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
import net.maritimecloud.core.id.MmsiId;
import net.maritimecloud.net.mms.MmsClient;
import net.maritimecloud.net.mms.MmsClientConfiguration;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User for benchmarking the MMS server.
 */
public class Benchmark extends AbstractMMSCommandLineTool {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Benchmark.class);

    @Parameter(names="-id", description = "MMSI ID")
    Integer id = null;

    @Parameter(names="-ids", description = "List of MMSI IDs, in the form m1,m2,m3-m4")
    String ids = null;

    /** {@inheritDoc} */
    @Override
    protected void runMMSCommand(Injector injector) throws Exception {

        List<MmsiId> mmsiList = parseMmsiSpec(ids);
        if (id != null) {
            mmsiList.add(new MmsiId(id));
        }

        // Launch the MMS clients
        List<MmsClient> clients = new ArrayList<>();
        mmsiList.parallelStream().forEach(mmsi -> {
            try {
                MmsClientConfiguration conf = MmsClientConfiguration.create(mmsi);
                clients.add(createMmsClient(conf));
                LOG.info("Connected successfully to cloud server: " + getHostURL() + " with shipId " + mmsi);
            } catch (Exception ex) {
                LOG.error("Failed connecting to cloud server: " + getHostURL() + " with shipId " + mmsi, ex);
            }
        });

        // Sleep
        sleep(4000L);

        // Terminate the clients
        clients.parallelStream().forEach(mmsClient -> {
            try {
                mmsClient.shutdown();
                mmsClient.awaitTermination(2, TimeUnit.SECONDS);
                LOG.info("Terminated the MMS client " + mmsClient.getClientId());
            } catch (InterruptedException e) {
                LOG.error("Failed shutting down MMS client", e);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new Benchmark().execute(args);
    }
}
