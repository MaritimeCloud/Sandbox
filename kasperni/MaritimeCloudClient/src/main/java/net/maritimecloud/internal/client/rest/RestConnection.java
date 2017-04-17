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
package net.maritimecloud.internal.client.rest;

import static java.util.Objects.requireNonNull;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.maritimecloud.internal.client.destination.ConnectionLogger;
import net.maritimecloud.internal.mms.client.DefaultMmsClient;

/**
 *
 * @author Kasper Nielsen
 */
@Path("/connection")
public class RestConnection extends JsonUtil {

    /** The MMS client. */
    private final DefaultMmsClient mmsClient;

    private final ConnectionLogger logger;

    public RestConnection(DefaultMmsClient client, ConnectionLogger logger) {
        this.mmsClient = requireNonNull(client);
        this.logger = requireNonNull(logger);
    }

    @Path("log")
    @GET
    public String log() {
        return logger.getFullConnectionLog();
    }

    @Path("status")
    @GET
    public String status() {
        return mmsClient.connection().isConnected() ? "Connected" : "Disconnected";
    }

    @Path("log/received")
    @GET
    public String logReceived() {
        return logger.getInfo(false);
    }

    @Path("log/send")
    @GET
    public String logSend() {
        return logger.getInfo(true);
    }
}
