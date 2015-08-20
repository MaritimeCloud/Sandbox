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

import net.maritimecloud.idreg.client.keycloak.KeycloakClient;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests.
 */
public class OIDCClientTest {

    @Test
    public void testOIDCCLient() throws Exception {

        try (Reader r =  new InputStreamReader(getClass().getResourceAsStream("/keycloak.json"))) {
            OIDCClient client = OIDCClient.newBuilder()
                    .configuration(r)
                    .build();

            assertNotNull(client);
            assertTrue(client instanceof KeycloakClient);
        }
    }

}
