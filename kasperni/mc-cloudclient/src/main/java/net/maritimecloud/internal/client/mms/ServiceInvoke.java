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

import static net.maritimecloud.internal.client.rest.JsonUtil.extract;
import static net.maritimecloud.internal.client.rest.JsonUtil.validateParameters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import net.maritimecloud.core.id.MaritimeId;
import net.maritimecloud.internal.client.destination.Destination;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.destination.FileManager;
import net.maritimecloud.internal.client.rest.JsonUtil;
import net.maritimecloud.internal.client.serialization.Constants;
import net.maritimecloud.internal.client.serialization.Serializer;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonString;
import net.maritimecloud.internal.message.text.json.JsonMessageReader;
import net.maritimecloud.internal.mms.client.DefaultMmsClient;
import net.maritimecloud.internal.msdl.dynamic.DynamicMessage;
import net.maritimecloud.internal.util.logging.Logger;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.message.ValueSerializer;
import net.maritimecloud.msdl.model.EndpointMethod;
import net.maritimecloud.net.EndpointInvocationFuture;

/**
 *
 * @author Kasper Nielsen
 */
public class ServiceInvoke {

    /** The logger. */
    private static final Logger LOG = Logger.get(ServiceInvoke.class);

    DestinationManager dm;

    Serializer serializer;

    EndpointManager bm;

    DefaultMmsClient client;

    public ServiceInvoke(DestinationManager dm, Serializer serializer, EndpointManager bm, DefaultMmsClient client) {
        this.dm = dm;
        this.serializer = serializer;
        this.bm = bm;
        this.client = client;
    }

    public String fromJSONRest(JsonObject o) throws IOException {
        return fromJSON(o, null);
    }

    public String fromJSONFile(FileManager fm, String o, Path path) throws IOException {
        try {
            String result = fromJSON(JsonUtil.parseJson(o), path);
            LOG.info("Send broadcast " + path);
            return result;
        } catch (NotFoundException | BadRequestException e) {
            LOG.error("Could not send broadcast " + path + ", Reason: " + e.getMessage(), e);
            Path moveTo = fm.getHome().resolve(FileManager.FILEIO_FAIL);

            Path newPath = moveTo.resolve(path.getFileName());

            Path errorMsg = moveTo.resolve(path.getFileName() + ".error");

            try {
                try (OutputStream out = Files.newOutputStream(errorMsg); PrintStream ps = new PrintStream(out)) {
                    e.printStackTrace(ps);
                }
                LOG.info("Wrote error msg " + errorMsg);
            } catch (Exception t) {
                LOG.error("Failed du write error msg", t);
            }

            try {
                Files.move(path, newPath);
            } catch (Exception t) {
                LOG.error("Failed to move file " + path + " to " + newPath, t);
            }
            return null;
        }
    }

    private String fromJSON(JsonObject o, Path p) throws IOException {
        validateParameters(o, Constants.ALLOWED_ENDPOINT_INVOKE);

        // Extract Attachment ID
        String attachmentId = null;

        String receiverId = extract(o, JsonString.class, Constants.INVOKE_RECEIVER_ID, true).getString();
        MaritimeId id = MaritimeId.create(receiverId);
        // Extract Broadcast Type

        String endpointType = extract(o, JsonString.class, Constants.INVOKE_ENDPOINT_TYPE, true).getString();

        EndpointMethod em = bm.getEndpointDefinition(endpointType).orElseThrow(
                () -> new NotFoundException("Unknown endpoint type '" + endpointType + "'."));

        // Extract Broadcast Message
        JsonObject parameters = extract(o, JsonObject.class, Constants.INVOKE_PARAMETERS, false);
        final MessageReader mr;
        if (parameters != null) {
            mr = new JsonMessageReader(parameters);
        } else {
            mr = new JsonMessageReader("{}");
        }
        DynamicMessage dm = DynamicMessage.readFrom(em.getFullName(), em.getParameters(), mr);

        @SuppressWarnings("unchecked")
        ValueSerializer<Object> resultSerializer = (ValueSerializer<Object>) DynamicMessage.valueSerializer(em
                .getReturnType());

        EndpointInvocationFuture<?> f = client
                .invokeRemote(id, em.getFullName(), dm, dm.serializer(), resultSerializer);

        // write status

        if (p != null) {
            Path response = p.getParent().resolve(p.getFileName() + ".response");
            Destination d = this.dm.create(p.getParent());
            f.handle((result, t) -> {
                if (t == null) {
                    String msg = serializer.writeServiceResponse(endpointType, null, resultSerializer, result);
                    d.send(response.getFileName().toString(), msg);
                } else {
                    t.printStackTrace();
                }
            });
        }


        if (p != null) {
            try {
                Files.delete(p);
            } catch (IOException e) {
                LOG.error("Could not delete file " + p, e);
            }
        }
        // Send it and return the message id
        // statuses.put(f.getMessageId().hexString(), new Status(f));


        // JsonString ackURL = extract(o, JsonString.class, Constants.BROADCAST_ACK_ADDRESS, false);
        // if (ackURL != null) {
        // Destination d = dm.createRest(ackURL.getString());
        // options.onRemoteReceive(h -> {
        // d.send("broadcast_ack_" + h.getMessageId().hexString() + "_" + h.getSender().getId(),
        // serializer.writeBroadcastAck(broadcastType, attachmentId, h));
        // });
        // }

        return f.getMessageId().hexString();
    }

}
