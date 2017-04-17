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

import java.io.StringReader;
import java.util.Collection;

import javax.ws.rs.BadRequestException;

import net.maritimecloud.internal.core.javax.json.Json;
import net.maritimecloud.internal.core.javax.json.JsonException;
import net.maritimecloud.internal.core.javax.json.JsonObject;
import net.maritimecloud.internal.core.javax.json.JsonReader;
import net.maritimecloud.internal.core.javax.json.JsonValue;


/**
 *
 * @author Kasper Nielsen
 */
public abstract class JsonUtil {

    @SuppressWarnings("unchecked")
    public static <T extends JsonValue> T extract(JsonObject parent, Class<T> type, String name, boolean required) {
        JsonValue value = parent.get(name);
        if (required && value == null) {
            throw new BadRequestException("a key-value pair with key '" + name + "' is required for the message");
        } else if (value != null && !type.isAssignableFrom(value.getClass())) {
            throw new BadRequestException("Illegal value for '" + name + "', expected a " + type.getSimpleName());
        }
        return (T) value;
    }

    public static JsonObject parseJson(String message) {
        JsonReader r = Json.createReader(new StringReader(message));
        try {
            return r.readObject();
        } catch (JsonException e) {
            System.err.println(message);
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    public static void validateParameters(JsonObject o, Collection<String> allowedKeys) {
        for (String s : o.keySet()) {
            if (!allowedKeys.contains(s)) {
                throw new BadRequestException("Unknown key '" + s + "', keys allowed : " + allowedKeys);
            }
        }
    }
}
