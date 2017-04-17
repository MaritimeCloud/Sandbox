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
import static net.maritimecloud.internal.client.rest.JsonUtil.parseJson;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.maritimecloud.internal.client.destination.Destination;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.mms.BroadcastDispatch;
import net.maritimecloud.internal.client.mms.BroadcastManager;
import net.maritimecloud.internal.client.mms.BroadcastManager.SendBroadcastStatus;
import net.maritimecloud.internal.client.serialization.Serializer;
import net.maritimecloud.msdl.model.BroadcastMessageDeclaration;
import net.maritimecloud.util.geometry.Area;

/**
 * REST endpoints for broadcasting.
 *
 * @author Kasper Nielsen
 */
@Path("/broadcast")
public class RestBroadcast {

    /** The broadcast manager. */
    private final BroadcastManager bm;

    private final Serializer serialzer;

    private final DestinationManager dm;

    public RestBroadcast(BroadcastManager bm, Serializer serialzer, DestinationManager dm) {
        this.bm = requireNonNull(bm);
        this.dm = dm;
        this.serialzer = requireNonNull(serialzer);
    }

    /**
     * Returns the status of broadcast that has been sent
     *
     * @param messageId
     *            the message id of the broadcast
     * @return the JSON representation of the status
     */
    @Path("status/{messageId}")
    @GET
    public String getStatus(@PathParam("messageId") String messageId) {
        SendBroadcastStatus s = bm.getBroadcastStatus(messageId).orElseThrow(
                () -> new NotFoundException("Unknown broadcast message id'" + messageId
                        + "'. The status of a broadcast is only retained for one hour after it has been sent."));
        return serialzer.writeBroadcastStatus(s.getMessage());
    }

    @Path("send")
    @POST
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED })
    @Produces(MediaType.TEXT_PLAIN)
    public String sendBroadcast(String message) throws IOException {
        return new BroadcastDispatch(dm, serialzer, bm).fromJSONRest(parseJson(message));
    }

    @Path("subscribe")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String subscribe(@FormParam("type") String broadcastType, @FormParam("url") String receiveURL) {
        BroadcastMessageDeclaration bmd = bm.getBroadcastDefinition(broadcastType).orElseThrow(
                () -> new NotFoundException("Unknown broadcast type '" + broadcastType + "'."));

        Destination d = dm.createRest(receiveURL);

        // Extract Broadcast Coverage (Optional) Not implemented yet
        Area area = null;

        return bm.broadcastSubscribe(bmd, d, area).getId().hexString();
    }

    @Path("unsubscribe")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public void unsubscribe(@FormParam("id") String subscriptionId) {
        BroadcastManager.Subscription s = bm.getSubscription(subscriptionId).orElseThrow(
                () -> new NotFoundException("Unknown subscription id'" + subscriptionId
                        + "'. Maybe the scrubscription has already been cancelled."));
        s.cancel();
    }
}
