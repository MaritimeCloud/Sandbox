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
package net.maritimecloud.sandbox.tls.server;

import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 *  In order to run this class, you ensure that:
 *  <ul>
 *      <li>You have added localhost.maritimecloud.net for localhost in /etc/hosts</li>
 *      <li>The keystore should be placed at ~/cert/maritimecloud.net.jks</li>
 *      <li>The java class must be run with system properties for KeyStorePassword and KeyManagerPassword, i.e.
 *      -DKeyStorePassword=XXX -DKeyManagerPassword=YYY</li>
 *  </ul>
 *
 *
 * @author Kasper Nielsen
 */
public class SSL_CustomCertificate extends AbstractSSLServer {

    public static void main(String[] args) throws Exception {
        new SSL_CustomCertificate().start(args);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure(SslContextFactory sslContextFactory) {
        System.out.println("Using Key Store: " + System.getProperty("user.home") + "/cert/maritimecloud.net.jks");
        System.out.println("Using KeyStorePassword: " + System.getProperty("KeyStorePassword"));
        System.out.println("Using KeyManagerPassword: " + System.getProperty("KeyManagerPassword"));
        sslContextFactory.setKeyStorePath(System.getProperty("user.home") + "/cert/maritimecloud.net.jks");
        sslContextFactory.setKeyStorePassword(System.getProperty("KeyStorePassword"));
        sslContextFactory.setKeyManagerPassword(System.getProperty("KeyManagerPassword"));
    }

}
