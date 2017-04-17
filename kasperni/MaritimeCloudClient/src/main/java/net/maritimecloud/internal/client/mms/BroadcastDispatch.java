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

import net.maritimecloud.internal.client.destination.Destination;
import net.maritimecloud.internal.client.destination.DestinationManager;
import net.maritimecloud.internal.client.destination.FileManager;
import net.maritimecloud.internal.client.rest.JsonUtil;
import net.maritimecloud.internal.client.serialization.Constants;
import net.maritimecloud.internal.client.serialization.Serializer;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonString;
import net.maritimecloud.internal.message.text.json.JsonMessageReader;
import net.maritimecloud.internal.msdl.dynamic.DynamicBroadcastMessage;
import net.maritimecloud.internal.util.logging.Logger;
import net.maritimecloud.message.MessageReader;
import net.maritimecloud.msdl.model.BroadcastMessageDeclaration;
import net.maritimecloud.net.mms.MmsBroadcastOptions;
import net.maritimecloud.util.geometry.Area;

/**
 *
 * @author Kasper Nielsen
 */
public class BroadcastDispatch {

    /** The logger. */
    private static final Logger LOG = Logger.get(BroadcastDispatch.class);

    final DestinationManager dm;

    final Serializer serializer;

    final BroadcastManager bm;

    public BroadcastDispatch(DestinationManager dm, Serializer serializer, BroadcastManager bm) {
        this.dm = dm;
        this.serializer = serializer;
        this.bm = bm;
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
        MmsBroadcastOptions options = new MmsBroadcastOptions();


        validateParameters(o, Constants.ALLOWED_BROADCAST_SEND);

        // Extract Attachment ID
        String attachmentId = null;

        // Extract Broadcast Type

        String broadcastType = extract(o, JsonString.class, Constants.BROADCAST_TYPE, true).getString();

        BroadcastMessageDeclaration bmd = bm.getBroadcastDefinition(broadcastType).orElseThrow(
                () -> new NotFoundException("Unknown broadcast type '" + broadcastType + "'."));

        // Extract Broadcast Message
        JsonObject broadcast = extract(o, JsonObject.class, Constants.BROADCAST_MESSAGE, true);
        MessageReader broadcastReader = new JsonMessageReader(broadcast);
        DynamicBroadcastMessage m = DynamicBroadcastMessage.readFrom(bmd, broadcastReader);

        // Extract Broadcast ACKing (Optional)
        JsonString ackURL = extract(o, JsonString.class, Constants.BROADCAST_ACK_ADDRESS, false);
        if (ackURL != null) {
            Destination d = dm.createRest(ackURL.getString());
            options.onRemoteReceive(h -> {
                d.send("broadcast_ack_" + h.getMessageId().hexString() + "_" + h.getSender().getId(),
                        serializer.writeBroadcastAck(broadcastType, attachmentId, h));
            });
        }

        // Extract Broadcast Coverage (Optional) Not implemented yet
        Area area = null;
        options.toArea(area);


        String id = bm.sendBroadcast(m, options, attachmentId).hexString();

        if (p != null) {
            try {
                Files.delete(p);
            } catch (IOException e) {
                LOG.error("Could not delete file " + p, e);
            }
        }

        return id;

    }
}
