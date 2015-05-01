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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

/**
 *
 * @author Kasper Nielsen
 */
public abstract class AbstractClient {
    public static final URI WSS = URI.create("wss://localhost:8443/websockettest/");
    public static final URI CUSTOM_WSS = URI.create("wss://localhost.maritimecloud.net:8443/websockettest/");

    /**
     * Loads the truststore with the self-signed certificate used by the server
     * @return the trust store managers
     */
    public static TrustManager[] loadTrustStore() throws Exception {
        String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory instance = TrustManagerFactory.getInstance(defaultAlgorithm);
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        trustStore.load(AbstractClient.class.getResourceAsStream("/client-truststore.jks"), "changeit".toCharArray());

        instance.init(trustStore);
        return instance.getTrustManagers();
    }

    /**
     * Loads the truststore with a self-signed certificate used by the client
     * @return the trust store managers
     */
    public static KeyManager[] loadKeyManagers() throws Exception {
        String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory instance = KeyManagerFactory.getInstance(defaultAlgorithm);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(AbstractClient.class.getResourceAsStream("/client-keystore.jks"), "changeit".toCharArray());

        instance.init(keyStore, "changeit".toCharArray());
        return instance.getKeyManagers();
    }


    /** returns the file path to the given resource */
    public static String getResourcePath(String resource) throws URISyntaxException {
        return AbstractClient.class.getResource("/" + resource).toExternalForm().substring("file:".length());
    }
}
