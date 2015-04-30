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
package net.maritimecloud.sandbox.tls.client;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Kasper Nielsen
 */
public abstract class AbstractClient {
    public static final URI WSS = URI.create("wss://localhost:8443/websockettest/");
    public static final URI CUSTOM_WSS = URI.create("wss://localhost.maritimecloud.net:8443/websockettest/");

    /** returns the file path to the given resource */
    public static String getResourcePath(String resource) throws URISyntaxException {
        return AbstractClient.class.getResource("/" + resource).toExternalForm().substring("file:".length());
    }
}
