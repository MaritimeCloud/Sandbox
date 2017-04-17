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
public interface Serializer {

    String writeBroadcast(DynamicBroadcastMessage m, MessageHeader h);

    String writeBroadcastAck(String broadcastType, String attachementId, MessageHeader h);

    String writeBroadcastStatus(DispatchedMessage dm);

    String writeServiceResponse(String endpointType, String attachementId, ValueSerializer<Object> serializer,
            Object result);

    String writeService(EndpointMethod paramEndpointMethod, MessageHeader paramMessageHeader,
            DynamicMessage paramDynamicMessage);
}
