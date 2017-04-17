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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Kasper Nielsen
 */
public interface Constants {

    String BROADCAST_ACK_ADDRESS = "broadcastAck";

    String BROADCAST_MESSAGE = "broadcast";

    String BROADCAST_TYPE = "broadcastType";

    String COMMON_ATTACHMENT = "attachment";

    String COMMON_MESSAGE_ID = "messageId";

    String COMMON_SENDER_ID = "senderId";

    String COMMON_SENDER_POSITION = "senderPosition";

    String COMMON_SENDER_TIMESTAMP = "senderTimestamp";

    String COMMON_RECEIVER_ID = "receiverId";

    String COMMON_RECEIVER_POSITION = "receiverPosition";

    String COMMON_RECEIVER_TIMESTAMP = "receiverTimestamp";

    String INVOKE_ENDPOINT_TYPE = "endpointType";

    String INVOKE_ENDPOINT_RESULT = "result";

    String INVOKE_PARAMETERS = "parameters";

    String INVOKE_RECEIVER_ID = "receiverId";

    Set<String> ALLOWED_BROADCAST_RECEIVE = immutable(COMMON_MESSAGE_ID, COMMON_SENDER_ID, COMMON_SENDER_TIMESTAMP,
            COMMON_SENDER_POSITION, BROADCAST_TYPE, BROADCAST_MESSAGE);

    // Area Coverage
    // Ack
    Set<String> ALLOWED_BROADCAST_SEND = immutable(BROADCAST_MESSAGE, BROADCAST_TYPE, BROADCAST_ACK_ADDRESS,
            COMMON_ATTACHMENT);

    Set<String> ALLOWED_ENDPOINT_INVOKE = immutable(COMMON_RECEIVER_ID, INVOKE_ENDPOINT_TYPE, INVOKE_PARAMETERS);

    static Set<String> immutable(String... values) {
        return Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(values)));
    }
}
