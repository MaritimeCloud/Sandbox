/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.maritimecloud.idreg.client;

import java.io.Reader;
import java.util.ServiceLoader;

/**
 * Factory for creating an instantiated OpenID Connect client
 */
public class OIDCClientFactory {

    private static ServiceLoader<OIDCClient> oidcLientLoader
            = ServiceLoader.load(OIDCClient.class);

    /**
     * Creates and instantiates a new OpenID Connect client
     *
     * @param settings provider specific settings used to initialize the client
     * @param customClaims custom claims to extract from the access token
     * @return the new initialized OpenID Connect client
     */
    public static OIDCClient newOIDCClient(Reader settings, String... customClaims) {
        OIDCClient client = oidcLientLoader.iterator().next();
        client.init(settings, customClaims);
        return client;
    }
}
