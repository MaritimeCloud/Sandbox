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
package net.maritimecloud.internal.client.serialization;

import static net.maritimecloud.internal.msdl.parser.antlr.StringUtil.LINE_SEPARATOR;
import net.maritimecloud.internal.message.text.json.JsonValueWriter;
import net.maritimecloud.internal.msdl.dynamic.DynamicBroadcastMessage;
import net.maritimecloud.internal.msdl.dynamic.DynamicMessage;
import net.maritimecloud.message.ValueSerializer;
import net.maritimecloud.msdl.model.EndpointMethod;
import net.maritimecloud.net.DispatchedMessage;
import net.maritimecloud.net.MessageHeader;

/**
 *
 * @author Kasper Nielsen
 */
public class JsonSerializer implements Constants, Serializer {

    public String writeBroadcast(DynamicBroadcastMessage m, MessageHeader h) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(LINE_SEPARATOR);
        ws(sb, COMMON_MESSAGE_ID, h.getMessageId().hexString(), true);
        ws(sb, BROADCAST_TYPE, m.name(), true);
        ws(sb, COMMON_SENDER_ID, h.getSender().toString(), true);
        if (h.getSenderPosition() != null) {
            ws(sb, COMMON_SENDER_POSITION, h.getSenderPosition().toStringDegrees(), true);
        }
        w(sb, COMMON_SENDER_TIMESTAMP, Long.toString(h.getSenderTime().getTime()), true);

        String msg;
        try {
            msg = JsonValueWriter.writeMessageTo(1, m, m.serializer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        sb.append("  \"").append(BROADCAST_MESSAGE).append("\" : ").append(msg).append("");
        sb.append(LINE_SEPARATOR);

        // ws(sb, BROADCAST_MESSAGE, msg, false);
        return sb.append("}").toString();
    }

    public String writeBroadcastAck(String broadcastType, String attachementId, MessageHeader h) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(LINE_SEPARATOR);
        ws(sb, COMMON_MESSAGE_ID, h.getMessageId().hexString(), true);
        ws(sb, BROADCAST_TYPE, broadcastType, true);
        ws(sb, COMMON_RECEIVER_ID, h.getSender().toString(), true);
        if (h.getSenderPosition() != null) {
            ws(sb, COMMON_RECEIVER_POSITION, h.getSenderPosition().toStringDegrees(), true);
        }
        w(sb, COMMON_RECEIVER_TIMESTAMP, Long.toString(h.getSenderTime().getTime()), true);

        return sb.append("}").toString();
    }

    public String writeBroadcastStatus(DispatchedMessage dm) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(LINE_SEPARATOR);
        ws(sb, COMMON_MESSAGE_ID, dm.getMessageId().hexString(), true);
        w(sb, "acknowledged", Boolean.toString(dm.relayed().isAcknowledged()), false);
        return sb.append("}").toString();
    }

    /** {@inheritDoc} */
    @Override
    public String writeService(EndpointMethod em, MessageHeader h, DynamicMessage dm) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(LINE_SEPARATOR);
        ws(sb, COMMON_MESSAGE_ID, h.getMessageId().hexString(), true);
        ws(sb, COMMON_SENDER_ID, h.getSender().toString(), true);
        if (h.getSenderPosition() != null) {
            ws(sb, COMMON_SENDER_POSITION, h.getSenderPosition().toStringDegrees(), true);
        }
        w(sb, COMMON_SENDER_TIMESTAMP, Long.toString(h.getSenderTime().getTime()), true);
        ws(sb, INVOKE_ENDPOINT_TYPE, em.getFullName(), !em.getParameters().isEmpty());

        if (!em.getParameters().isEmpty()) {
            String msg;
            try {
                msg = JsonValueWriter.writeMessageTo(1, dm, dm.serializer());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            sb.append("  \"").append(INVOKE_PARAMETERS).append("\" : ").append(msg).append("");
            sb.append(LINE_SEPARATOR);
        }

        return sb.append("}").toString();
    }

    /** {@inheritDoc} */
    @Override
    public String writeServiceResponse(String endpointType, String attachementId, ValueSerializer<Object> serializer,
            Object result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(LINE_SEPARATOR);
        ws(sb, INVOKE_ENDPOINT_TYPE, endpointType, result != null);
        if (result != null) {
            String msg;
            try {
                msg = JsonValueWriter.writeValueTo(1, result, serializer);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            sb.append("  \"").append(INVOKE_ENDPOINT_RESULT).append("\" : ").append(msg).append("");
            sb.append(LINE_SEPARATOR);
        }
        return sb.append("}").toString();
    }

    private static void w(StringBuilder sb, String key, String value, boolean hasNext) {
        sb.append("  \"").append(key).append("\" : ").append(value).append(hasNext ? "," : "").append(LINE_SEPARATOR);
    }

    private static void ws(StringBuilder sb, String key, String value, boolean hasNext) {
        sb.append("  \"").append(key).append("\" : \"").append(value).append("\"" + (hasNext ? "," : ""));
        sb.append(LINE_SEPARATOR);
    }
}
