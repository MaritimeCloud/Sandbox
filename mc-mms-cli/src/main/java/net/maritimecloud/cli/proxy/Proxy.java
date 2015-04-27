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
package net.maritimecloud.cli.proxy;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;
import net.maritimecloud.cli.AbstractMMSCommandLineTool;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * IMPORTANT: This is work in progress. The proxy works fine if it targets an MMS server directly.
 * However, if the target server is, say, an nginx MMS proxy, this class does not work at all.<br/>
 * Maybe the "Host" header of the initial HTTP upgrade request needs to be re-written. Investigate...
 *
 * <p>
 * Starts an MMS proxy server that can be used for testing
 * dropped connections, re-connects, etc.
 * <p>
 * Example usage:
 * <pre>java -jar mc-mms-cli-0.3-SNAPSHOT.jar proxy -port 43234 mms-test.e-navigation.net:43234</pre>
 *
 */
public class Proxy extends AbstractMMSCommandLineTool {

    static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    /** Parameter for specifying a single MMSI ID */
    @Parameter(names="-port", description = "The client port that gets proxied to the MMS server")
    int port = 43233;


    /** {@inheritDoc} */
    @Override
    protected void runMMSCommand(Injector injector) throws Exception {
        int mmsPort = 43234;
        String mmsHost = server.get(0);
        if (mmsHost.matches(".*:\\d+")) {
            mmsPort = Integer.valueOf(mmsHost.substring(mmsHost.lastIndexOf(":") + 1));
            mmsHost = mmsHost.substring(0, mmsHost.lastIndexOf(":"));
        }
        LOG.info("Starting MMS proxy on localhost:" + port + " -> " + mmsHost + ":" + mmsPort);

        ProxyTester proxy = new ProxyTester(new InetSocketAddress("localhost", port), new InetSocketAddress(mmsHost, mmsPort));
        proxy.start();

        // TODO: Start background thread that kills connections randomly
    }

    /**
     * Main method of the Proxy class
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new Proxy().execute(args);
    }
}
